package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoAuctionSlot;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoRequestParams;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.Asset;
import org.xrpl.xrpl4j.model.ledger.AuthAccount;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AmmBid;
import org.xrpl.xrpl4j.model.transactions.AmmCreate;
import org.xrpl.xrpl4j.model.transactions.AmmDeposit;
import org.xrpl.xrpl4j.model.transactions.AmmVote;
import org.xrpl.xrpl4j.model.transactions.AmmWithdraw;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.CustomEnvironment;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * All tests in this class will be disabled until AMM functionality has been merged into the rippled codebase and
 * is available in a local standalone docker container. Running these tests as part of a maven build will
 * overwrite the value of xrplEnvironment, so any ITs run after these ITs will point at the AMM devnet. This is
 * obviously undesirable, and in lieu of making a broader change to enable custom environments for specific test suites,
 * we choose to simply disable these tests until we can run them against the normal xrplEnvironment.
 */
public class AmmIT extends AbstractIT {

  String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

  //  @BeforeAll
  protected static void initXrplEnvironment() {
    xrplEnvironment = new CustomEnvironment(
      HttpUrl.parse("http://amm.devnet.rippletest.net:51234"),
      HttpUrl.parse("https://ammfaucet.devnet.rippletest.net")
    );
  }

  //  @Test
  void depositAndVoteOnTradingFee() throws JsonRpcClientErrorException, JsonProcessingException {
    Wallet issuerWallet = createRandomAccount();
    AmmInfoResult amm = createAmm(issuerWallet);
    Wallet traderWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult traderAccount = scanForResult(() -> this.getValidatedAccountInfo(traderWallet.classicAddress()));
    SingleKeySignatureService signatureService = new SingleKeySignatureService(
      PrivateKey.fromBase16EncodedPrivateKey(traderWallet.privateKey().get())
    );

    AccountInfoResult traderAccountAfterDeposit = depositXrp(
      issuerWallet,
      traderWallet,
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
      .lastLedgerSequence(traderAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(8)).unsignedIntegerValue())
      .signingPublicKey(traderWallet.publicKey())
      .asset2(
        Asset.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerWallet.classicAddress())
          .build()
      )
      .asset(Asset.XRP)
      .tradingFee(newTradingFee)
      .build();

    SignedTransaction<AmmVote> signedVote = signatureService.sign(KeyMetadata.EMPTY, ammVote);

    SubmitResult<Transaction> voteSubmitResult = xrplClient.submit(signedVote);
    assertThat(voteSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedVote.hash(),
      traderAccount.ledgerIndexSafe(),
      ammVote.lastLedgerSequence().get(),
      ammVote.sequence(),
      traderWallet.classicAddress()
    );

