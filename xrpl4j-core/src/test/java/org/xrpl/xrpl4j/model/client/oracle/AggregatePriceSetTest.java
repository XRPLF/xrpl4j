package org.xrpl.xrpl4j.model.client.oracle;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class AggregatePriceSetTest {

  @Test
  void testBigDecimalFields() {
    AggregatePriceSet set = AggregatePriceSet.builder()
      .meanString("1234.5678")
      .size(UnsignedLong.ONE)
      .standardDeviationString("345678.23496")
      .build();

    assertThat(set.mean()).isEqualTo(BigDecimal.valueOf(1234.5678));
    assertThat(set.standardDeviation()).isEqualTo(BigDecimal.valueOf(345678.23496));
  }
}