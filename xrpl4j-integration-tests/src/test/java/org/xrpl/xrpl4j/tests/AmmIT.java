package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoAuctionSlot;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoAuthAccount;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoRequestParams;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.common.TimeUtils;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.AmmLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.AmmClawbackFlags;
import org.xrpl.xrpl4j.model.flags.AmmDepositFlags;
import org.xrpl.xrpl4j.model.flags.AmmWithdrawFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.ledger.AmmObject;
import org.xrpl.xrpl4j.model.ledger.AuctionSlot;
import org.xrpl.xrpl4j.model.ledger.AuthAccount;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.AmmBid;
import org.xrpl.xrpl4j.model.transactions.AmmClawback;
import org.xrpl.xrpl4j.model.transactions.AmmCreate;
import org.xrpl.xrpl4j.model.transactions.AmmDeposit;
import org.xrpl.xrpl4j.model.transactions.AmmVote;
import org.xrpl.xrpl4j.model.transactions.AmmWithdraw;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

public class AmmIT extends AbstractIT {

  String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');
  String usd = "USD";

  @Test
  void depositAndVoteOnTradingFee() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();
    AmmInfoResult amm = createAmm(issuerKeyPair, feeResult);
    KeyPair traderKeyPair = createRandomAccountEd25519();

    AccountInfoResult traderAccount = scanForResult(
      () -> this.getValidatedAccountInfo(traderKeyPair.publicKey().deriveAddress())
    );

    AccountInfoResult traderAccountAfterDeposit = depositXrp(
      issuerKeyPair,
      traderKeyPair,
      traderAccount,
      amm,
      signatureService,
      feeResult
    );

    TradingFee newTradingFee = TradingFee.ofPercent(BigDecimal.valueOf(0.24));
    AmmVote ammVote = AmmVote.builder()
      .account(traderAccount.accountData().account())
      .sequence(traderAccountAfterDeposit.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(traderAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .signingPublicKey(traderKeyPair.publicKey())
      .asset2(
        IouIssue.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
      )
      .asset(Issue.XRP)
      .tradingFee(newTradingFee)
      .build();

    SingleSignedTransaction<AmmVote> signedVote = signatureService.sign(traderKeyPair.privateKey(), ammVote);

    SubmitResult<AmmVote> voteSubmitResult = xrplClient.submit(signedVote);
    assertThat(voteSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedVote.hash(),
      traderAccount.ledgerIndexSafe(),
      ammVote.lastLedgerSequence().get(),
      ammVote.sequence(),
      traderKeyPair.publicKey().deriveAddress()
    );

    BigDecimal issuerLpTokenBalance = new BigDecimal(xrplClient.accountLines(
        AccountLinesRequestParams.builder()
          .account(issuerKeyPair.publicKey().deriveAddress())
          .peer(amm.amm().account())
          .ledgerSpecifier(LedgerSpecifier.CURRENT)
          .build()
      ).lines().stream()
      .filter(trustLine -> trustLine.currency().equals(amm.amm().lpToken().currency()))
      .findFirst()
      .orElseThrow(RuntimeException::new)
      .balance());

    BigDecimal traderLpTokenBalance = new BigDecimal(xrplClient.accountLines(
        AccountLinesRequestParams.builder()
          .account(traderKeyPair.publicKey().deriveAddress())
          .peer(amm.amm().account())
          .ledgerSpecifier(LedgerSpecifier.CURRENT)
          .build()
      ).lines().stream()
      .filter(trustLine -> trustLine.currency().equals(amm.amm().lpToken().currency()))
      .findFirst()
      .orElseThrow(RuntimeException::new)
      .balance());

    // Expected trading fee is the weighted average of each vote, where the weight is number of LP tokens held
    // by each voter
    TradingFee expectedTradingFee = TradingFee.ofPercent(
      issuerLpTokenBalance.multiply(amm.amm().tradingFee().bigDecimalValue()).add(
          traderLpTokenBalance.multiply(newTradingFee.bigDecimalValue())
        ).divide(issuerLpTokenBalance.add(traderLpTokenBalance), RoundingMode.HALF_UP)
        .setScale(3, RoundingMode.HALF_UP)
    );

    AmmInfoResult ammAfterVote = getAmmInfo(issuerKeyPair);
    assertThat(ammAfterVote.amm().tradingFee()).isEqualTo(expectedTradingFee);
  }

  @Test
  void depositAndBid() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();
    AmmInfoResult amm = createAmm(issuerKeyPair, feeResult);
    KeyPair traderKeyPair = createRandomAccountEd25519();
    KeyPair authAccount1 = createRandomAccountEd25519();

    AccountInfoResult traderAccount = scanForResult(
      () -> this.getValidatedAccountInfo(traderKeyPair.publicKey().deriveAddress())
    );

    AccountInfoResult traderAccountAfterDeposit = depositXrp(
      issuerKeyPair,
      traderKeyPair,
      traderAccount,
      amm,
      signatureService,
      feeResult
    );

    AmmBid bid = AmmBid.builder()
      .account(traderAccount.accountData().account())
      .sequence(traderAccountAfterDeposit.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(traderAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .signingPublicKey(traderKeyPair.publicKey())
      .asset2(
        IouIssue.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
      )
      .asset(Issue.XRP)
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(authAccount1.publicKey().deriveAddress()))
      )
      .bidMin(
        IssuedCurrencyAmount.builder()
          .from(amm.amm().lpToken())
          .value("100")
          .build()
      )
      .build();

    SingleSignedTransaction<AmmBid> signedBid = signatureService.sign(traderKeyPair.privateKey(), bid);

    SubmitResult<AmmBid> voteSubmitResult = xrplClient.submit(signedBid);
    assertThat(voteSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedBid.hash(),
      traderAccount.ledgerIndexSafe(),
      bid.lastLedgerSequence().get(),
      bid.sequence(),
      traderKeyPair.publicKey().deriveAddress()
    );

    AmmInfoResult ammAfterBid = getAmmInfo(issuerKeyPair);

    assertThat(ammAfterBid.amm().auctionSlot()).isNotEmpty();
    AmmInfoAuctionSlot auctionSlot = ammAfterBid.amm().auctionSlot().get();
    assertThat(auctionSlot.account()).isEqualTo(traderAccount.accountData().account());
    assertThat(auctionSlot.authAccounts()).asList().extracting("account")
      .containsExactly(authAccount1.publicKey().deriveAddress());
  }

  @Test
  void depositAndWithdraw() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();
    AmmInfoResult amm = createAmm(issuerKeyPair, feeResult);
    KeyPair traderKeyPair = createRandomAccountEd25519();

    AccountInfoResult traderAccount = scanForResult(
      () -> this.getValidatedAccountInfo(traderKeyPair.publicKey().deriveAddress())
    );

    AccountInfoResult traderAccountAfterDeposit = depositXrp(
      issuerKeyPair,
      traderKeyPair,
      traderAccount,
      amm,
      signatureService,
      feeResult
    );

