package com.ripple.xrpl4j.codec.fixtures.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableValueTest.class)
@JsonDeserialize(as = ImmutableValueTest.class)
public interface ValueTest {

  @JsonProperty("test_json")
  JsonNode testJson();

  @Nullable
  @JsonProperty("type_id")
  Integer typeId();

  @Value.Default
  @JsonProperty("is_native")
  default boolean isNative() {
    return false;
  }

  String type();

  @Nullable
  @JsonProperty("expected_hex")
  String expectedHex();

  @Value.Default
  @JsonProperty("is_negative")
  default boolean isNegative() {
    return false;
  }

  @Nullable
  @JsonProperty("type_specialisation_field")
  String typeSpecializationField();

}
