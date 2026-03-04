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

public class EscrowDataTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    EscrowData data = EscrowData.of("DEADBEEF");
    assertThat(data.toString()).isEqualTo("DEADBEEF");

    EscrowData dataLower = EscrowData.of("abcdef1234567890");
    assertThat(dataLower.toString()).isEqualTo("abcdef1234567890");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    EscrowData data = EscrowData.of("48656C6C6F20576F726C64");
    EscrowDataWrapper wrapper = EscrowDataWrapper.of(data);

    String json = "{\"value\": \"48656C6C6F20576F726C64\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testInvalidHex() {
    assertThrows(IllegalArgumentException.class, () -> EscrowData.of("ZZZZ"));
    assertThrows(IllegalArgumentException.class, () -> EscrowData.of("0x1234"));
    assertThrows(IllegalArgumentException.class, () -> EscrowData.of("not-hex"));
  }

  @Test
  void testEmptyValue() {
    EscrowData data = EscrowData.of("");
    assertThat(data.value()).isEqualTo("");
  }

  private void assertSerializesAndDeserializes(
    EscrowDataWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    EscrowDataWrapper deserialized = objectMapper.readValue(
      serialized, EscrowDataWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableEscrowDataWrapper.class)
  @JsonDeserialize(as = ImmutableEscrowDataWrapper.class)
  interface EscrowDataWrapper {

    static EscrowDataWrapper of(EscrowData value) {
      return ImmutableEscrowDataWrapper.builder().value(value).build();
    }

    EscrowData value();

  }
}