    AmmInfoResult ammInfoAfterDeposit = getAmmInfo(issuerKeyPair);
    AmmWithdraw withdraw = AmmWithdraw.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccountAfterDeposit.accountData().sequence())
      .lastLedgerSequence(
        traderAccountAfterDeposit.ledgerCurrentIndexSafe()
          .plus(UnsignedInteger.valueOf(4000))
          .unsignedIntegerValue()
      )
      .signingPublicKey(traderKeyPair.publicKey())
      .asset2(
        IouIssue.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
      )
      .asset(Issue.XRP)
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(9)))
      .flags(AmmWithdrawFlags.SINGLE_ASSET)
      .build();

    SingleSignedTransaction<AmmWithdraw> signedWithdraw = signatureService.sign(traderKeyPair.privateKey(), withdraw);

    SubmitResult<AmmWithdraw> voteSubmitResult = xrplClient.submit(signedWithdraw);
    assertThat(voteSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedWithdraw.hash(),
      traderAccount.ledgerIndexSafe(),
      withdraw.lastLedgerSequence().get(),
      withdraw.sequence(),
      traderKeyPair.publicKey().deriveAddress()
    );

    AmmInfoResult ammAfterWithdraw = getAmmInfo(issuerKeyPair);
    assertThat(ammAfterWithdraw.amm().amount()).isInstanceOf(XrpCurrencyAmount.class)
      .isEqualTo(((XrpCurrencyAmount) ammInfoAfterDeposit.amm().amount())
        .minus((XrpCurrencyAmount) withdraw.amount().get()));

    AccountInfoResult traderAccountAfterWithdraw = xrplClient.accountInfo(
      AccountInfoRequestParams.of(traderKeyPair.publicKey().deriveAddress())
    );

    assertThat(traderAccountAfterWithdraw.accountData().balance()).isEqualTo(
      traderAccountAfterDeposit.accountData().balance()
        .minus(withdraw.fee())
        .plus((XrpCurrencyAmount) withdraw.amount().get())
    );
  }

  @Test
  void depositAndClawbackTwoAssets() throws JsonRpcClientErrorException, JsonProcessingException {
    // create issuer and trader wallets
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair traderKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccount = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult traderAccount = scanForResult(
      () -> this.getValidatedAccountInfo(traderKeyPair.publicKey().deriveAddress())
    );

    // enable rippling
    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence(), feeResult, AccountSetFlag.DEFAULT_RIPPLE);

    // enable Allow Trustline Clawback
    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence().plus(UnsignedInteger.ONE),
      feeResult, AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK);

    // create Trustline from issuer to trader for currency USD and define two currencies
    TrustSet trustSet = TrustSet.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence())
      .signingPublicKey(traderKeyPair.publicKey())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(usd)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build()
      )
      .flags(TrustSetFlags.builder().tfClearNoRipple().build())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustline =
      signatureService.sign(traderKeyPair.privateKey(), trustSet);
    SubmitResult<TrustSet> trustlineSubmitResult = xrplClient.submit(signedTrustline);
    assertThat(trustlineSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create Trustline from issuer to trader for xrpl4j coin
    TrustSet trustSet2 = TrustSet.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(traderKeyPair.publicKey())
      .limitAmount(
        IssuedCurrencyAmount.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("1000000")
          .build()
      )
      .flags(TrustSetFlags.builder().tfClearNoRipple().build())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustline2 =
      signatureService.sign(traderKeyPair.privateKey(), trustSet2);
    SubmitResult<TrustSet> trustlineSubmitResult2 = xrplClient.submit(signedTrustline2);
    assertThat(trustlineSubmitResult2.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // send payment of xrpl4jcoin to trader wallet from issuer
    Payment paymentXrpl4jCoin = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(traderKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .currency(xrpl4jCoin)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("500")
        .build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentXrpl4jCoin =
      signatureService.sign(issuerKeyPair.privateKey(), paymentXrpl4jCoin);
    SubmitResult<Payment> paymentXrpl4jCoinSubmitResult = xrplClient.submit(signedPaymentXrpl4jCoin);
    assertThat(paymentXrpl4jCoinSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // send payment of USD to trader wallet from issuer
    Payment paymentUsd = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(traderKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .currency(usd)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(3)))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentUsd = signatureService.sign(issuerKeyPair.privateKey(), paymentUsd);
    SubmitResult<Payment> paymentUsdSubmitResult = xrplClient.submit(signedPaymentUsd);
    assertThat(paymentUsdSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create AMM from issuer account
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );
    AmmCreate ammCreate = AmmCreate.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .fee(reserveAmount)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(xrpl4jCoin)
          .value("100")
          .build()
      )
      .amount2(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("250")
          .build()
      )
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(issuerAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(100)).unsignedIntegerValue())
      .signingPublicKey(traderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedCreate = signatureService.sign(traderKeyPair.privateKey(), ammCreate);
    SubmitResult<AmmCreate> submitResult = xrplClient.submit(signedCreate);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create variables for issued currencies
    Issue testCurrencyIssue = IouIssue.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(usd)
      .build();
    Issue xrpl4jCoinIssue = IouIssue.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(xrpl4jCoin)
      .build();

    // deposit assets into AMM pool
    AmmDeposit ammDeposit = AmmDeposit.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.valueOf(3)))
      .asset(testCurrencyIssue)
      .asset2(xrpl4jCoinIssue)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("10")
          .build()
      )
      .flags(AmmDepositFlags.SINGLE_ASSET)
      .signingPublicKey(traderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmDeposit> signedAmmDeposit =
      signatureService.sign(traderKeyPair.privateKey(), ammDeposit);
    SubmitResult<AmmDeposit> ammDepositResult = xrplClient.submit(signedAmmDeposit);
    assertThat(ammDepositResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create AMMClawback
    AmmClawback ammClawback = AmmClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .holder(traderKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(20))
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(4)))
      .asset(testCurrencyIssue)
      .asset2(xrpl4jCoinIssue)
      .flags(AmmClawbackFlags.CLAW_TWO_ASSETS)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmClawback> signedAmmClawback =
      signatureService.sign(issuerKeyPair.privateKey(), ammClawback);
    SubmitResult<AmmClawback> ammClawbackResult = xrplClient.submit(signedAmmClawback);
    assertThat(ammClawbackResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }

  @Test
  void depositAndClawbackWithAmount() throws JsonRpcClientErrorException, JsonProcessingException {
    // create issuer and trader wallets
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair traderKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccount = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult traderAccount = scanForResult(
      () -> this.getValidatedAccountInfo(traderKeyPair.publicKey().deriveAddress())
    );

    // enable rippling
    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence(), feeResult, AccountSetFlag.DEFAULT_RIPPLE);

    // enable Allow Trustline Clawback
    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence().plus(UnsignedInteger.ONE),
      feeResult, AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK);

    // create Trustline from issuer to trader for currency USD and define two currencies
    TrustSet trustSet = TrustSet.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence())
      .signingPublicKey(traderKeyPair.publicKey())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(usd)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build()
      )
      .flags(TrustSetFlags.builder().tfClearNoRipple().build())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustline =
      signatureService.sign(traderKeyPair.privateKey(), trustSet);
    SubmitResult<TrustSet> trustlineSubmitResult = xrplClient.submit(signedTrustline);
    assertThat(trustlineSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create Trustline from issuer to trader for xrpl4jcoin
    TrustSet trustSet2 = TrustSet.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(traderKeyPair.publicKey())
      .limitAmount(
        IssuedCurrencyAmount.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("1000000")
          .build()
      )
      .flags(TrustSetFlags.builder().tfClearNoRipple().build())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustline2 =
      signatureService.sign(traderKeyPair.privateKey(), trustSet2);
    SubmitResult<TrustSet> trustlineSubmitResult2 = xrplClient.submit(signedTrustline2);
    assertThat(trustlineSubmitResult2.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // send payment of xrpl4jcoin to trader wallet from issuer
    Payment paymentXrpl4jCoin = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(traderKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .currency(xrpl4jCoin)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("500")
        .build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentXrpl4jCoin =
      signatureService.sign(issuerKeyPair.privateKey(), paymentXrpl4jCoin);
    SubmitResult<Payment> paymentXrpl4jCoinSubmitResult = xrplClient.submit(signedPaymentXrpl4jCoin);
    assertThat(paymentXrpl4jCoinSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // send payment of USD to trader wallet from issuer
    Payment paymentUsd = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(traderKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .currency(usd)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(3)))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentUsd = signatureService.sign(issuerKeyPair.privateKey(), paymentUsd);
    SubmitResult<Payment> paymentUsdSubmitResult = xrplClient.submit(signedPaymentUsd);
    assertThat(paymentUsdSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create AMM from issuer account
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );
    AmmCreate ammCreate = AmmCreate.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .fee(reserveAmount)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(xrpl4jCoin)
          .value("100")
          .build()
      )
      .amount2(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("250")
          .build()
      )
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(issuerAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(100)).unsignedIntegerValue())
      .signingPublicKey(traderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedCreate = signatureService.sign(traderKeyPair.privateKey(), ammCreate);
    SubmitResult<AmmCreate> submitResult = xrplClient.submit(signedCreate);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create variables for issued currencies
    Issue testCurrencyIssue = IouIssue.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(usd)
      .build();
    Issue xrpl4jCoinIssue = IouIssue.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(xrpl4jCoin)
      .build();

    // deposit assets into AMM pool
    AmmDeposit ammDeposit = AmmDeposit.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.valueOf(3)))
      .asset(testCurrencyIssue)
      .asset2(xrpl4jCoinIssue)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("10")
          .build()
      )
      .flags(AmmDepositFlags.SINGLE_ASSET)
      .signingPublicKey(traderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmDeposit> signedAmmDeposit =
      signatureService.sign(traderKeyPair.privateKey(), ammDeposit);
    SubmitResult<AmmDeposit> ammDepositResult = xrplClient.submit(signedAmmDeposit);
    assertThat(ammDepositResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create AMMClawback
    AmmClawback ammClawback = AmmClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .holder(traderKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(20))
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(4)))
      .asset(testCurrencyIssue)
      .asset2(xrpl4jCoinIssue)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("5")
          .build()
      )
      .flags(AmmClawbackFlags.UNSET)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmClawback> signedAmmClawback =
      signatureService.sign(issuerKeyPair.privateKey(), ammClawback);
    SubmitResult<AmmClawback> ammClawbackResult = xrplClient.submit(signedAmmClawback);
    assertThat(ammClawbackResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }

  @Test
  void depositAndClawbackSingleAsset() throws JsonRpcClientErrorException, JsonProcessingException {
    // create issuer and trader wallets
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair traderKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccount = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult traderAccount = scanForResult(
      () -> this.getValidatedAccountInfo(traderKeyPair.publicKey().deriveAddress())
    );

    // enable rippling
    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence(), feeResult, AccountSetFlag.DEFAULT_RIPPLE);

    // enable Allow Trustline Clawback
    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence().plus(UnsignedInteger.ONE),
      feeResult, AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK);

    // create Trustline from issuer to trader for currency USD
    TrustSet trustSet = TrustSet.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence())
      .signingPublicKey(traderKeyPair.publicKey())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(usd)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build()
      )
      .flags(TrustSetFlags.builder().tfClearNoRipple().build())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustline =
      signatureService.sign(traderKeyPair.privateKey(), trustSet);
    SubmitResult<TrustSet> trustlineSubmitResult = xrplClient.submit(signedTrustline);
    assertThat(trustlineSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create Trustline from issuer to trader for xrpl4jcoin
    TrustSet trustSet2 = TrustSet.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(traderKeyPair.publicKey())
      .limitAmount(
        IssuedCurrencyAmount.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("1000000")
          .build()
      )
      .flags(TrustSetFlags.builder().tfClearNoRipple().build())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustline2 =
      signatureService.sign(traderKeyPair.privateKey(), trustSet2);
    SubmitResult<TrustSet> trustlineSubmitResult2 = xrplClient.submit(signedTrustline2);
    assertThat(trustlineSubmitResult2.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // send payment of xrpl4jCoin to trader wallet from issuer
    Payment paymentXrpl4jCoin = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(traderKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .currency(xrpl4jCoin)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("500")
        .build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentXrpl4jCoin =
      signatureService.sign(issuerKeyPair.privateKey(), paymentXrpl4jCoin);
    SubmitResult<Payment> paymentXrpl4jCoinSubmitResult = xrplClient.submit(signedPaymentXrpl4jCoin);
    assertThat(paymentXrpl4jCoinSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // send payment of USD to trader wallet from issuer
    Payment paymentUsd = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(traderKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .currency(usd)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(3)))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentUsd = signatureService.sign(issuerKeyPair.privateKey(), paymentUsd);
    SubmitResult<Payment> paymentUsdSubmitResult = xrplClient.submit(signedPaymentUsd);
    assertThat(paymentUsdSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create AMM from issuer account
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );
    AmmCreate ammCreate = AmmCreate.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .fee(reserveAmount)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(xrpl4jCoin)
          .value("100")
          .build()
      )
      .amount2(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("250")
          .build()
      )
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(issuerAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(100)).unsignedIntegerValue())
      .signingPublicKey(traderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedCreate = signatureService.sign(traderKeyPair.privateKey(), ammCreate);
    SubmitResult<AmmCreate> submitResult = xrplClient.submit(signedCreate);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create variables for issued currencies
    Issue testCurrencyIssue = IouIssue.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(usd)
      .build();
    Issue xrpl4jCoinIssue = IouIssue.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(xrpl4jCoin)
      .build();

    // deposit assets into AMM pool
    AmmDeposit ammDeposit = AmmDeposit.builder()
      .account(traderKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(traderAccount.accountData().sequence().plus(UnsignedInteger.valueOf(3)))
      .asset(testCurrencyIssue)
      .asset2(xrpl4jCoinIssue)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(usd)
          .value("10")
          .build()
      )
      .flags(AmmDepositFlags.SINGLE_ASSET)
      .signingPublicKey(traderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmDeposit> signedAmmDeposit =
      signatureService.sign(traderKeyPair.privateKey(), ammDeposit);
    SubmitResult<AmmDeposit> ammDepositResult = xrplClient.submit(signedAmmDeposit);
    assertThat(ammDepositResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // create AMMClawback
    AmmClawback ammClawback = AmmClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .holder(traderKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(20))
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.valueOf(4)))
      .asset(testCurrencyIssue)
      .asset2(xrpl4jCoinIssue)
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(AmmClawbackFlags.UNSET)
      .build();

    SingleSignedTransaction<AmmClawback> signedAmmClawback =
      signatureService.sign(issuerKeyPair.privateKey(), ammClawback);
    SubmitResult<AmmClawback> ammClawbackResult = xrplClient.submit(signedAmmClawback);
    assertThat(ammClawbackResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }

  private AccountInfoResult depositXrp(
    KeyPair issuerKeyPair,
    KeyPair traderKeyPair,
    AccountInfoResult traderAccount,
    AmmInfoResult amm,
    SignatureService<PrivateKey> signatureService,
    FeeResult feeResult
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    XrpCurrencyAmount depositAmount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10));
    AmmDeposit deposit = AmmDeposit.builder()
      .account(traderAccount.accountData().account())
      .asset2(
        IouIssue.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
      )
      .asset(Issue.XRP)
      .flags(AmmDepositFlags.SINGLE_ASSET)
      .amount(depositAmount)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence())
      .signingPublicKey(traderKeyPair.publicKey())
      .lastLedgerSequence(traderAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .build();

    SingleSignedTransaction<AmmDeposit> signedDeposit = signatureService.sign(traderKeyPair.privateKey(), deposit);
    SubmitResult<AmmDeposit> submitResult = xrplClient.submit(signedDeposit);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedDeposit.hash(),
      traderAccount.ledgerIndexSafe(),
      deposit.lastLedgerSequence().get(),
      deposit.sequence(),
      traderKeyPair.publicKey().deriveAddress()
    );

    AccountInfoResult traderAccountAfterDeposit = xrplClient.accountInfo(
      AccountInfoRequestParams.of(traderAccount.accountData().account())
    );

    assertThat(traderAccountAfterDeposit.accountData().balance())
      .isEqualTo(traderAccount.accountData().balance().minus(deposit.fee()).minus(depositAmount));

    AccountLinesResult traderLines = xrplClient.accountLines(
      AccountLinesRequestParams.builder()
        .account(traderAccount.accountData().account())
        .peer(amm.amm().account())
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(traderLines.lines()).asList().hasSize(1);
    TrustLine lpLine = traderLines.lines().get(0);
    assertThat(lpLine.currency()).isEqualTo(amm.amm().lpToken().currency());
    assertThat(new BigDecimal(lpLine.balance())).isGreaterThan(BigDecimal.ZERO);

    return traderAccountAfterDeposit;
  }

  private AmmInfoResult createAmm(
    KeyPair issuerKeyPair,
    FeeResult feeResult
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccount = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    enableFlag(issuerKeyPair, issuerAccount.accountData().sequence(), feeResult, AccountSetFlag.DEFAULT_RIPPLE);

    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );
    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccount.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(reserveAmount)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(xrpl4jCoin)
          .value("2.5")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10)))
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(issuerAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedCreate = signatureService.sign(issuerKeyPair.privateKey(), ammCreate);
    SubmitResult<AmmCreate> submitResult = xrplClient.submit(signedCreate);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedCreate.hash(),
      issuerAccount.ledgerIndexSafe(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    return getAmmInfo(issuerKeyPair);
  }

  private AmmInfoResult getAmmInfo(KeyPair issuerKeyPair) throws JsonRpcClientErrorException {
    AmmInfoResult ammInfoResult = xrplClient.ammInfo(
      AmmInfoRequestParams.from(
        Issue.XRP,
        IouIssue.builder()
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .currency(xrpl4jCoin)
          .build()
      ));

    AccountInfoResult ammAccountInfo = xrplClient.accountInfo(
      AccountInfoRequestParams.of(ammInfoResult.amm().account())
    );

    assertThat(ammAccountInfo.accountData().ammId()).isNotEmpty();

    AmmInfoResult ammInfoByAccount = xrplClient.ammInfo(
      AmmInfoRequestParams.from(ammAccountInfo.accountData().account())
    );

    assertThat(ammInfoByAccount.amm()).isEqualTo(ammInfoResult.amm());

    LedgerEntryResult<AmmObject> ammObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.amm(
        AmmLedgerEntryParams.builder()
          .asset(Issue.XRP)
          .asset2(
            IouIssue.builder()
              .issuer(issuerKeyPair.publicKey().deriveAddress())
              .currency(xrpl4jCoin)
              .build()
          )
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(ammObject.node().account()).isEqualTo(ammInfoByAccount.amm().account());
    assertThat(ammObject.node().asset()).isEqualTo(Issue.XRP);
    assertThat(ammObject.node().asset2()).isEqualTo(
      IouIssue.builder()
        .issuer(((IssuedCurrencyAmount) ammInfoByAccount.amm().amount2()).issuer())
        .currency(((IssuedCurrencyAmount) ammInfoByAccount.amm().amount2()).currency())
        .build()
    );
    assertThat(
      (ammObject.node().auctionSlot().isPresent() && ammInfoByAccount.amm().auctionSlot().isPresent()) ||
        (!ammObject.node().auctionSlot().isPresent() && !ammInfoByAccount.amm().auctionSlot().isPresent())
    ).isTrue();
    if (ammObject.node().auctionSlot().isPresent()) {
      AuctionSlot entryAuctionSlot = ammObject.node().auctionSlot().get();
      AmmInfoAuctionSlot infoAuctionSlot = ammInfoByAccount.amm().auctionSlot().get();
      assertThat(entryAuctionSlot.account()).isEqualTo(infoAuctionSlot.account());
      assertThat(entryAuctionSlot.authAccountsAddresses()).isEqualTo(infoAuctionSlot.authAccounts().stream().map(
        AmmInfoAuthAccount::account).collect(Collectors.toList()));
      assertThat(entryAuctionSlot.price()).isEqualTo(infoAuctionSlot.price());
      assertThat(TimeUtils.xrplTimeToZonedDateTime(UnsignedLong.valueOf(entryAuctionSlot.expiration().longValue())))
        .isEqualTo(infoAuctionSlot.expiration());
      assertThat(entryAuctionSlot.discountedFee()).isEqualTo(infoAuctionSlot.discountedFee());
    }

    assertThat(ammObject.node().lpTokenBalance()).isEqualTo(ammInfoByAccount.amm().lpToken());
    assertThat(ammObject.node().tradingFee()).isEqualTo(ammInfoByAccount.amm().tradingFee());

    assertThat(ammObject.node().voteSlots().size()).isEqualTo(ammInfoByAccount.amm().voteSlots().size());

    LedgerEntryResult<AmmObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(ammObject.index(), AmmObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(ammObject.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(ammObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());

    return ammInfoResult;
  }

  // =============================================
  // MPT AMM Integration Tests
  // =============================================

  /**
   * Creates an MPT issuance, authorizes a holder, mints tokens to the holder, then creates an AMM with
   * MPT/XRP as the asset pair. Verifies the AMM via {@code ammInfo} RPC and {@link AmmLedgerEntryParams}.
   */
  @Test
  void mptAmmCreateAndVerifyWithAmmInfoAndLedgerEntry() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance with tfMptCanTrade to allow AMM usage
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Get the MPT issuance ID from transaction metadata
    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // The issuer itself needs to hold MPT to seed the AMM, so authorize issuer as holder too
    // (actually the issuer is the minter - they can deposit directly from issuance)
    // Create AMM with MPT as Amount and XRP as Amount2
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );

    MptCurrencyAmount mptAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("1000")
      .build();

    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuanceCreate.sequence().plus(UnsignedInteger.ONE))
      .fee(reserveAmount)
      .amount(mptAmount)
      .amount2(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10)))
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(
        issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedAmmCreate = signatureService.sign(issuerKeyPair.privateKey(), ammCreate);
    SubmitResult<AmmCreate> ammCreateResult = xrplClient.submit(signedAmmCreate);
    assertThat(ammCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAmmCreate.hash(),
      ammCreateResult.validatedLedgerIndex(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Verify AMM via ammInfo RPC by asset pair
    AmmInfoResult ammInfoByAssets = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), Issue.XRP)
    );

    assertThat(ammInfoByAssets.amm().account()).isNotNull();
    assertThat(ammInfoByAssets.amm().amount()).isInstanceOf(MptCurrencyAmount.class);
    assertThat(((MptCurrencyAmount) ammInfoByAssets.amm().amount()).mptIssuanceId()).isEqualTo(mptIssuanceId);
    assertThat(ammInfoByAssets.amm().amount2()).isInstanceOf(XrpCurrencyAmount.class);

    // Verify ammInfo by AMM account address
    AmmInfoResult ammInfoByAccount = xrplClient.ammInfo(
      AmmInfoRequestParams.from(ammInfoByAssets.amm().account())
    );
    assertThat(ammInfoByAccount.amm()).isEqualTo(ammInfoByAssets.amm());

    // Verify AMM ledger entry via AmmLedgerEntryParams with MPT asset
    LedgerEntryResult<AmmObject> ammObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.amm(
        AmmLedgerEntryParams.builder()
          .asset(MptIssue.of(mptIssuanceId))
          .asset2(Issue.XRP)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(ammObject.node().account()).isEqualTo(ammInfoByAccount.amm().account());
    assertThat(ammObject.node().asset()).isEqualTo(MptIssue.of(mptIssuanceId));
    assertThat(ammObject.node().asset2()).isEqualTo(Issue.XRP);
    assertThat(ammObject.node().lpTokenBalance()).isEqualTo(ammInfoByAccount.amm().lpToken());
    assertThat(ammObject.node().tradingFee()).isEqualTo(ammInfoByAccount.amm().tradingFee());

    // Verify ledger entry by index
    LedgerEntryResult<AmmObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(ammObject.index(), AmmObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndex.node()).isEqualTo(ammObject.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(ammObject.index(), LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }

  /**
   * Creates an MPT/XRP AMM, then has a second holder deposit MPT into the pool using
   * {@link AmmDeposit} with MPT amounts, and withdraw XRP from the pool using {@link AmmWithdraw}.
   * Verifies the AMM state after deposit and withdrawal via {@code ammInfo}.
   */
  @Test
  void mptAmmDepositAndWithdraw() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair holderKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult holderAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Authorize holder to hold this MPT
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(holderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAuthorize.hash(),
      authorizeResult.validatedLedgerIndex(),
      authorize.lastLedgerSequence().get(),
      authorize.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Get updated issuer account info before mint
    AccountInfoResult issuerAccountInfoBeforeMint = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Mint MPT tokens to holder via Payment from issuer
    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("100000")
      .build();
    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfoBeforeMint.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerAccountInfoBeforeMint.ledgerIndexSafe().plus(UnsignedInteger.valueOf(1000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedMint.hash(),
      mintResult.validatedLedgerIndex(),
      mint.lastLedgerSequence().get(),
      mint.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Get updated account info after mint
    AccountInfoResult issuerAccountInfoAfterMint = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Issuer creates AMM with MPT/XRP pair
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );

    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfoAfterMint.accountData().sequence())
      .fee(reserveAmount)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("5000").build())
      .amount2(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10)))
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(
        issuerAccountInfoAfterMint.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedAmmCreate = signatureService.sign(issuerKeyPair.privateKey(), ammCreate);
    SubmitResult<AmmCreate> ammCreateResult = xrplClient.submit(signedAmmCreate);
    assertThat(ammCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAmmCreate.hash(),
      ammCreateResult.validatedLedgerIndex(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );
    logger.info("AmmCreate finalized: hash={}", signedAmmCreate.hash());

    AmmInfoResult ammInfo = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), Issue.XRP)
    );
    logger.info("AmmInfo retrieved: account={}", ammInfo.amm().account());
    assertThat(ammInfo.amm().amount()).isInstanceOf(MptCurrencyAmount.class);

    // Verify AMM exists by querying it again before deposit
    AmmInfoResult ammInfoBeforeDeposit = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), Issue.XRP)
    );
    logger.info("AmmInfo before deposit: account={}, amount={}, amount2={}",
      ammInfoBeforeDeposit.amm().account(),
      ammInfoBeforeDeposit.amm().amount(),
      ammInfoBeforeDeposit.amm().amount2());

    // Holder deposits MPT into the MPT/XRP AMM (single-asset deposit)
    AccountInfoResult holderInfoBeforeDeposit = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount depositMptAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("5000")  // Deposit 5000 MPT tokens
      .build();
    AmmDeposit deposit = AmmDeposit.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .asset(MptIssue.of(mptIssuanceId))
      .asset2(Issue.XRP)
      .amount(depositMptAmount)  // Depositing MPT, not XRP
      .flags(AmmDepositFlags.SINGLE_ASSET)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderInfoBeforeDeposit.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderInfoBeforeDeposit.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<AmmDeposit> signedDeposit = signatureService.sign(holderKeyPair.privateKey(), deposit);
    SubmitResult<AmmDeposit> depositResult = xrplClient.submit(signedDeposit);
    assertThat(depositResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("MPT deposit successful! Deposited {} MPT tokens into AMM", depositMptAmount.value());

    scanForFinality(
      signedDeposit.hash(),
      depositResult.validatedLedgerIndex(),
      deposit.lastLedgerSequence().get(),
      deposit.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Verify AMM state reflects the deposit - MPT amount should have increased
    AmmInfoResult ammInfoAfterDeposit = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), Issue.XRP)
    );
    assertThat(ammInfoAfterDeposit.amm().amount()).isInstanceOf(MptCurrencyAmount.class);
    UnsignedLong mptAmountBefore = UnsignedLong.valueOf(((MptCurrencyAmount) ammInfo.amm().amount()).value());
    UnsignedLong mptAmountAfter =
      UnsignedLong.valueOf(((MptCurrencyAmount) ammInfoAfterDeposit.amm().amount()).value());
    assertThat(mptAmountAfter).isGreaterThan(mptAmountBefore);

    // Holder withdraws XRP from the AMM (single-asset withdraw)
    AccountInfoResult holderInfoAfterDeposit = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    AmmWithdraw withdraw = AmmWithdraw.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .asset(MptIssue.of(mptIssuanceId))
      .asset2(Issue.XRP)
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2)))
      .flags(AmmWithdrawFlags.SINGLE_ASSET)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderInfoAfterDeposit.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderInfoAfterDeposit.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<AmmWithdraw> signedWithdraw = signatureService.sign(holderKeyPair.privateKey(), withdraw);
    SubmitResult<AmmWithdraw> withdrawResult = xrplClient.submit(signedWithdraw);
    assertThat(withdrawResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedWithdraw.hash(),
      withdrawResult.validatedLedgerIndex(),
      withdraw.lastLedgerSequence().get(),
      withdraw.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Verify AMM XRP amount decreased after withdrawal
    AmmInfoResult ammInfoAfterWithdraw = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), Issue.XRP)
    );
    assertThat(ammInfoAfterWithdraw.amm().amount2()).isInstanceOf(XrpCurrencyAmount.class);
    assertThat((XrpCurrencyAmount) ammInfoAfterWithdraw.amm().amount2())
      .isLessThan((XrpCurrencyAmount) ammInfoAfterDeposit.amm().amount2());
  }

  /**
   * Tests AMM clawback with MPT/XRP pool.
   * Creates an MPT/XRP AMM, trader deposits MPT, then issuer claws back MPT from the AMM.
   */
  @Test
  void mptAmmClawback() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair holderKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    final AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    final AccountInfoResult holderAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    // Enable AllowTrustLineClawback flag on issuer account
    AccountSet accountSet = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .sequence(issuerAccountInfo.accountData().sequence())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK)
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      issuerKeyPair.privateKey(), accountSet
    );
    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(signedAccountSet);
    assertThat(accountSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAccountSet.hash(),
      accountSetResult.validatedLedgerIndex(),
      accountSet.lastLedgerSequence().get(),
      accountSet.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Create MPT issuance with clawback enabled
    AccountInfoResult issuerInfoAfterAccountSet = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerInfoAfterAccountSet.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        issuerInfoAfterAccountSet.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .tfMptCanClawback(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Authorize holder
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(holderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAuthorize.hash(),
      authorizeResult.validatedLedgerIndex(),
      authorize.lastLedgerSequence().get(),
      authorize.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Mint MPT to holder
    AccountInfoResult issuerInfoBeforeMint = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("100000")
      .build();

    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .sequence(issuerInfoBeforeMint.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerInfoBeforeMint.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(
      issuerKeyPair.privateKey(), mint
    );
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedMint.hash(),
      mintResult.validatedLedgerIndex(),
      mint.lastLedgerSequence().get(),
      mint.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Get updated account info after mint
    AccountInfoResult issuerInfoAfterMint = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Create AMM with MPT/XRP
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );

    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerInfoAfterMint.accountData().sequence())
      .fee(reserveAmount)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("10000").build())
      .amount2(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(100)))
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(
        issuerInfoAfterMint.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedAmmCreate = signatureService.sign(
      issuerKeyPair.privateKey(), ammCreate
    );
    SubmitResult<AmmCreate> ammCreateResult = xrplClient.submit(signedAmmCreate);
    assertThat(ammCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAmmCreate.hash(),
      ammCreateResult.validatedLedgerIndex(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Holder deposits MPT into AMM
    AccountInfoResult holderInfoBeforeDeposit = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    AmmDeposit deposit = AmmDeposit.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .asset(MptIssue.of(mptIssuanceId))
      .asset2(Issue.XRP)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("5000").build())
      .flags(AmmDepositFlags.SINGLE_ASSET)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderInfoBeforeDeposit.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderInfoBeforeDeposit.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<AmmDeposit> signedDeposit = signatureService.sign(
      holderKeyPair.privateKey(), deposit
    );
    SubmitResult<AmmDeposit> depositResult = xrplClient.submit(signedDeposit);
    assertThat(depositResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedDeposit.hash(),
      depositResult.validatedLedgerIndex(),
      deposit.lastLedgerSequence().get(),
      deposit.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Issuer claws back MPT from AMM
    AccountInfoResult issuerInfoBeforeClawback = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AmmClawback clawback = AmmClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .holder(holderKeyPair.publicKey().deriveAddress())
      .asset(MptIssue.of(mptIssuanceId))
      .asset2(Issue.XRP)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("1000").build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerInfoBeforeClawback.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerInfoBeforeClawback.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<AmmClawback> signedClawback = signatureService.sign(
      issuerKeyPair.privateKey(), clawback
    );
    SubmitResult<AmmClawback> clawbackResult = xrplClient.submit(signedClawback);
    assertThat(clawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedClawback.hash(),
      clawbackResult.validatedLedgerIndex(),
      clawback.lastLedgerSequence().get(),
      clawback.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Verify AMM MPT amount decreased after clawback
    AmmInfoResult ammAfterClawback = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), Issue.XRP)
    );

    assertThat(ammAfterClawback.amm().amount()).isInstanceOf(MptCurrencyAmount.class);
    MptCurrencyAmount ammMptAmount = (MptCurrencyAmount) ammAfterClawback.amm().amount();
    // The AMM should have less MPT after clawback
    assertThat(new BigDecimal(ammMptAmount.value())).isLessThan(new BigDecimal("15000"));

    logger.info("Successfully clawed back MPT from MPT/XRP AMM");
  }

  /**
   * Tests AMM ledger entry and ammInfo with MPT/MPT asset pair (two different MPTs).
   * Creates two MPT issuances, creates an AMM with MPT1/MPT2, then verifies via ledgerEntry and ammInfo.
   */
  @Test
  void mptAmmLedgerEntryWithTwoMpts() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuer1KeyPair = createRandomAccountEd25519();
    KeyPair issuer2KeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuer1AccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuer1KeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult issuer2AccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuer2KeyPair.publicKey().deriveAddress())
    );

    // Create first MPT issuance
    MpTokenIssuanceCreate issuance1Create = MpTokenIssuanceCreate.builder()
      .account(issuer1KeyPair.publicKey().deriveAddress())
      .sequence(issuer1AccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuer1AccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuer1KeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuance1Create = signatureService.sign(
      issuer1KeyPair.privateKey(), issuance1Create
    );
    SubmitResult<MpTokenIssuanceCreate> issuance1CreateResult = xrplClient.submit(signedIssuance1Create);
    assertThat(issuance1CreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuance1Create.hash(),
      issuance1CreateResult.validatedLedgerIndex(),
      issuance1Create.lastLedgerSequence().get(),
      issuance1Create.sequence(),
      issuer1KeyPair.publicKey().deriveAddress()
    );

    final MpTokenIssuanceId mpt1IssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuance1Create.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Create second MPT issuance
    MpTokenIssuanceCreate issuance2Create = MpTokenIssuanceCreate.builder()
      .account(issuer2KeyPair.publicKey().deriveAddress())
      .sequence(issuer2AccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuer2AccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuer2KeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuance2Create = signatureService.sign(
      issuer2KeyPair.privateKey(), issuance2Create
    );
    SubmitResult<MpTokenIssuanceCreate> issuance2CreateResult = xrplClient.submit(signedIssuance2Create);
    assertThat(issuance2CreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuance2Create.hash(),
      issuance2CreateResult.validatedLedgerIndex(),
      issuance2Create.lastLedgerSequence().get(),
      issuance2Create.sequence(),
      issuer2KeyPair.publicKey().deriveAddress()
    );

    MpTokenIssuanceId mpt2IssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuance2Create.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Authorize issuer1 to hold MPT2 (from issuer2)
    AccountInfoResult issuer1InfoBeforeAuth = scanForResult(
      () -> this.getValidatedAccountInfo(issuer1KeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize authorizeMpt2 = MpTokenAuthorize.builder()
      .account(issuer1KeyPair.publicKey().deriveAddress())
      .sequence(issuer1InfoBeforeAuth.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuer1KeyPair.publicKey())
      .lastLedgerSequence(
        issuer1InfoBeforeAuth.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpt2IssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorizeMpt2 = signatureService.sign(
      issuer1KeyPair.privateKey(), authorizeMpt2
    );
    SubmitResult<MpTokenAuthorize> authorizeMpt2Result = xrplClient.submit(signedAuthorizeMpt2);
    assertThat(authorizeMpt2Result.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAuthorizeMpt2.hash(),
      authorizeMpt2Result.validatedLedgerIndex(),
      authorizeMpt2.lastLedgerSequence().get(),
      authorizeMpt2.sequence(),
      issuer1KeyPair.publicKey().deriveAddress()
    );

    // Mint MPT2 tokens to issuer1 from issuer2
    AccountInfoResult issuer2InfoBeforeMint = scanForResult(
      () -> this.getValidatedAccountInfo(issuer2KeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mpt2MintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpt2IssuanceId)
      .value("50000")
      .build();

    Payment mintMpt2 = Payment.builder()
      .account(issuer2KeyPair.publicKey().deriveAddress())
      .destination(issuer1KeyPair.publicKey().deriveAddress())
      .amount(mpt2MintAmount)
      .sequence(issuer2InfoBeforeMint.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuer2KeyPair.publicKey())
      .lastLedgerSequence(
        issuer2InfoBeforeMint.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMintMpt2 = signatureService.sign(
      issuer2KeyPair.privateKey(), mintMpt2
    );
    SubmitResult<Payment> mintMpt2Result = xrplClient.submit(signedMintMpt2);
    assertThat(mintMpt2Result.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedMintMpt2.hash(),
      mintMpt2Result.validatedLedgerIndex(),
      mintMpt2.lastLedgerSequence().get(),
      mintMpt2.sequence(),
      issuer2KeyPair.publicKey().deriveAddress()
    );

    // Create AMM with MPT1/MPT2 pair
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );

    AccountInfoResult issuer1InfoBeforeAmm = scanForResult(
      () -> this.getValidatedAccountInfo(issuer1KeyPair.publicKey().deriveAddress())
    );

    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuer1KeyPair.publicKey().deriveAddress())
      .sequence(issuer1InfoBeforeAmm.accountData().sequence())
      .fee(reserveAmount)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(mpt1IssuanceId).value("10000").build())
      .amount2(MptCurrencyAmount.builder().mptIssuanceId(mpt2IssuanceId).value("5000").build())
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(
        issuer1InfoBeforeAmm.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .signingPublicKey(issuer1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedAmmCreate = signatureService.sign(
      issuer1KeyPair.privateKey(), ammCreate
    );
    SubmitResult<AmmCreate> ammCreateResult = xrplClient.submit(signedAmmCreate);
    assertThat(ammCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAmmCreate.hash(),
      ammCreateResult.validatedLedgerIndex(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      issuer1KeyPair.publicKey().deriveAddress()
    );

    // Verify AMM via ammInfo with MPT1/MPT2 asset pair
    AmmInfoResult ammInfoByAssets = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mpt1IssuanceId), MptIssue.of(mpt2IssuanceId))
    );

    assertThat(ammInfoByAssets.amm().account()).isNotNull();
    assertThat(ammInfoByAssets.amm().amount()).isInstanceOf(MptCurrencyAmount.class);
    assertThat(ammInfoByAssets.amm().amount2()).isInstanceOf(MptCurrencyAmount.class);

    // Extract the actual MPT issuance IDs from the AMM info response
    // Note: rippled may normalize the asset order, so we need to check what order they're actually stored in
    MpTokenIssuanceId actualAsset1Id = ((MptCurrencyAmount) ammInfoByAssets.amm().amount()).mptIssuanceId();
    MpTokenIssuanceId actualAsset2Id = ((MptCurrencyAmount) ammInfoByAssets.amm().amount2()).mptIssuanceId();

    // Verify that both MPT issuance IDs are present (in either order)
    assertThat(actualAsset1Id).isIn(mpt1IssuanceId, mpt2IssuanceId);
    assertThat(actualAsset2Id).isIn(mpt1IssuanceId, mpt2IssuanceId);
    assertThat(actualAsset1Id).isNotEqualTo(actualAsset2Id);

    // Verify AMM ledger entry via AmmLedgerEntryParams with MPT1/MPT2 assets
    LedgerEntryResult<AmmObject> ammObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.amm(
        AmmLedgerEntryParams.builder()
          .asset(MptIssue.of(mpt1IssuanceId))
          .asset2(MptIssue.of(mpt2IssuanceId))
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(ammObject.node().account()).isEqualTo(ammInfoByAssets.amm().account());

    // Extract the actual asset order from ledgerEntry response
    // Note: ledgerEntry and ammInfo may return assets in different orders, so we need to verify both are present
    MpTokenIssuanceId ledgerEntryAsset1Id = ammObject.node().asset().map(
      xrpIssue -> {
        throw new RuntimeException("Expected MPT, got XRP");
      },
      iouIssue -> {
        throw new RuntimeException("Expected MPT, got IOU");
      },
      mptIssue -> mptIssue.mptIssuanceId()
    );

    MpTokenIssuanceId ledgerEntryAsset2Id = ammObject.node().asset2().map(
      xrpIssue -> {
        throw new RuntimeException("Expected MPT, got XRP");
      },
      iouIssue -> {
        throw new RuntimeException("Expected MPT, got IOU");
      },
      mptIssue -> mptIssue.mptIssuanceId()
    );

    // Verify both MPT issuance IDs are present in the ledger entry (order-agnostic)
    assertThat(ledgerEntryAsset1Id).isIn(mpt1IssuanceId, mpt2IssuanceId);
    assertThat(ledgerEntryAsset2Id).isIn(mpt1IssuanceId, mpt2IssuanceId);
    assertThat(ledgerEntryAsset1Id).isNotEqualTo(ledgerEntryAsset2Id);

    assertThat(ammObject.node().lpTokenBalance()).isEqualTo(ammInfoByAssets.amm().lpToken());
    assertThat(ammObject.node().tradingFee()).isEqualTo(ammInfoByAssets.amm().tradingFee());

    // Verify ledger entry by index (using 256-byte hash)
    LedgerEntryResult<AmmObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(ammObject.index(), AmmObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndex.node()).isEqualTo(ammObject.node());

    logger.info("Successfully verified AMM with MPT1/MPT2 pair via ammInfo and ledgerEntry");
  }

  /**
   * Tests AMM ammInfo and ledgerEntry with MPT/IOU asset pair.
   * Creates an MPT issuance and an IOU trust line, creates an AMM with MPT/IOU,
   * then verifies via ammInfo and ledgerEntry.
   */
  @Test
  void mptAmmInfoAndLedgerEntryWithMptAndIou() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair mptIssuerKeyPair = createRandomAccountEd25519();
    KeyPair iouIssuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult mptIssuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult iouIssuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(iouIssuerKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .sequence(mptIssuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        mptIssuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      mptIssuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      mptIssuerKeyPair.publicKey().deriveAddress()
    );

    final MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Enable DefaultRipple on IOU issuer account
    AccountInfoResult iouIssuerInfoBeforeAccountSet = scanForResult(
      () -> this.getValidatedAccountInfo(iouIssuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet iouIssuerAccountSet = AccountSet.builder()
      .account(iouIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(iouIssuerKeyPair.publicKey())
      .sequence(iouIssuerInfoBeforeAccountSet.accountData().sequence())
      .lastLedgerSequence(
        iouIssuerInfoBeforeAccountSet.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .setFlag(AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    SingleSignedTransaction<AccountSet> signedIouIssuerAccountSet = signatureService.sign(
      iouIssuerKeyPair.privateKey(), iouIssuerAccountSet
    );
    SubmitResult<AccountSet> iouIssuerAccountSetResult = xrplClient.submit(signedIouIssuerAccountSet);
    assertThat(iouIssuerAccountSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIouIssuerAccountSet.hash(),
      iouIssuerAccountSetResult.validatedLedgerIndex(),
      iouIssuerAccountSet.lastLedgerSequence().get(),
      iouIssuerAccountSet.sequence(),
      iouIssuerKeyPair.publicKey().deriveAddress()
    );

    // Create IOU trust line from MPT issuer to IOU issuer
    AccountInfoResult mptIssuerInfoBeforeTrust = scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    String iouCurrency = "USD";
    TrustSet trustSet = TrustSet.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerInfoBeforeTrust.accountData().sequence())
      .lastLedgerSequence(
        mptIssuerInfoBeforeTrust.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .limitAmount(IssuedCurrencyAmount.builder()
        .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
        .currency(iouCurrency)
        .value("100000")
        .build())
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(
      mptIssuerKeyPair.privateKey(), trustSet
    );
    SubmitResult<TrustSet> trustSetResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedTrustSet.hash(),
      trustSetResult.validatedLedgerIndex(),
      trustSet.lastLedgerSequence().get(),
      trustSet.sequence(),
      mptIssuerKeyPair.publicKey().deriveAddress()
    );

    // IOU issuer sends IOU to MPT issuer
    AccountInfoResult iouIssuerInfoBeforePayment = scanForResult(
      () -> this.getValidatedAccountInfo(iouIssuerKeyPair.publicKey().deriveAddress())
    );

    Payment iouPayment = Payment.builder()
      .account(iouIssuerKeyPair.publicKey().deriveAddress())
      .destination(mptIssuerKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
        .currency(iouCurrency)
        .value("10000")
        .build())
      .sequence(iouIssuerInfoBeforePayment.accountData().sequence())
      .lastLedgerSequence(
        iouIssuerInfoBeforePayment.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(iouIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedIouPayment = signatureService.sign(
      iouIssuerKeyPair.privateKey(), iouPayment
    );
    SubmitResult<Payment> iouPaymentResult = xrplClient.submit(signedIouPayment);
    assertThat(iouPaymentResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIouPayment.hash(),
      iouPaymentResult.validatedLedgerIndex(),
      iouPayment.lastLedgerSequence().get(),
      iouPayment.sequence(),
      iouIssuerKeyPair.publicKey().deriveAddress()
    );

    // Create AMM with MPT/IOU pair
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );

    AccountInfoResult mptIssuerInfoBeforeAmm = scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    AmmCreate ammCreate = AmmCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .sequence(mptIssuerInfoBeforeAmm.accountData().sequence())
      .fee(reserveAmount)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("5000").build())
      .amount2(IssuedCurrencyAmount.builder()
        .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
        .currency(iouCurrency)
        .value("1000")
        .build())
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(
        mptIssuerInfoBeforeAmm.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AmmCreate> signedAmmCreate = signatureService.sign(
      mptIssuerKeyPair.privateKey(), ammCreate
    );
    SubmitResult<AmmCreate> ammCreateResult = xrplClient.submit(signedAmmCreate);
    assertThat(ammCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAmmCreate.hash(),
      ammCreateResult.validatedLedgerIndex(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      mptIssuerKeyPair.publicKey().deriveAddress()
    );

    // Verify AMM via ammInfo with MPT/IOU asset pair
    IouIssue iouIssue = IouIssue.builder()
      .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
      .currency(iouCurrency)
      .build();

    AmmInfoResult ammInfoByAssets = xrplClient.ammInfo(
      AmmInfoRequestParams.from(MptIssue.of(mptIssuanceId), iouIssue)
    );

    assertThat(ammInfoByAssets.amm().account()).isNotNull();
    assertThat(ammInfoByAssets.amm().amount()).isInstanceOf(MptCurrencyAmount.class);
    assertThat(((MptCurrencyAmount) ammInfoByAssets.amm().amount()).mptIssuanceId()).isEqualTo(mptIssuanceId);
    assertThat(ammInfoByAssets.amm().amount2()).isInstanceOf(IssuedCurrencyAmount.class);
    assertThat(((IssuedCurrencyAmount) ammInfoByAssets.amm().amount2()).currency()).isEqualTo(iouCurrency);

    // Verify AMM ledger entry via AmmLedgerEntryParams with MPT/IOU assets
    LedgerEntryResult<AmmObject> ammObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.amm(
        AmmLedgerEntryParams.builder()
          .asset(MptIssue.of(mptIssuanceId))
          .asset2(iouIssue)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(ammObject.node().account()).isEqualTo(ammInfoByAssets.amm().account());
    assertThat(ammObject.node().asset()).isEqualTo(MptIssue.of(mptIssuanceId));
    assertThat(ammObject.node().asset2()).isEqualTo(iouIssue);
    assertThat(ammObject.node().lpTokenBalance()).isEqualTo(ammInfoByAssets.amm().lpToken());
    assertThat(ammObject.node().tradingFee()).isEqualTo(ammInfoByAssets.amm().tradingFee());

    logger.info("Successfully verified AMM with MPT/IOU pair via ammInfo and ledgerEntry");
  }

  private void enableFlag(KeyPair issuerKeyPair, UnsignedInteger sequence, FeeResult feeResult,
                          AccountSetFlag accountSetFlag)
    throws JsonRpcClientErrorException, JsonProcessingException {
    AccountSet accountSet = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .sequence(sequence)
      .setFlag(accountSetFlag)
      .build();

    SingleSignedTransaction<AccountSet> signed = signatureService.sign(issuerKeyPair.privateKey(), accountSet);
    SubmitResult<AccountSet> setResult = xrplClient.submit(signed);
    assertThat(setResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      setResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress()),
      info -> info.accountData().flags().lsfDefaultRipple()
    );
  }
}
