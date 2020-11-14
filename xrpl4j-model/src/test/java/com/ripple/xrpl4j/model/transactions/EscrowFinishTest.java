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
      .fee(XrpCurrencyAmount.of("1"))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .build();

    assertThat(actual.condition()).isNotPresent();
    assertThat(actual.fulfillment()).isNotPresent();
    assertThat(actual.fee()).isEqualTo(XrpCurrencyAmount.of("1"));
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
      .fee(XrpCurrencyAmount.of("1"))
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
      .fee(XrpCurrencyAmount.of("1"))
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
      .fee(XrpCurrencyAmount.of("330"))
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
    assertThat(actual.fee()).isEqualTo(XrpCurrencyAmount.of("330"));
  }

  @Test
  public void testNormalizeWithFeeTooLow() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("If a fulfillment is specified, the fee must be set to 330 or greater.");

    Fulfillment fulfillment = PreimageSha256Fulfillment.from("ssh" .getBytes());
    EscrowFinish.builder()
      .fee(XrpCurrencyAmount.of("1"))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      .condition(fulfillment.getDerivedCondition())
      .build();
  }

  @Test
  public void testNormalizeWithInvalidFee() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("XrpCurrencyAmount must be a whole number in drops.");

    Fulfillment fulfillment = PreimageSha256Fulfillment.from("ssh" .getBytes());
    EscrowFinish.builder()
      .fee(XrpCurrencyAmount.of("")) // <-- empty fee is invalid.
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
      .fee(XrpCurrencyAmount.of("1"))
      .account(Address.of("account"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("owner"))
      .offerSequence(UnsignedInteger.ZERO);

    // 0 bytes
    Fulfillment fulfillment = PreimageSha256Fulfillment.from(new byte[0]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("330"));

    // 1 byte
    fulfillment = PreimageSha256Fulfillment.from(new byte[1]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("330"));

    // 2 byte2
    fulfillment = PreimageSha256Fulfillment.from(new byte[2]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("330"));

    // 15 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[15]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("330"));

    // 16 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[16]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 10 drops for 16 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("340"));

    // 17 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[17]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 10 drops for 16 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("340"));

    // 31 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[31]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 10 drops for 16 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("340"));

    // 32 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[32]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 20 drops for 32 bytes
    // (see https://xrpl.org/transaction-cost.html#fee-levels)
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("350"));

    // 33 bytes
    fulfillment = PreimageSha256Fulfillment.from(new byte[33]);
    builder.fulfillment(fulfillment);
    builder.fee(XrpCurrencyAmount.of(EscrowFinish.computeFee(XrpCurrencyAmount.of(10), fulfillment) + ""));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard 10 drop fee, plus 320 drops + 20 drops for 32 bytes
    assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.of("350"));
  }

  @Test
  public void testComputeFee() {
    // 0 bytes
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[0])).asBigInteger())
      .isEqualTo(330);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[1])).asBigInteger())
      .isEqualTo(330);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[2])).asBigInteger())
      .isEqualTo(330);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[15])).asBigInteger())
      .isEqualTo(330);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[16])).asBigInteger())
      .isEqualTo(340);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[17])).asBigInteger())
      .isEqualTo(340);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[31])).asBigInteger())
      .isEqualTo(340);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[32])).asBigInteger())
      .isEqualTo(350);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[33])).asBigInteger())
      .isEqualTo(350);
    assertThat(
      EscrowFinish.computeFee(XrpCurrencyAmount.of(10), PreimageSha256Fulfillment.from(new byte[64])).asBigInteger())
      .isEqualTo(370);
  }

}
