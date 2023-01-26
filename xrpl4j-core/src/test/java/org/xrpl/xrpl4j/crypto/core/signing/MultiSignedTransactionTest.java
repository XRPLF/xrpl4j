package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.jayway.jsonassert.JsonAssert;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link MultiSignedTransaction}.
 */
class MultiSignedTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private SignatureWithPublicKey signature1;
  private SignatureWithPublicKey signature2;
  private MultiSignedTransaction<Payment> multiSignedTransaction;


  @BeforeEach
  void setUp() {
    signature1 = SignatureWithPublicKey.builder()
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "01"))
      .build();
    signature2 = SignatureWithPublicKey.builder()
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "00"))
      .build();
    multiSignedTransaction = MultiSignedTransaction.<Payment>builder()
      .unsignedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .build())
      .addSignatureWithPublicKeySet(
        signature1,
        signature2
      )
      .build();
  }

  @Test
  void testHash() {
    assertThat(multiSignedTransaction.hash().value())
      .isEqualTo("53F05196A66B08F30D1968A38BF8A9D85C624B03CF7AD2E8A7381AF1A7152043");
  }

  @Test
  void testSignedTransactionBytes() throws JsonProcessingException {
    assertThat(multiSignedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(
        multiSignedTransaction.signedTransaction()
      ))
    );
  }

  @Test
  void testSignedTransactionConstructed() {
    Transaction signedTransaction = multiSignedTransaction.signedTransaction();
    assertThat(signedTransaction.signers()).asList().hasSize(2)
      .extracting("signer.account", "signer.signingPublicKey", "signer.transactionSignature")
      .containsExactly(Tuple.tuple(
          signature2.signingPublicKey().deriveAddress(),
          signature2.signingPublicKey(),
          signature2.transactionSignature().base16Value()
        ),
        Tuple.tuple(
          signature1.signingPublicKey().deriveAddress(),
          signature1.signingPublicKey(),
          signature1.transactionSignature().base16Value()
        ));
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