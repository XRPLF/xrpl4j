package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

/**
 * Unit tests for {@link TradingFee}.
 */
public class TradingFeeTest {

  private static final TradingFee TRADING_FEE = TradingFee.of(UnsignedInteger.ONE);
  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> TradingFee.of(null));
    assertThatThrownBy(() -> TradingFee.ofPercent(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void testEquality() {
    assertThat(TRADING_FEE).isEqualTo(TRADING_FEE);
    assertThat(TRADING_FEE).isNotEqualTo(new Object());
    assertThat(TRADING_FEE.equals(null)).isFalse();
  }

  @Test
  void fromUnsignedInteger() {
    assertThat(TRADING_FEE.toString()).isEqualTo("1");
    assertThat(TRADING_FEE.value()).isEqualTo(UnsignedInteger.ONE);
  }

  @Test
  void ofPercent() {
    TradingFee fee = TradingFee.ofPercent(BigDecimal.valueOf(0.99900));
    assertThat(fee.value()).isEqualTo(UnsignedInteger.valueOf(999));

    fee = TradingFee.ofPercent(BigDecimal.valueOf(1.0000));
    assertThat(fee.value()).isEqualTo(UnsignedInteger.valueOf(1000));

    fee = TradingFee.ofPercent(BigDecimal.valueOf(0));
    assertThat(fee.value()).isEqualTo(UnsignedInteger.valueOf(0));

    fee = TradingFee.ofPercent(BigDecimal.valueOf(0.00000000000));
    assertThat(fee.value()).isEqualTo(UnsignedInteger.valueOf(0));
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

    percent = BigDecimal.valueOf(1, 3);
    fee = TradingFee.ofPercent(percent);
    assertThat(fee.bigDecimalValue()).isEqualTo(percent);
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    TradingFee tradingFee = TradingFee.of(UnsignedInteger.valueOf(1000));
    TradingFeeWrapper wrapper = TradingFeeWrapper.of(tradingFee);

    String json = "{\"tradingFee\": 1000}";
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
