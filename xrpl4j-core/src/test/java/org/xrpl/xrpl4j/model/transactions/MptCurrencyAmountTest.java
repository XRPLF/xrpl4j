package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MptCurrencyAmount}.
 */
class MptCurrencyAmountTest {

  @Test
  void toStringIssuedCurrentAmount() {
    // Negative values.
    MptCurrencyAmount amount = MptCurrencyAmount.builder()
      .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
      .value("-1")
      .build();
    assertThat(amount.toString()).isEqualTo(
      "MptCurrencyAmount{" +
      "mptIssuanceId=ABCD, " +
      "value=-1" +
      "}"
    );
    assertThat(amount.isNegative()).isTrue();

    // Positive values
    amount = MptCurrencyAmount.builder()
      .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
      .value("1")
      .build();
    assertThat(amount.toString()).isEqualTo(
      "MptCurrencyAmount{" +
      "mptIssuanceId=ABCD, " +
      "value=1" +
      "}"
    );
    assertThat(amount.isNegative()).isFalse();

  }

  @Test
  void isNegative() {
    // Negative
    {
      final MptCurrencyAmount issuedCurrency = MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("-1")
        .build();
      assertThat(issuedCurrency.isNegative()).isTrue();
    }

    // Positive
    {
      final MptCurrencyAmount issuedCurrency = MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("1")
        .build();
      assertThat(issuedCurrency.isNegative()).isFalse();
    }

    // Zero
    {
      final MptCurrencyAmount amount = MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("0")
        .build();
      assertThat(amount.isNegative()).isFalse();
    }
  }

  @Test
  void unsignedLongValue() {
    // Negative
    assertThat(
      MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("-1")
        .build()
        .unsignedLongValue()
    ).isEqualTo(UnsignedLong.ONE);
    // Positive
    assertThat(
      MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("1")
        .build()
        .unsignedLongValue()
    ).isEqualTo(UnsignedLong.ONE);
  }

  @Test
  void builderWithUnsignedLong() {
    assertThat(
      MptCurrencyAmount.builder(UnsignedLong.ONE)
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .build()
    ).isEqualTo(
      MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("1")
        .build()
    );
  }
}