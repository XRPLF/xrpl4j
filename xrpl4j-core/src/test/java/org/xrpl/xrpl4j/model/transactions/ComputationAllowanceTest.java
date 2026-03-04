package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class ComputationAllowanceTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testFromUnsignedInteger() {
    ComputationAllowance allowance = ComputationAllowance.of(UnsignedInteger.valueOf(1000000));
    assertThat(allowance.toString()).isEqualTo("1000000");
    assertThat(allowance.value()).isEqualTo(UnsignedInteger.valueOf(1000000));
  }

  @Test
  void testZeroValue() {
    ComputationAllowance allowance = ComputationAllowance.of(UnsignedInteger.ZERO);
    assertThat(allowance.value()).isEqualTo(UnsignedInteger.ZERO);
  }

  @Test
  void testMaxValue() {
    ComputationAllowance allowance = ComputationAllowance.of(UnsignedInteger.MAX_VALUE);
    assertThat(allowance.value()).isEqualTo(UnsignedInteger.MAX_VALUE);
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    ComputationAllowance allowance = ComputationAllowance.of(UnsignedInteger.valueOf(500000));
    ComputationAllowanceWrapper wrapper = ComputationAllowanceWrapper.of(allowance);

    String json = "{\"value\": 500000}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    ComputationAllowanceWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    ComputationAllowanceWrapper deserialized = objectMapper.readValue(
      serialized, ComputationAllowanceWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableComputationAllowanceWrapper.class)
  @JsonDeserialize(as = ImmutableComputationAllowanceWrapper.class)
  interface ComputationAllowanceWrapper {

    static ComputationAllowanceWrapper of(ComputationAllowance value) {
      return ImmutableComputationAllowanceWrapper.builder().value(value).build();
    }

    ComputationAllowance value();

  }
}
