package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

/**
 * Unit tests for {@link TransactionType}.
 */
public class TransactionTypeTests {

  @ParameterizedTest
  @ArgumentsSource(value = TransactionTypeValidArgumentProvider.class)
  public void shouldReturnTransactionTypeForValidValues(String value) {
    TransactionType transactionType = TransactionType.forValue(value);
    System.out.println(transactionType + "" + value);
    assertNotNull(transactionType);
    assertTrue(transactionType instanceof TransactionType);
  }

  @EmptySource
  @NullSource
  @ParameterizedTest
  @ArgumentsSource(value = TransactionTypeInvalidArgumentProvider.class)
  public void shouldThrowIllegalArgumentExceptionForInvalidValues(String value) {
    assertThrows(IllegalArgumentException.class, () -> TransactionType.forValue(value),
      "No matching TransactionType enum value for String value " + value);
  }

  public static class TransactionTypeValidArgumentProvider implements ArgumentsProvider {

    @Override
    public java.util.stream.Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return java.util.stream.Stream.of(TransactionType.values()).map(TransactionType::value).map(Arguments::of);
    }

  }

  public static class TransactionTypeInvalidArgumentProvider implements ArgumentsProvider {

    @Override
    public java.util.stream.Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return java.util.stream.Stream.of("bla", "blaaa", "123").map(Arguments::of);
    }

  }

  @Test
  public void testTxTypeCapitalization() {
    assertThat(TransactionType.ACCOUNT_DELETE.value()).isEqualTo("AccountDelete");
    assertThat(TransactionType.ACCOUNT_SET.value()).isEqualTo("AccountSet");
    assertThat(TransactionType.CHECK_CANCEL.value()).isEqualTo("CheckCancel");
    assertThat(TransactionType.CHECK_CASH.value()).isEqualTo("CheckCash");
    assertThat(TransactionType.CHECK_CREATE.value()).isEqualTo("CheckCreate");
    assertThat(TransactionType.DEPOSIT_PRE_AUTH.value()).isEqualTo("DepositPreauth");
    assertThat(TransactionType.ENABLE_AMENDMENT.value()).isEqualTo("EnableAmendment");
    assertThat(TransactionType.ESCROW_CANCEL.value()).isEqualTo("EscrowCancel");
    assertThat(TransactionType.ESCROW_CREATE.value()).isEqualTo("EscrowCreate");
    assertThat(TransactionType.ESCROW_FINISH.value()).isEqualTo("EscrowFinish");
    assertThat(TransactionType.OFFER_CANCEL.value()).isEqualTo("OfferCancel");
    assertThat(TransactionType.OFFER_CREATE.value()).isEqualTo("OfferCreate");
    assertThat(TransactionType.PAYMENT.value()).isEqualTo("Payment");
    assertThat(TransactionType.PAYMENT_CHANNEL_CLAIM.value()).isEqualTo("PaymentChannelClaim");
    assertThat(TransactionType.PAYMENT_CHANNEL_CREATE.value()).isEqualTo("PaymentChannelCreate");
    assertThat(TransactionType.PAYMENT_CHANNEL_FUND.value()).isEqualTo("PaymentChannelFund");
    assertThat(TransactionType.SET_FEE.value()).isEqualTo("SetFee");
    assertThat(TransactionType.SET_REGULAR_KEY.value()).isEqualTo("SetRegularKey");
    assertThat(TransactionType.SIGNER_LIST_SET.value()).isEqualTo("SignerListSet");
    assertThat(TransactionType.TRUST_SET.value()).isEqualTo("TrustSet");
    assertThat(TransactionType.TICKET_CREATE.value()).isEqualTo("TicketCreate");
    assertThat(TransactionType.UNL_MODIFY.value()).isEqualTo("UNLModify");
  }
}