    AmmInfoResult ammAfterVote = getAmmInfo(issuerWallet);
    assertThat(ammAfterVote.amm().tradingFee()).isEqualTo(newTradingFee);
  }

  //  @Test
  void depositAndBid() throws JsonRpcClientErrorException, JsonProcessingException {
    Wallet issuerWallet = createRandomAccount();
    AmmInfoResult amm = createAmm(issuerWallet);
    Wallet traderWallet = createRandomAccount();
    Wallet authAccount1 = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult traderAccount = scanForResult(() -> this.getValidatedAccountInfo(traderWallet.classicAddress()));
    SingleKeySignatureService signatureService = new SingleKeySignatureService(
      PrivateKey.fromBase16EncodedPrivateKey(traderWallet.privateKey().get())
    );

    AccountInfoResult traderAccountAfterDeposit = depositXrp(
      issuerWallet,
      traderWallet,
      traderAccount,
      amm,
      signatureService,
      feeResult
    );

    AmmBid bid = AmmBid.builder()
      .account(traderAccount.accountData().account())
      .sequence(traderAccountAfterDeposit.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(traderAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(8)).unsignedIntegerValue())
      .signingPublicKey(traderWallet.publicKey())
      .asset2(
        Asset.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerWallet.classicAddress())
          .build()
      )
      .asset(Asset.XRP)
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(authAccount1.classicAddress()))
      )
      .bidMin(
        IssuedCurrencyAmount.builder()
          .from(amm.amm().lpToken())
          .value("100")
          .build()
      )
      .build();

    SignedTransaction<AmmBid> signedBid = signatureService.sign(KeyMetadata.EMPTY, bid);

    SubmitResult<Transaction> voteSubmitResult = xrplClient.submit(signedBid);
    assertThat(voteSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedBid.hash(),
      traderAccount.ledgerIndexSafe(),
      bid.lastLedgerSequence().get(),
      bid.sequence(),
      traderWallet.classicAddress()
    );

    AmmInfoResult ammAfterBid = getAmmInfo(issuerWallet);

    assertThat(ammAfterBid.amm().auctionSlot()).isNotEmpty();
    AmmInfoAuctionSlot auctionSlot = ammAfterBid.amm().auctionSlot().get();
    assertThat(auctionSlot.account()).isEqualTo(traderAccount.accountData().account());
    assertThat(auctionSlot.authAccounts()).asList().extracting("account")
      .containsExactly(authAccount1.classicAddress());
  }

  //  @Test
  void depositAndWithdraw() throws JsonRpcClientErrorException, JsonProcessingException {
    Wallet issuerWallet = createRandomAccount();
    AmmInfoResult amm = createAmm(issuerWallet);
    Wallet traderWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult traderAccount = scanForResult(() -> this.getValidatedAccountInfo(traderWallet.classicAddress()));
    SingleKeySignatureService signatureService = new SingleKeySignatureService(
      PrivateKey.fromBase16EncodedPrivateKey(traderWallet.privateKey().get())
    );

    AccountInfoResult traderAccountAfterDeposit = depositXrp(
      issuerWallet,
      traderWallet,
      traderAccount,
      amm,
      signatureService,
      feeResult
    );

    AmmInfoResult ammInfoAfterDeposit = getAmmInfo(issuerWallet);
    AmmWithdraw withdraw = AmmWithdraw.builder()
      .account(traderWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccountAfterDeposit.accountData().sequence())
      .lastLedgerSequence(
        traderAccountAfterDeposit.ledgerCurrentIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue()
      )
      .signingPublicKey(traderWallet.publicKey())
      .asset2(
        Asset.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerWallet.classicAddress())
          .build()
      )
      .asset(Asset.XRP)
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(90)))
      .flags(Flags.AmmWithdrawFlags.SINGLE_ASSET)
      .build();

    SignedTransaction<AmmWithdraw> signedWithdraw = signatureService.sign(KeyMetadata.EMPTY, withdraw);

    SubmitResult<Transaction> voteSubmitResult = xrplClient.submit(signedWithdraw);
    assertThat(voteSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedWithdraw.hash(),
      traderAccount.ledgerIndexSafe(),
      withdraw.lastLedgerSequence().get(),
      withdraw.sequence(),
      traderWallet.classicAddress()
    );

    AmmInfoResult ammAfterWithdraw = getAmmInfo(issuerWallet);
    assertThat(ammAfterWithdraw.amm().amount2()).isInstanceOf(XrpCurrencyAmount.class)
      .isEqualTo(((XrpCurrencyAmount) ammInfoAfterDeposit.amm().amount2())
        .minus((XrpCurrencyAmount) withdraw.amount().get()));

    AccountInfoResult traderAccountAfterWithdraw = xrplClient.accountInfo(
      AccountInfoRequestParams.of(traderWallet.classicAddress())
    );

    assertThat(traderAccountAfterWithdraw.accountData().balance()).isEqualTo(
      traderAccountAfterDeposit.accountData().balance()
        .minus(withdraw.fee())
        .plus((XrpCurrencyAmount) withdraw.amount().get())
    );
  }

  private AccountInfoResult depositXrp(
    Wallet issuerWallet,
    Wallet traderWallet,
    AccountInfoResult traderAccount,
    AmmInfoResult amm,
    SignatureService signatureService,
    FeeResult feeResult
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    XrpCurrencyAmount depositAmount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(100));
    AmmDeposit deposit = AmmDeposit.builder()
      .account(traderAccount.accountData().account())
      .asset2(
        Asset.builder()
          .currency(xrpl4jCoin)
          .issuer(issuerWallet.classicAddress())
          .build()
      )
      .asset(Asset.XRP)
      .amount(depositAmount)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(traderAccount.accountData().sequence())
      .signingPublicKey(traderWallet.publicKey())
      .lastLedgerSequence(traderAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .build();

    SignedTransaction<AmmDeposit> signedDeposit = signatureService.sign(KeyMetadata.EMPTY, deposit);
    SubmitResult<Transaction> submitResult = xrplClient.submit(signedDeposit);
    assertThat(submitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedDeposit.hash(),
      traderAccount.ledgerIndexSafe(),
      deposit.lastLedgerSequence().get(),
      deposit.sequence(),
      traderWallet.classicAddress()
    );

    AccountInfoResult traderAccountAfterDeposit = xrplClient.accountInfo(
      AccountInfoRequestParams.of(traderAccount.accountData().account())
    );

    assertThat(traderAccountAfterDeposit.accountData().balance())
      .isEqualTo(traderAccount.accountData().balance().minus(deposit.fee()).minus(depositAmount));

    AccountLinesResult traderLines = xrplClient.accountLines(
      AccountLinesRequestParams.builder()
        .account(traderAccount.accountData().account())
        .peer(amm.amm().ammAccount())
        .build()
    );

    assertThat(traderLines.lines()).asList().hasSize(1);
    TrustLine lpLine = traderLines.lines().get(0);
    assertThat(lpLine.currency()).isEqualTo(amm.amm().lpToken().currency());
    assertThat(new BigDecimal(lpLine.balance())).isGreaterThan(BigDecimal.ZERO);

    return traderAccountAfterDeposit;
  }

  private AmmInfoResult createAmm(Wallet issuerWallet) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccount = scanForResult(() -> this.getValidatedAccountInfo(issuerWallet.classicAddress()));
    XrpCurrencyAmount reserveAmount = xrplClient.serverInformation().info()
      .map(
        rippled -> rippled.closedLedger().orElse(rippled.validatedLedger().get()).reserveIncXrp(),
        clio -> clio.validatedLedger().get().reserveIncXrp(),
        reporting -> reporting.closedLedger().orElse(reporting.validatedLedger().get()).reserveIncXrp()
      );
    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuerWallet.classicAddress())
      .sequence(issuerAccount.accountData().sequence())
      .fee(reserveAmount)
      .amount(
        IssuedCurrencyAmount.builder()
          .issuer(issuerWallet.classicAddress())
          .currency(xrpl4jCoin)
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(100)))
      .tradingFee(TradingFee.ofPercent(BigDecimal.ONE))
      .lastLedgerSequence(issuerAccount.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .signingPublicKey(issuerWallet.publicKey())
      .build();

    SingleKeySignatureService signatureService = new SingleKeySignatureService(
      PrivateKey.fromBase16EncodedPrivateKey(issuerWallet.privateKey().get())
    );

    SignedTransaction<AmmCreate> signedCreate = signatureService.sign(KeyMetadata.EMPTY, ammCreate);
    SubmitResult<Transaction> submitResult = xrplClient.submit(signedCreate);
    assertThat(submitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedCreate.hash(),
      issuerAccount.ledgerIndexSafe(),
      ammCreate.lastLedgerSequence().get(),
      ammCreate.sequence(),
      issuerWallet.classicAddress()
    );

    return getAmmInfo(issuerWallet);
  }

  private AmmInfoResult getAmmInfo(Wallet issuerWallet) throws JsonRpcClientErrorException {
    return xrplClient.ammInfo(
      AmmInfoRequestParams.builder()
        .asset(
          Asset.builder()
            .issuer(issuerWallet.classicAddress())
            .currency(xrpl4jCoin)
            .build()
        )
        .asset2(Asset.XRP)
        .build()
    );
  }
}
