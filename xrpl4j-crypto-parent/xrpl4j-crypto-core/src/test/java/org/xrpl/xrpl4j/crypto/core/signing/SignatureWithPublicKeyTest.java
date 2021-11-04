package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link SignatureWithPublicKey}.
 */
class SignatureWithPublicKeyTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private SignatureWithPublicKey signatureWithPublicKey;

  @BeforeEach
  void setUp() {
    signatureWithPublicKey = SignatureWithPublicKey.builder()
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "00"))
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .build();
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(signatureWithPublicKey);
    JsonAssert.with(json).assertNotNull("$.signingPublicKey");
    JsonAssert.with(json).assertNotNull("$.transactionSignature");

    SignatureWithPublicKey actual = ObjectMapperFactory.create().readValue(json, SignatureWithPublicKey.class);
    assertThat(actual).isEqualTo(signatureWithPublicKey);
  }

}