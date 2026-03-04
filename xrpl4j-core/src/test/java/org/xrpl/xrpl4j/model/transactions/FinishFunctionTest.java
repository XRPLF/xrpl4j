package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class FinishFunctionTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    FinishFunction finishFunction = FinishFunction.of("0061736D");
    assertThat(finishFunction.toString()).isEqualTo("0061736D");

    FinishFunction finishFunctionUpper = FinishFunction.of("ABCDEF1234567890");
    assertThat(finishFunctionUpper.toString()).isEqualTo("ABCDEF1234567890");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    FinishFunction finishFunction = FinishFunction.of("0061736D01000000");
    FinishFunctionWrapper wrapper = FinishFunctionWrapper.of(finishFunction);

    String json = "{\"value\": \"0061736D01000000\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testInvalidHex() {
    assertThrows(IllegalArgumentException.class, () -> FinishFunction.of("ZZZZ"));
    assertThrows(IllegalArgumentException.class, () -> FinishFunction.of("0x1234"));
    assertThrows(IllegalArgumentException.class, () -> FinishFunction.of("not-hex"));
  }

  @Test
  void testEmptyValue() {
    FinishFunction finishFunction = FinishFunction.of("");
    assertThat(finishFunction.value()).isEqualTo("");
  }

  private void assertSerializesAndDeserializes(
    FinishFunctionWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    FinishFunctionWrapper deserialized = objectMapper.readValue(
      serialized, FinishFunctionWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableFinishFunctionWrapper.class)
  @JsonDeserialize(as = ImmutableFinishFunctionWrapper.class)
  interface FinishFunctionWrapper {

    static FinishFunctionWrapper of(FinishFunction value) {
      return ImmutableFinishFunctionWrapper.builder().value(value).build();
    }

    FinishFunction value();

  }
}

