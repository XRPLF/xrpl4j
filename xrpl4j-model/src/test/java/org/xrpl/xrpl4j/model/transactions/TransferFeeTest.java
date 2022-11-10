package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransferFeeTest {

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
    assertDoesNotThrow(() -> TransferFee.of(UnsignedInteger.valueOf(10000)));

    assertThatThrownBy(() -> TransferFee.of(UnsignedInteger.valueOf(50001)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("TransferFee should be in the range 0 to 50000.");
  }
}
