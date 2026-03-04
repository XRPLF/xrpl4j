package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

public class WasmReturnCodeTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testPositiveValue() {
    WasmReturnCode returnCode = WasmReturnCode.of(1);
    assertThat(returnCode.toString()).isEqualTo("1");
    assertThat(returnCode.value()).isEqualTo(1);
  }

  @Test
  void testNegativeValue() {
    WasmReturnCode returnCode = WasmReturnCode.of(-1);
    assertThat(returnCode.toString()).isEqualTo("-1");
    assertThat(returnCode.value()).isEqualTo(-1);
  }

  @Test
  void testZeroValue() {
    WasmReturnCode returnCode = WasmReturnCode.of(0);
    assertThat(returnCode.value()).isEqualTo(0);
  }

  @Test
  void testMaxValue() {
    WasmReturnCode returnCode = WasmReturnCode.of(Integer.MAX_VALUE);
    assertThat(returnCode.value()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void testMinValue() {
    WasmReturnCode returnCode = WasmReturnCode.of(Integer.MIN_VALUE);
    assertThat(returnCode.value()).isEqualTo(Integer.MIN_VALUE);
  }

  @Test
  void testJsonPositive() throws JsonProcessingException, JSONException {
    WasmReturnCode returnCode = WasmReturnCode.of(42);
    WasmReturnCodeWrapper wrapper = WasmReturnCodeWrapper.of(returnCode);

    String json = "{\"value\": 42}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testJsonNegative() throws JsonProcessingException, JSONException {
    WasmReturnCode returnCode = WasmReturnCode.of(-42);
    WasmReturnCodeWrapper wrapper = WasmReturnCodeWrapper.of(returnCode);

    String json = "{\"value\": -42}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    WasmReturnCodeWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    WasmReturnCodeWrapper deserialized = objectMapper.readValue(
      serialized, WasmReturnCodeWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableWasmReturnCodeWrapper.class)
  @JsonDeserialize(as = ImmutableWasmReturnCodeWrapper.class)
  interface WasmReturnCodeWrapper {

    static WasmReturnCodeWrapper of(WasmReturnCode value) {
      return ImmutableWasmReturnCodeWrapper.builder().value(value).build();
    }

    WasmReturnCode value();

  }
}

