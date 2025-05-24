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

import java.math.BigDecimal;

public class VoteWeightTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void bigDecimalValue() {
    VoteWeight weight = VoteWeight.of(UnsignedInteger.ZERO);
    assertThat(weight.bigDecimalValue().compareTo(BigDecimal.ZERO)).isEqualTo(0);

    weight = VoteWeight.of(UnsignedInteger.ONE);
    assertThat(weight.bigDecimalValue().compareTo(BigDecimal.valueOf(0.001))).isEqualTo(0);

    weight = VoteWeight.of(UnsignedInteger.valueOf(999));
    assertThat(weight.bigDecimalValue().compareTo(BigDecimal.valueOf(0.999))).isEqualTo(0);

    weight = VoteWeight.of(UnsignedInteger.valueOf(99_000));
    assertThat(weight.bigDecimalValue().compareTo(BigDecimal.valueOf(99.000))).isEqualTo(0);

    weight = VoteWeight.of(UnsignedInteger.valueOf(100_000));
    assertThat(weight.bigDecimalValue().compareTo(BigDecimal.valueOf(100.000))).isEqualTo(0);
  }

  @Test
  void testToString() {
    VoteWeight weight = VoteWeight.of(UnsignedInteger.ZERO);
    assertThat(weight.toString()).isEqualTo(UnsignedInteger.ZERO.toString());
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    VoteWeight voteWeight = VoteWeight.of(UnsignedInteger.valueOf(1000));
    VoteWeightWrapper wrapper = VoteWeightWrapper.of(voteWeight);

    String json = "{\"voteWeight\": 1000}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    VoteWeightWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    VoteWeightWrapper deserialized = objectMapper.readValue(
      serialized, VoteWeightWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableVoteWeightWrapper.class)
  @JsonDeserialize(as = ImmutableVoteWeightWrapper.class)
  interface VoteWeightWrapper {

    static VoteWeightWrapper of(VoteWeight voteWeight) {
      return ImmutableVoteWeightWrapper.builder().voteWeight(voteWeight).build();
    }

    VoteWeight voteWeight();

  }
}
