package org.xrpl.xrpl4j.model.client.fees;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

/**
 * Unit tests for {@link FeeOptions}.
 */
class FeeOptionsTest {

  @Test
  void feeOptionsLow() {
    FeeOptions feeOptions = FeeOptions.builder()
      .lowFee(XrpCurrencyAmount.ofDrops(1L))
      .mediumFee(XrpCurrencyAmount.ofDrops(2L))
      .highFee(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.valueOf(-1))
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(1L));
    assertThat(feeOptions.isTranactionQueueFull()).isFalse();
    assertThat(feeOptions.isTransactionQueueEmpty()).isTrue();

    feeOptions = FeeOptions.builder()
      .lowFee(XrpCurrencyAmount.ofDrops(1L))
      .mediumFee(XrpCurrencyAmount.ofDrops(2L))
      .highFee(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.ZERO)
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(1L));
    assertThat(feeOptions.isTranactionQueueFull()).isFalse();
    assertThat(feeOptions.isTransactionQueueEmpty()).isTrue();
  }

  @Test
  void feeOptionsMedium() {
    FeeOptions feeOptions = FeeOptions.builder()
      .lowFee(XrpCurrencyAmount.ofDrops(1L))
      .mediumFee(XrpCurrencyAmount.ofDrops(2L))
      .highFee(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.valueOf(0.5))
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(2L));
    assertThat(feeOptions.isTranactionQueueFull()).isFalse();
    assertThat(feeOptions.isTransactionQueueEmpty()).isFalse();
  }

  @Test
  void isTranactionQueueHigh() {
    FeeOptions feeOptions = FeeOptions.builder()
      .lowFee(XrpCurrencyAmount.ofDrops(1L))
      .mediumFee(XrpCurrencyAmount.ofDrops(2L))
      .highFee(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.ONE)
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(100L));
    assertThat(feeOptions.isTranactionQueueFull()).isTrue();
    assertThat(feeOptions.isTransactionQueueEmpty()).isFalse();

    feeOptions = FeeOptions.builder()
      .lowFee(XrpCurrencyAmount.ofDrops(1L))
      .mediumFee(XrpCurrencyAmount.ofDrops(2L))
      .highFee(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.valueOf(1.1))
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(100L));
    assertThat(feeOptions.isTranactionQueueFull()).isTrue();
    assertThat(feeOptions.isTransactionQueueEmpty()).isFalse();
  }
}