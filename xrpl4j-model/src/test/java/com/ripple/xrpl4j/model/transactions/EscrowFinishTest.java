package com.ripple.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.cryptoconditions.Fulfillment;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import com.ripple.xrpl4j.model.transactions.ImmutableEscrowFinish.Builder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link EscrowFinish}.
 */
public class EscrowFinishTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testNormalizeWithNoFulfillmentNoCondition() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .build();

    assertThat(actual.condition()).isNotPresent();
    assertThat(actual.fulfillment()).isNotPresent();
    assertThat(actual.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(1));
    assertThat(actual.account()).isEqualTo(Address.of("account"));
    assertThat(actual.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(actual.owner()).isEqualTo(Address.of("owner"));
    assertThat(actual.offerSequence()).isEqualTo(UnsignedInteger.ZERO);
  }

  @Test
  public void testNormalizeWithFulfillmentNoCondition() {
    expectedException.expect(IllegalStateException.class);
    expectedException
      .expectMessage("If a fulfillment is specified, the corresponding condition must also be specified.");

    Fulfillment fulfillment = PreimageSha256Fulfillment.from("ssh" .getBytes());

    EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      .build();

  }

  @Test
  public void testNormalizeWithNoFulfillmentAndCondition() {
    expectedException.expect(IllegalStateException.class);
    expectedException
      .expectMessage("If a condition is specified, the corresponding fulfillment must also be specified.");

    Fulfillment fulfillment = PreimageSha256Fulfillment.from("ssh" .getBytes());

    EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .condition(fulfillment.getDerivedCondition())
      .build();

  }

  @Test
  public void testNormalizeWithFulfillmentAndConditionButLowFee() {
    // We expect the

    Fulfillment fulfillment = PreimageSha256Fulfillment.from("ssh" .getBytes());

    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(330))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      .condition(fulfillment.getDerivedCondition())
      .build();

    assertThat(actual.condition()).isPresent();
    assertThat(actual.account()).isEqualTo(Address.of("account"));
    assertThat(actual.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(actual.owner()).isEqualTo(Address.of("owner"));
    assertThat(actual.offerSequence()).isEqualTo(UnsignedInteger.ZERO);

    assertThat(actual.fulfillment()).isPresent();
    assertThat(actual.fulfillment().get()).isEqualTo(fulfillment);
    assertThat(actual.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(330));
  }

  @Test
  public void testNormalizeWithFeeTooLow() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("If a fulfillment is specified, the fee must be set to 330 or greater.");

    Fulfillment fulfillment = PreimageSha256Fulfillment.from("ssh" .getBytes());
    EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      .condition(fulfillment.getDerivedCondition())
      .build();
  }

  @Test
  public void testNormalizeWithVariousFulfillmentSizes() {
    Builder builder = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO);

    // 0 bytes
    Fulfillment fulfillment = PreimageSha256Fulfillment.from(new byte[0]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(330));

    // 1 byte
    fulfillment = PreimageSha256Fulfillment.from(new byte[1]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(330));

    // 2 byte2
    fulfillment = PreimageSha256Fulfillment.from(new byte[2]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(330));

    // 15 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[15]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(330));

    // 16 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[16]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 10 drops for 16 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(340));

    // 17 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[17]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 10 drops for 16 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(340));

    // 31 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[31]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 10 drops for 16 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(340));

    // 32 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[32]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 20 drops for 32 bytes
    // (see https://xrpl.org/transaction-cost.html#fee-levels)
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(350));

    // 33 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[33]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 20 drops for 32 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(350));
  }

  @Test
  public void testComputeFee() {
    // 0 bytes
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[0])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(330));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[1])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(330));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[2])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(330));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[15])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(330));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[16])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(340));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[17])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(340));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[31])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(340));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[32])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(350));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[33])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(350));
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), PreimageSha256Fulfillment.from(new byte[64])))
      .isEqualTo(XrpCurrencyAmount.ofDrops(370));
  }

}
