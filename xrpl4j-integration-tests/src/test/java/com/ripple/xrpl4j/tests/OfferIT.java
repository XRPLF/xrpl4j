package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.client.accounts.AccountInfoResult;
import com.ripple.xrpl4j.model.client.fees.FeeResult;
import com.ripple.xrpl4j.model.ledger.OfferObject;
import com.ripple.xrpl4j.model.ledger.RippleStateObject;
import com.ripple.xrpl4j.model.client.transactions.SubmitResult;
import com.ripple.xrpl4j.client.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.OfferCancel;
import com.ripple.xrpl4j.model.transactions.OfferCreate;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Supplier;

public class OfferIT extends AbstractIT {

  private final Address TESTNET_USD_ISSUER = Address.of("rD9W7ULveavz8qBGM1R5jMgK2QKsEDPQVi");
  public static final String CURRENCY = "USD";

  @Test
  public void createOpenOfferAndCancel() throws JsonRpcClientErrorException {
    // GIVEN a buy offer that has a really bad exchange rate
    // THEN the OfferCreate should generate an open offer on the order book

    //////////////////////
    // Generate and fund purchaser's account
    Wallet purchaser = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(() -> this.getValidatedAccountInfo(purchaser.classicAddress()));

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
        .issuer(TESTNET_USD_ISSUER)
        .value("1000")
        .build()
      )
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.OfferFlags.builder()
        .fullyCanonicalSig(true)
        .sell(true)
        .build())
      .build();

    SubmitResult<OfferCreate> response = xrplClient.submit(purchaser, offerCreate);
    assertThat(response.transaction().flags().tfFullyCanonicalSig()).isTrue();
    assertThat(response.transaction().flags().tfSell()).isTrue();

    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transaction().hash().orElse("n/a")
    );

    //////////////////////
    // Poll the ledger for the source purchaser's offers, and validate the expected offer exists
    OfferObject offerObject = scanForOffer(purchaser, sequence);
    assertThat(offerObject.takerGets()).isEqualTo(offerCreate.takerGets());
    assertThat(offerObject.takerPays()).isEqualTo(offerCreate.takerPays());

    cancelOffer(purchaser, offerObject.sequence());
  }

  /**
   * Cancels an offer and verifies the offer no longer exists on ledger for the account.
   * @param purchaser
   * @param offerSequence
   * @throws JsonRpcClientErrorException
   */
  private void cancelOffer(Wallet purchaser, UnsignedInteger offerSequence) throws JsonRpcClientErrorException {
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
    assertThat(cancelResponse.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");

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
    AccountInfoResult accountInfoResult = this.scanForResult(() -> this.getValidatedAccountInfo(purchaser.classicAddress()));

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
        .issuer(TESTNET_USD_ISSUER)
        .value("1000")
        .build()
      )
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.OfferFlags.builder()
        .fullyCanonicalSig(true)
        .immediateOrCancel(true)
        .build())
      .build();

    SubmitResult<OfferCreate> response = xrplClient.submit(purchaser, offerCreate);
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transaction().hash().orElse("n/a")
    );

    //////////////////////
    // Poll the ledger for the source purchaser's offers, and validate no offers or balances (ripple states) exist
    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.classicAddress(), OfferObject.class));
    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.classicAddress(), RippleStateObject.class));
  }

  /**
   * Asserts the supplier returns empty results, waiting up to 10 seconds for that condition to be true.
   * @param supplier results supplier.
   */
  private void assertEmptyResults(Supplier<Collection<?>> supplier) {
    Awaitility.await()
      .atMost(Duration.TEN_SECONDS)
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
    AccountInfoResult accountInfoResult = this.scanForResult(() -> this.getValidatedAccountInfo(purchaser.classicAddress()));

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    IssuedCurrencyAmount requestCurrencyAmount = IssuedCurrencyAmount.builder()
      .currency(CURRENCY)
      .issuer(TESTNET_USD_ISSUER)
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
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transaction().hash().orElse("n/a")
    );

    //////////////////////
    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaser, CURRENCY, TESTNET_USD_ISSUER);
    // The "issuer" for the balance in a trust line depends on whether the balance is positive or negative.
    // If a RippleState object shows a positive balance, the high account is the issuer.
    // If the balance is negative, the low account is the issuer.
    // Often, the issuer has its limit set to 0 and the other account has a positive limit, but this is not reliable
    // because limits can change without affecting an existing balance.
    if (issuedCurrency.lowLimit().issuer().equals(TESTNET_USD_ISSUER)) {
      assertThat(issuedCurrency.balance().value()).isEqualTo("-" + requestCurrencyAmount.value());
    } else {
      assertThat(issuedCurrency.balance().value()).isEqualTo(requestCurrencyAmount.value());
    }
  }

  @Test
  public void cancelNonExistentOffer() throws JsonRpcClientErrorException {
    Wallet purchaser = createRandomAccount();
    UnsignedInteger nonExistentOfferSequence = UnsignedInteger.valueOf(12345);
    // cancel offer does the assertions
    cancelOffer(purchaser, nonExistentOfferSequence);
  }

  public OfferObject scanForOffer(Wallet purchaser, UnsignedInteger sequence) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(purchaser.classicAddress(), OfferObject.class)
        .stream()
        .filter(object -> object.sequence().equals(sequence) && object.account().equals(purchaser.classicAddress()))
        .findFirst()
        .orElse(null));
  }

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
