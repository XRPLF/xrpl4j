package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.AccountSetTransactionFlags;
import org.xrpl.xrpl4j.model.flags.EnableAmendmentFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.stream.Stream;

/**
 * Unit tests for {@link RawTransactionWrapper}.
 */
class RawTransactionWrapperTest {

  private ObjectMapper objectMapper;
  private static final Address ACCOUNT = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
  private static final Address DESTINATION = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void testBuilderWithValidInnerTransaction() {
    Payment innerPayment = createValidInnerPayment();

    RawTransactionWrapper wrapper = RawTransactionWrapper.builder()
      .rawTransaction(innerPayment)
      .build();

    assertThat(wrapper.rawTransaction()).isEqualTo(innerPayment);
    assertThat(wrapper.rawTransaction().transactionFlags().tfInnerBatchTxn()).isTrue();
  }

  @Test
  void testOfFactoryMethod() {
    Payment innerPayment = createValidInnerPayment();

    RawTransactionWrapper wrapper = RawTransactionWrapper.of(innerPayment);

    assertThat(wrapper.rawTransaction()).isEqualTo(innerPayment);
    assertThat(wrapper.rawTransaction().transactionFlags().tfInnerBatchTxn()).isTrue();
  }

  @Test
  void testValidationFailsWhenTfInnerBatchTxnNotSet() {
    Payment paymentWithoutFlag = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .build();

    assertThatThrownBy(() -> RawTransactionWrapper.of(paymentWithoutFlag))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Inner transaction must have the `tfInnerBatchTxn` flag set.");
  }

