package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

public class TransferFeeTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void transferFeeEquality() {

    assertThat(TransferFee.of(UnsignedInteger.ONE)).isEqualTo(TransferFee.of(UnsignedInteger.ONE));
    assertThat(TransferFee.of(UnsignedInteger.valueOf(10)))
      .isEqualTo(TransferFee.of(UnsignedInteger.valueOf(10)));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(99.99)))
      .isEqualTo(TransferFee.ofPercent(BigDecimal.valueOf(99.99)));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(99.9)))
      .isEqualTo(TransferFee.ofPercent(BigDecimal.valueOf(99.90)));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(99.9)).value())
      .isEqualTo(UnsignedInteger.valueOf(9990));
  }

  @Test
  public void percentValueIncorrectFormat() {
    assertThrows(
      IllegalArgumentException.class,
      () -> TransferFee.ofPercent(BigDecimal.valueOf(99.999)),
      "Percent value should have a maximum of 2 decimal places."
    );
  }

  @Test
  public void validateBounds() {
    assertDoesNotThrow(() -> TransferFee.of(UnsignedInteger.valueOf(49999)));
    assertDoesNotThrow(() -> TransferFee.of(UnsignedInteger.valueOf(50000)));

    assertThatThrownBy(() -> TransferFee.of(UnsignedInteger.valueOf(50001)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("TransferFee should be in the range 0 to 50000.");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    TransferFee transferFee = TransferFee.of(UnsignedInteger.valueOf(1000));
    TransferFeeWrapper wrapper = TransferFeeWrapper.of(transferFee);

    String json = "{\"transferFee\": \"1000\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    TransferFeeWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    TransferFeeWrapper deserialized = objectMapper.readValue(
      serialized, TransferFeeWrapper.class
    );
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTransferFeeWrapper.class)
  @JsonDeserialize(as = ImmutableTransferFeeWrapper.class)
  interface TransferFeeWrapper {

    static TransferFeeWrapper of(TransferFee transferFee) {
      return ImmutableTransferFeeWrapper.builder().transferFee(transferFee).build();
    }

    TransferFee transferFee();

  }
}
