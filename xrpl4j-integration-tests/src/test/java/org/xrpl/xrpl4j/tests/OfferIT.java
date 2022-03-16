package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class OfferIT extends AbstractIT {

  public static final String CURRENCY = "USD";

  private static Wallet issuerWallet;

  private static boolean usdIssued = false;

  /**
   * Sets up an issued currency (USD) that can be used to test Offers against this currency.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  @BeforeEach
  public void ensureUsdIssued() throws JsonRpcClientErrorException {
    // this only needs to run once before all tests but can't be a BeforeAll static method due to dependencies on
    // instance methods in AbstractIT
    if (usdIssued) {
      return;
    }
    issuerWallet = createRandomAccount();
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult =
      this.scanForResult(() -> this.getValidatedAccountInfo(issuerWallet.classicAddress()));

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    OfferCreate offerCreate = OfferCreate.builder()
      .account(issuerWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(sequence)
      .signingPublicKey(issuerWallet.publicKey())
      .takerGets(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerWallet.classicAddress())
        .value("100")
        .build()
      )
      .takerPays(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(200.0)))
      .flags(Flags.OfferCreateFlags.builder()
        .tfFullyCanonicalSig(true)
        .tfSell(true)
        .build())
      .build();

    SubmitResult<OfferCreate> response = xrplClient.submit(issuerWallet, offerCreate);
    assertThat(response.transactionResult().transaction().flags().tfFullyCanonicalSig()).isTrue();
    assertThat(response.transactionResult().transaction().flags().tfSell()).isTrue();

    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(response.transactionResult().hash());
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );
    usdIssued = true;
  }

  @Test
  public void createOpenOfferAndCancel() throws JsonRpcClientErrorException {
    // GIVEN a buy offer that has a really bad exchange rate
    // THEN the OfferCreate should generate an open offer on the order book

    //////////////////////
    // Generate and fund purchaser's account
    Wallet purchaser = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.classicAddress())
    );

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    OfferCreate offerCreate = OfferCreate.builder()
      .account(purchaser.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(sequence)
      .signingPublicKey(purchaser.publicKey())
      .takerPays(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerWallet.classicAddress())
        .value("1000")
        .build()
      )
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.OfferCreateFlags.builder()
        .tfFullyCanonicalSig(true)
        .tfSell(true)
        .build())
      .build();

    SubmitResult<OfferCreate> response = xrplClient.submit(purchaser, offerCreate);
    assertThat(response.transactionResult().transaction().flags().tfFullyCanonicalSig()).isTrue();
    assertThat(response.transactionResult().transaction().flags().tfSell()).isTrue();

    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(response.transactionResult().hash());
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source purchaser's offers, and validate the expected offer exists
    OfferObject offerObject = scanForOffer(purchaser, sequence);
    assertThat(offerObject.takerGets()).isEqualTo(offerCreate.takerGets());
    assertThat(offerObject.takerPays()).isEqualTo(offerCreate.takerPays());

    cancelOffer(purchaser, offerObject.sequence(), "tesSUCCESS");
  }

  /**
   * Cancels an offer and verifies the offer no longer exists on ledger for the account.
   *
   * @param purchaser     The {@link Wallet} of the buyer.
   * @param offerSequence The sequence of the offer.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  private void cancelOffer(
    Wallet purchaser,
    UnsignedInteger offerSequence,
    String expectedResult
  ) throws JsonRpcClientErrorException {
    AccountInfoResult infoResult = this.scanForResult(() -> this.getValidatedAccountInfo(purchaser.classicAddress()));
    UnsignedInteger nextSequence = infoResult.accountData().sequence();

    OfferCancel offerCancel = OfferCancel.builder()
      .account(purchaser.classicAddress())
      .fee(xrplClient.fee().drops().minimumFee())
      .sequence(nextSequence)
      .offerSequence(offerSequence)
      .signingPublicKey(purchaser.publicKey())
      .build();

    SubmitResult<OfferCancel> cancelResponse = xrplClient.submit(purchaser, offerCancel);
    assertThat(cancelResponse.result()).isEqualTo(expectedResult);

    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.classicAddress(), OfferObject.class));
    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.classicAddress(), RippleStateObject.class));
  }

  @Test
  public void createUnmatchedKillOrFill() throws JsonRpcClientErrorException {
    // GIVEN a buy offer that has a really bad exchange rate
    // THEN the OfferCreate should not match any offers and immediately be killed

    //////////////////////
    // Generate and fund purchaser's account
    Wallet purchaser = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.classicAddress())
    );

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    OfferCreate offerCreate = OfferCreate.builder()
      .account(purchaser.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(sequence)
      .signingPublicKey(purchaser.publicKey())
      .takerPays(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerWallet.classicAddress())
        .value("1000")
        .build()
      )
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.OfferCreateFlags.builder()
        .tfFullyCanonicalSig(true)
        .tfImmediateOrCancel(true)
        .build())
      .build();

    SubmitResult<OfferCreate> response = xrplClient.submit(purchaser, offerCreate);
    assertThat(response.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source purchaser's offers, and validate no offers or balances (ripple states) exist
    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.classicAddress(), OfferObject.class));
    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.classicAddress(), RippleStateObject.class));
  }

  /**
   * Asserts the supplier returns empty results, waiting up to 10 seconds for that condition to be true.
   *
   * @param supplier results supplier.
   */
  private void assertEmptyResults(Supplier<Collection<?>> supplier) {
    Awaitility.await()
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> supplier.get(), Matchers.empty());
  }

  @Test
  public void createFullyMatchedOffer() throws JsonRpcClientErrorException {
    // GIVEN a buy offer that has a really great exchange rate
    // THEN the OfferCreate should fully match an open offer and generate a balance

    //////////////////////
    // Generate and fund purchaser's account
    Wallet purchaser = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.classicAddress())
    );

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    IssuedCurrencyAmount requestCurrencyAmount = IssuedCurrencyAmount.builder()
      .currency(CURRENCY)
      .issuer(issuerWallet.classicAddress())
      .value("0.01")
      .build();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(purchaser.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(sequence)
      .signingPublicKey(purchaser.publicKey())
      .takerPays(requestCurrencyAmount)
      .takerGets(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10.0)))
      .build();

    SubmitResult<OfferCreate> response = xrplClient.submit(purchaser, offerCreate);
    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(response.transactionResult().hash());
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaser, CURRENCY, issuerWallet.classicAddress());
    // The "issuer" for the balance in a trust line depends on whether the balance is positive or negative.
    // If a RippleState object shows a positive balance, the high account is the issuer.
    // If the balance is negative, the low account is the issuer.
    // Often, the issuer has its limit set to 0 and the other account has a positive limit, but this is not reliable
    // because limits can change without affecting an existing balance.
    if (issuedCurrency.lowLimit().issuer().equals(issuerWallet.classicAddress())) {
      assertThat(issuedCurrency.balance().value()).isEqualTo("-" + requestCurrencyAmount.value());
    } else {
      assertThat(issuedCurrency.balance().value()).isEqualTo(requestCurrencyAmount.value());
    }
  }

  @Test
  public void cancelNonExistentOffer() throws JsonRpcClientErrorException {
    Wallet purchaser = createRandomAccount();
    UnsignedInteger nonExistentOfferSequence = UnsignedInteger.valueOf(111111111);
    // cancel offer does the assertions
    cancelOffer(purchaser, nonExistentOfferSequence, "temBAD_SEQUENCE");
  }

  /**
   * Scan the ledger for a specific Offer.
   *
   * @param purchaser The {@link Wallet} of the offer owner.
   * @param sequence  The sequence of the offer.
   *
   * @return An {@link OfferObject}.
   */
  public OfferObject scanForOffer(Wallet purchaser, UnsignedInteger sequence) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(purchaser.classicAddress(), OfferObject.class)
        .stream()
        .filter(object -> object.sequence().equals(sequence) && object.account().equals(purchaser.classicAddress()))
        .findFirst()
        .orElse(null));
  }

  /**
   * Scan the ledger until the purchaser account has a trustline with the issuer account for a given currency.
   *
   * @param purchaser The {@link Wallet} of the source account.
   * @param currency  A {@link String} denoting the currency code.
   * @param issuer    The {@link Address} of the issuer account.
   *
   * @return The {@link RippleStateObject} between the purchaser and issuer, if found.
   */
  public RippleStateObject scanForIssuedCurrency(Wallet purchaser, String currency, Address issuer) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(purchaser.classicAddress(), RippleStateObject.class)
        .stream()
        .filter(state ->
          state.balance().currency().equals(currency) &&
            (state.lowLimit().issuer().equals(issuer)) || state.highLimit().issuer().equals(issuer))
        .findFirst()
        .orElse(null));
  }

}
