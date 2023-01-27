package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jayway.jsonassert.JsonAssert;
import org.immutables.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.Arrays;

/**
 * Unit tests for {@link Signature}.
 */
class SignatureTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private Signature signature;

  @BeforeEach
  void setUp() {
    signature = Signature.builder()
      .value(UnsignedByteArray.of(new byte[32]))
      .build();
  }

  @Test
  void value() {
    assertThat(Arrays.equals(signature.value().toByteArray(), new byte[32])).isTrue();
  }

  @Test
  void base16Value() {
    assertThat(signature.base16Value()).isEqualTo(HEX_32_BYTES);
  }

  @Test
  void hexValue() {
    assertThat(signature.base16Value()).isEqualTo(signature.hexValue());
    assertThat(signature.base16Value()).isEqualTo(HEX_32_BYTES);
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(signature);
    JsonAssert.with(json).assertThat("$", is(HEX_32_BYTES));
    assertThat(json).isEqualTo("\"" + HEX_32_BYTES + "\"");

    Signature actual = ObjectMapperFactory.create().readValue(json, Signature.class);
    assertThat(actual).isEqualTo(signature);
  }

  @Test
  void of() {
    assertThat(Signature.of(signature.value())).isEqualTo(signature);
  }

  @Test
  void fromBase16() {
    assertThat(Signature.fromBase16(HEX_32_BYTES)).isEqualTo(signature);
  }

  @Test
  void serializeSignature() throws JsonProcessingException {

    ObjectMapper objectMapper = ObjectMapperFactory.create();

    WrappedSignature wrappedSignature = WrappedSignature.builder()
      .signature(signature)
      .build();
    String serializedWrappedSignature = objectMapper.writeValueAsString(wrappedSignature);

    String serialized = "{\"signature\":\"" + HEX_32_BYTES + "\"}";
    assertThat(serializedWrappedSignature).isEqualTo(serialized);

    WrappedSignature deserializedWrappedSignature = objectMapper.readValue(
      serializedWrappedSignature, WrappedSignature.class
    );
    assertThat(deserializedWrappedSignature).isEqualTo(wrappedSignature);

  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableWrappedSignature.class)
  @JsonDeserialize(as = ImmutableWrappedSignature.class)
  interface WrappedSignature {

    static ImmutableWrappedSignature.Builder builder() {
      return ImmutableWrappedSignature.builder();
    }

    Signature signature();
  }
}