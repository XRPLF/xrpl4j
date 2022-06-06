package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link SignatureWithKeyMetadata}.
 */
class SignatureWithKeyMetadataTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private SignatureWithKeyMetadata signatureWithKeyMetadata;

  @BeforeEach
  void setUp() {
    signatureWithKeyMetadata = SignatureWithKeyMetadata.builder()
      .signingKeyMetadata(KeyMetadata.EMPTY)
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .build();
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(signatureWithKeyMetadata);
    JsonAssert.with(json).assertNotNull("$.signingKeyMetadata");
    JsonAssert.with(json).assertNotNull("$.transactionSignature");

    SignatureWithKeyMetadata actual = ObjectMapperFactory.create().readValue(json, SignatureWithKeyMetadata.class);
    assertThat(actual).isEqualTo(signatureWithKeyMetadata);
  }

}