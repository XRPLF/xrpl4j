package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link SingleSingedTransaction}.
 */
class SingleSingedTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private SingleSingedTransaction singleSingedTransaction;

  @BeforeEach
  void setUp() {
    singleSingedTransaction = SingleSingedTransaction.builder()
      .signedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .signingPublicKey("")
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .build())
      .unsignedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .signingPublicKey("")
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .build())
      .signedTransactionBytes(UnsignedByteArray.fromHex(HEX_32_BYTES))
      .signature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .build();
  }

  @Test
  void value() {
    assertThat(singleSingedTransaction.hash().value())
      .isEqualTo("C68144B000C9C53DAA172705BF06D4B52DB7775A639F783C02741DE390625793");
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(singleSingedTransaction);
    JsonAssert.with(json).assertNotNull("$.unsignedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransactionBytes");
    JsonAssert.with(json).assertNotNull("$.signature");

    SingleSingedTransaction actual = ObjectMapperFactory.create().readValue(json, SingleSingedTransaction.class);
    assertThat(actual).isEqualTo(singleSingedTransaction);
  }

}