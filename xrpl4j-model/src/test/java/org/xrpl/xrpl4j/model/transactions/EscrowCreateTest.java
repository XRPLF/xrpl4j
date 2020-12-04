package org.xrpl.xrpl4j.model.transactions;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link EscrowCreate}.
 */
public class EscrowCreateTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testWithNeitherCancelNorFinish() {
    EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("account"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("destination"))
        .build();
  }

  @Test
  public void testCancelBeforeFinish() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(
        "If both CancelAfter and FinishAfter are specified, the FinishAfter time must be before the CancelAfter time."
    );

    EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("account"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("destination"))
        .cancelAfter(UnsignedLong.ONE)
        .finishAfter(UnsignedLong.valueOf(2L))
        .build();
  }

  @Test
  public void testCancelAfterFinish() {
    EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("account"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("destination"))
        .cancelAfter(UnsignedLong.valueOf(2L))
        .finishAfter(UnsignedLong.ONE)
        .build();
  }

  @Test
  public void testCancelEqualsFinish() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(
        "If both CancelAfter and FinishAfter are specified, the FinishAfter time must be before the CancelAfter time."
    );

    EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("account"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("destination"))
        .cancelAfter(UnsignedLong.ONE)
        .finishAfter(UnsignedLong.ONE)
        .build();
  }

}
