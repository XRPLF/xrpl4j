package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link SingleSignedTransaction}.
 */
class SingleSingedTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private SingleSignedTransaction singleSingedTransaction;

  @BeforeEach
  void setUp() {
    singleSingedTransaction = SingleSignedTransaction.builder()
      .signedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .build())
      .unsignedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .build())
      .signature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .build();
  }

  @Test
  void value() {
    assertThat(singleSingedTransaction.hash().value())
      .isEqualTo("3C9BE23F7820DDC3D93CF69A7E47C6A89FEB0EF27A1F25E62F87F9E1F7E6E910");
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(singleSingedTransaction);
    JsonAssert.with(json).assertNotNull("$.unsignedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransactionBytes");
    JsonAssert.with(json).assertNotNull("$.signature");

    SingleSignedTransaction actual = ObjectMapperFactory.create().readValue(json, SingleSignedTransaction.class);
    assertThat(actual).isEqualTo(singleSingedTransaction);
  }

}