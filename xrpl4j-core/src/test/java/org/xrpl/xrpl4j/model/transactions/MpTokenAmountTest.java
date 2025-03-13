package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MpTokenAmount}.
 */
class MpTokenAmountTest {

  @Test
  void toStringIssuedCurrentAmount() {
    // Negative values.
    MpTokenAmount amount = MpTokenAmount.builder()
      .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
      .value("-1")
      .build();
    assertThat(amount.toString()).isEqualTo(
      "MpTokenAmount{" +
      "mptIssuanceId=ABCD, " +
      "value=-1" +
      "}"
    );
    assertThat(amount.isNegative()).isTrue();

    // Positive values
    amount = MpTokenAmount.builder()
      .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
      .value("1")
      .build();
    assertThat(amount.toString()).isEqualTo(
      "MpTokenAmount{" +
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
      final MpTokenAmount issuedCurrency = MpTokenAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("-1")
        .build();
      assertThat(issuedCurrency.isNegative()).isTrue();
    }

    // Positive
    {
      final MpTokenAmount issuedCurrency = MpTokenAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("1")
        .build();
      assertThat(issuedCurrency.isNegative()).isFalse();
    }

    // Zero
    {
      final MpTokenAmount amount = MpTokenAmount.builder()
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
      MpTokenAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("-1")
        .build()
        .unsignedLongValue()
    ).isEqualTo(UnsignedLong.ONE);
    // Positive
    assertThat(
      MpTokenAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("ABCD"))
        .value("1")
        .build()
        .unsignedLongValue()
    ).isEqualTo(UnsignedLong.ONE);
  }
}