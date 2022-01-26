package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransferFeeTest {

  @Test
  public void transferFeeEquality() {

    assertThat(TransferFee.of(UnsignedInteger.ONE).equals(TransferFee.of(UnsignedInteger.ONE)));
    assertThat(TransferFee.of(UnsignedInteger.valueOf(10)).equals(TransferFee.of(UnsignedInteger.valueOf(10))));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(99.99)))
      .isEqualTo(TransferFee.ofPercent(BigDecimal.valueOf(99.99)));
  }
}
