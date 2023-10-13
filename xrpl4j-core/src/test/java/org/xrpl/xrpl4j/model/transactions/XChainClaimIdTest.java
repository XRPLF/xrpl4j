package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigDecimal;

public class XChainClaimIdTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    XChainClaimId claimId = XChainClaimId.of(UnsignedLong.ZERO);
    assertThat(claimId.toString()).isEqualTo("0");

    XChainClaimId claimIdMax = XChainClaimId.of(UnsignedLong.MAX_VALUE);
    assertThat(claimIdMax.toString()).isEqualTo("18446744073709551615");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    XChainClaimId claimId = XChainClaimId.of(UnsignedLong.valueOf(1000));
    XChainClaimIdWrapper wrapper = XChainClaimIdWrapper.of(claimId);

    String json = "{\"claimId\": \"1000\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testMaxJson() throws JSONException, JsonProcessingException {
    XChainClaimId claimId = XChainClaimId.of(UnsignedLong.MAX_VALUE);
    XChainClaimIdWrapper wrapper = XChainClaimIdWrapper.of(claimId);

    String json = "{\"claimId\": \"18446744073709551615\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    XChainClaimIdWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    XChainClaimIdWrapper deserialized = objectMapper.readValue(
      serialized, XChainClaimIdWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableXChainClaimIdWrapper.class)
  @JsonDeserialize(as = ImmutableXChainClaimIdWrapper.class)
  interface XChainClaimIdWrapper {

    static XChainClaimIdWrapper of(XChainClaimId claimId) {
      return ImmutableXChainClaimIdWrapper.builder().claimId(claimId).build();
    }

    XChainClaimId claimId();

  }
}
