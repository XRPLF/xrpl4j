package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link MultiSignedTransaction}.
 */
class MultiSignedTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private MultiSignedTransaction multiSignedTransaction;

  @BeforeEach
  void setUp() {
    multiSignedTransaction = MultiSignedTransaction.builder()
      .signedTransaction(Payment.builder()
        .account(Address.of(""))
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .signingPublicKey("")
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(Address.of(""))
        .build())
      .unsignedTransaction(Payment.builder()
        .account(Address.of(""))
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .signingPublicKey("")
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(Address.of(""))
        .build())
      .signedTransactionBytes(UnsignedByteArray.fromHex(HEX_32_BYTES))
      .signatureWithPublicKeySet(Sets.newHashSet(SignatureWithPublicKey.builder()
        .transactionSignature(Signature.builder()
          .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
          .build())
        .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "00"))
        .build()))
      .build();
  }

  @Test
  void value() {
    assertThat(multiSignedTransaction.hash().value())
      .isEqualTo("C68144B000C9C53DAA172705BF06D4B52DB7775A639F783C02741DE390625793");
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(multiSignedTransaction);
    JsonAssert.with(json).assertNotNull("$.unsignedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransactionBytes");
    JsonAssert.with(json).assertNotNull("$.signatureWithPublicKeySet");

    MultiSignedTransaction actual = ObjectMapperFactory.create().readValue(json, MultiSignedTransaction.class);
    assertThat(actual).isEqualTo(multiSignedTransaction);
  }
}