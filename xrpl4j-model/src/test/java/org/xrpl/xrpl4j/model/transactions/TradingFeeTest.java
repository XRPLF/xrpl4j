package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

public class TradingFeeTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void fromUnsignedInteger() {
    TradingFee fee = TradingFee.of(UnsignedInteger.ONE);
    assertThat(fee.toString()).isEqualTo("1");
    assertThat(fee.value()).isEqualTo(UnsignedInteger.ONE);
  }

  @Test
  void ofPercent() {
    TradingFee fee = TradingFee.ofPercent(BigDecimal.valueOf(0.99900));
    assertThat(fee.value()).isEqualTo(UnsignedInteger.valueOf(999));

    fee = TradingFee.ofPercent(BigDecimal.valueOf(1.0000));
    assertThat(fee.value()).isEqualTo(UnsignedInteger.valueOf(1000));
  }

  @Test
  void ofPercentWithTooManyDecimalPlaces() {
    assertThatThrownBy(
      () -> TradingFee.ofPercent(BigDecimal.valueOf(0.0001))
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Percent value should have a maximum of 3 decimal places.");

    assertThatThrownBy(
      () -> TradingFee.ofPercent(BigDecimal.valueOf(0.0001000))
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Percent value should have a maximum of 3 decimal places.");
  }

  @Test
  void bigDecimalValue() {
    BigDecimal percent = BigDecimal.valueOf(0.001000);
    TradingFee fee = TradingFee.ofPercent(percent);
    assertThat(fee.bigDecimalValue()).isEqualTo(percent);
  }

  @Test
  void validateBounds() {
    assertThatThrownBy(
      () -> TradingFee.of(UnsignedInteger.valueOf(1001))
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("TradingFee should be in the range 0 to 1000.");

    assertDoesNotThrow(() -> TradingFee.of(UnsignedInteger.valueOf(1000)));
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    TradingFee tradingFee = TradingFee.of(UnsignedInteger.valueOf(1000));
    TradingFeeWrapper wrapper = TradingFeeWrapper.of(tradingFee);

    String json = "{\"tradingFee\": \"1000\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    TradingFeeWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    TradingFeeWrapper deserialized = objectMapper.readValue(
      serialized, TradingFeeWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTradingFeeWrapper.class)
  @JsonDeserialize(as = ImmutableTradingFeeWrapper.class)
  interface TradingFeeWrapper {

    static TradingFeeWrapper of(TradingFee tradingFee) {
      return ImmutableTradingFeeWrapper.builder().tradingFee(tradingFee).build();
    }

    TradingFee tradingFee();

  }
}
