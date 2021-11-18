package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
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
    JsonAssert.with(json).assertThat("$.value", is(HEX_32_BYTES));

    Signature actual = ObjectMapperFactory.create().readValue(json, Signature.class);
    assertThat(actual).isEqualTo(signature);
  }

  @Test
  void of() {
    assertThat(Signature.of(signature.value())).isEqualTo(signature);
  }

  @Test
  void fromHex() {
    assertThat(Signature.fromHex(HEX_32_BYTES)).isEqualTo(signature);
  }

  @Test
  void fromBase16() {
    assertThat(Signature.fromBase16(HEX_32_BYTES)).isEqualTo(signature);
  }
}