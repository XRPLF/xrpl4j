package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

public class TransferFeeTests {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    TransferFee transferFee = TransferFee.of(UnsignedInteger.valueOf(1000));
    TransferFeeTests.TransferFeeWrapper wrapper = TransferFeeTests.TransferFeeWrapper.of(transferFee);

    String json = "{\"transferFee\": \"1000\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    TransferFeeTests.TransferFeeWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    TransferFeeTests.TransferFeeWrapper deserialized = objectMapper.readValue(
      serialized, TransferFeeTests.TransferFeeWrapper.class
    );
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTransferFeeWrapper.class)
  @JsonDeserialize(as = ImmutableTransferFeeWrapper.class)
  interface TransferFeeWrapper {

    static TransferFeeTests.TransferFeeWrapper of(TransferFee transferFee) {
      return ImmutableTransferFeeWrapper.builder().transferFee(transferFee).build();
    }

    TransferFee transferFee();

  }
}
