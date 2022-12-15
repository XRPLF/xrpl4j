package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoRequestParams;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.Asset;
import org.xrpl.xrpl4j.model.transactions.AmmCreate;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.CustomEnvironment;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.math.BigDecimal;

public class AmmIT extends AbstractIT {

  @BeforeAll
  protected static void initXrplEnvironment() {
    xrplEnvironment = new CustomEnvironment(
      HttpUrl.parse("http://amm.devnet.rippletest.net:51234"),
      HttpUrl.parse("https://ammfaucet.devnet.rippletest.net")
    );
  }

  @Test
  void name() throws JsonRpcClientErrorException, JsonProcessingException {
    Wallet issuerWallet = createRandomAccount();
    Wallet counterpartyWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');
    AccountInfoResult issuerAccount = scanForResult(() -> this.getValidatedAccountInfo(issuerWallet.classicAddress()));
    AmmCreate ammCreate = AmmCreate.builder()
      .account(issuerWallet.classicAddress())
      .sequence(issuerAccount.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
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

    AmmInfoResult ammInfoResult = xrplClient.ammInfo(
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

    logger.info("ammInfoResult: {}", ammInfoResult);
  }
}