  @Test
  void testValidationFailsWhenTfInnerBatchTxnNotSetUsingBuilder() {
    Payment paymentWithoutFlag = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(PaymentFlags.builder().tfPartialPayment(true).build())
      .build();

    assertThatThrownBy(() -> RawTransactionWrapper.builder()
      .rawTransaction(paymentWithoutFlag)
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Inner transaction must have the `tfInnerBatchTxn` flag set.");
  }

  /**
   * Pseudo-transactions carrying {@code tfInnerBatchTxn}, each of which would otherwise satisfy the flag check. The
   * ledger emits these itself and xrpld rejects them from users, so they must never be wrapped as inner
   * transactions no matter which flags they carry.
   */
  static Stream<Arguments> pseudoTransactionsWithInnerBatchFlag() {
    return Stream.of(
      Arguments.of(
        EnableAmendment.builder()
          .account(ACCOUNT)
          .fee(XrpCurrencyAmount.ofDrops(0))
          .sequence(UnsignedInteger.ONE)
          .amendment(Hash256.of("42426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369F97ABF044EE"))
          .flags(EnableAmendmentFlags.of(TransactionFlags.INNER_BATCH_TXN.getValue()))
          .build()
      ),
      Arguments.of(
        SetFee.builder()
          .account(ACCOUNT)
          .fee(XrpCurrencyAmount.ofDrops(0))
          .sequence(UnsignedInteger.ONE)
          .baseFeeDrops(XrpCurrencyAmount.ofDrops(10))
          .reserveBaseDrops(XrpCurrencyAmount.ofDrops(20_000_000))
          .reserveIncrementDrops(XrpCurrencyAmount.ofDrops(5_000_000))
          .flags(TransactionFlags.INNER_BATCH_TXN)
          .build()
      ),
      Arguments.of(
        UnlModify.builder()
          .fee(XrpCurrencyAmount.ofDrops(0))
          .sequence(UnsignedInteger.ONE)
          .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
          .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
          .unlModifyDisabling(UnsignedInteger.ONE)
          .flags(TransactionFlags.INNER_BATCH_TXN)
          .build()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("pseudoTransactionsWithInnerBatchFlag")
  void testValidationFailsForPseudoTransaction(Transaction pseudoTransaction) {
    // Precondition: the flag check alone would have let this through.
    assertThat(pseudoTransaction.transactionFlags().tfInnerBatchTxn()).isTrue();

    assertThatThrownBy(() -> RawTransactionWrapper.of(pseudoTransaction))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Inner transaction must not be a pseudo-transaction, but was " +
        pseudoTransaction.transactionType() + ".");
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    Payment innerPayment = createValidInnerPayment();
    RawTransactionWrapper wrapper = RawTransactionWrapper.of(innerPayment);

    String expectedJson = String.format(
      "{\"RawTransaction\":{" +
        "\"TransactionType\":\"Payment\"," +
        "\"Account\":\"%s\"," +
        "\"Destination\":\"%s\"," +
        "\"Amount\":\"1000\"," +
        "\"Fee\":\"0\"," +
        "\"Sequence\":1," +
        "\"Flags\":%d," +
        "\"SigningPubKey\":\"\"" +
        "}}",
      ACCOUNT.value(),
      DESTINATION.value(),
      PaymentFlags.INNER_BATCH_TXN.getValue()
    );

    assertSerializesAndDeserializes(wrapper, expectedJson);
  }

  @Test
  void testJsonDeserialization() throws JsonProcessingException {
    String json = String.format(
      "{\"RawTransaction\":{" +
        "\"TransactionType\":\"Payment\"," +
        "\"Account\":\"%s\"," +
        "\"Destination\":\"%s\"," +
        "\"Amount\":\"1000\"," +
        "\"Fee\":\"0\"," +
        "\"Sequence\":1," +
        "\"Flags\":%d," +
        "\"SigningPubKey\":\"\"" +
        "}}",
      ACCOUNT.value(),
      DESTINATION.value(),
      PaymentFlags.INNER_BATCH_TXN.getValue()
    );

    RawTransactionWrapper deserialized = objectMapper.readValue(json, RawTransactionWrapper.class);

    assertThat(deserialized.rawTransaction()).isInstanceOf(Payment.class);
    Payment payment = (Payment) deserialized.rawTransaction();
    assertThat(payment.account()).isEqualTo(ACCOUNT);
    assertThat(payment.destination()).isEqualTo(DESTINATION);
    assertThat(payment.amount()).isEqualTo(XrpCurrencyAmount.ofDrops(1000));
    assertThat(payment.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
    assertThat(payment.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(payment.flags().tfInnerBatchTxn()).isTrue();
  }

  @Test
  void testRoundTripSerialization() throws JsonProcessingException {
    Payment innerPayment = createValidInnerPayment();
    RawTransactionWrapper original = RawTransactionWrapper.of(innerPayment);

    String serialized = objectMapper.writeValueAsString(original);
    RawTransactionWrapper deserialized = objectMapper.readValue(serialized, RawTransactionWrapper.class);

    assertThat(deserialized).isEqualTo(original);
    assertThat(deserialized.rawTransaction()).isEqualTo(innerPayment);
  }

  @Test
  void testWithDifferentTransactionType() {
    // Test with AccountSet transaction instead of Payment
    AccountSet innerAccountSet = AccountSet.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .flags(AccountSetTransactionFlags.builder()
        .tfInnerBatchTxn(true)
        .build())
      .build();

    RawTransactionWrapper wrapper = RawTransactionWrapper.of(innerAccountSet);

    assertThat(wrapper.rawTransaction()).isEqualTo(innerAccountSet);
    assertThat(wrapper.rawTransaction().transactionFlags().tfInnerBatchTxn()).isTrue();
  }

  @Test
  void testEqualityAndHashCode() {
    Payment innerPayment1 = createValidInnerPayment();
    Payment innerPayment2 = createValidInnerPayment();

    RawTransactionWrapper wrapper1 = RawTransactionWrapper.of(innerPayment1);
    RawTransactionWrapper wrapper2 = RawTransactionWrapper.of(innerPayment2);

    assertThat(wrapper1).isEqualTo(wrapper2);
    assertThat(wrapper1.hashCode()).isEqualTo(wrapper2.hashCode());
  }

  @Test
  void testToString() {
    Payment innerPayment = createValidInnerPayment();
    RawTransactionWrapper wrapper = RawTransactionWrapper.of(innerPayment);

    String toString = wrapper.toString();
    assertThat(toString).contains("RawTransactionWrapper");
    assertThat(toString).contains("rawTransaction");
  }

  private Payment createValidInnerPayment() {
    return Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();
  }

  private void assertSerializesAndDeserializes(
    RawTransactionWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    RawTransactionWrapper deserialized = objectMapper.readValue(serialized, RawTransactionWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }
}
