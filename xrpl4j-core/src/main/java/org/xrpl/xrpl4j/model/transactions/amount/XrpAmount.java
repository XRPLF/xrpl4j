package org.xrpl.xrpl4j.model.transactions.amount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.jackson.modules.XrpAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.XrpAmountSerializer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * An {@link Amount} representing XRP, denominated in drops.
 *
 * <p>Construct via the typed static factories rather than the raw builder:
 * <pre>{@code
 *   XrpAmount amount = XrpAmount.ofDrops(1_000_000L);   // 1 XRP
 *   XrpAmount amount = XrpAmount.ofDrops(UnsignedLong.valueOf(1_000_000L));
 *   XrpAmount amount = XrpAmount.ofXrp(new BigDecimal("1.5"));   // 1.5 XRP
 * }</pre>
 *
 * <p>The {@link #value()} always returns a decimal-integer string representing the
 * drop count, prefixed with {@code -} if the amount is negative (which can occur in metadata returned by the RPC). For
 * example, {@code "1000000"} or {@code "-500"}.</p>
 *
 */
@Immutable
@JsonSerialize(using = XrpAmountSerializer.class)
@JsonDeserialize(using = XrpAmountDeserializer.class)
public interface XrpAmount extends Amount {

  /**
   * The number of drops in one XRP.
   */
  long ONE_XRP_IN_DROPS = 1_000_000L;

  /**
   * The maximum number of whole XRP.
   */
  long MAX_XRP = 100_000_000_000L; // per https://xrpl.org/rippleapi-reference.html#value

  /**
   * The maximum number of drops of XRP.
   */
  long MAX_XRP_IN_DROPS = MAX_XRP * ONE_XRP_IN_DROPS;

  /**
   * The smallest representable whole-XRP amount (1 drop = 0.000001 XRP).
   */
  BigDecimal MIN_XRP_BD = new BigDecimal("0.000001");

  /**
   * The maximum number of whole XRP, as a {@link BigDecimal}.
   */
  BigDecimal MAX_XRP_BD = BigDecimal.valueOf(MAX_XRP);

  /**
   * Construct an {@link XrpAmount} from a (possibly negative) number of drops.
   *
   * @param drops The number of drops. May be negative.
   *
   * @return An {@link XrpAmount}.
   */
  static XrpAmount ofDrops(final long drops) {
    return ImmutableXrpAmount.builder()
      .value(drops < 0 ? "-" + Math.abs(drops) : String.valueOf(drops))
      .build();
  }

  /**
   * Construct a non-negative {@link XrpAmount} from an {@link UnsignedLong} drop count.
   *
   * @param drops The number of drops. Must not be null.
   *
   * @return An {@link XrpAmount}.
   */
  static XrpAmount ofDrops(final UnsignedLong drops) {
    Objects.requireNonNull(drops, "drops must not be null");
    return ImmutableXrpAmount.builder()
      .value(drops.toString())
      .build();
  }

  /**
   * Construct an {@link XrpAmount} from a {@link BigDecimal} amount denominated in whole XRP units.
   *
   * <p>The absolute value must be at least {@link XrpAmount#MIN_XRP_BD} ({@code 0.000001}) and no greater
   * than {@link XrpAmount#MAX_XRP_BD} ({@code 100,000,000,000} XRP). Zero is also accepted.
   *
   * <pre>{@code
   *   XrpAmount amount = XrpAmount.ofXrp(new BigDecimal("1.5"));   // 1,500,000 drops
   * }</pre>
   *
   * @param amount A {@link BigDecimal} amount of XRP. Must not be null.
   *
   * @return An {@link XrpAmount} representing the equivalent number of drops.
   *
   * @throws IllegalArgumentException if the amount is out of range.
   */
  static XrpAmount ofXrp(final BigDecimal amount) {
    Objects.requireNonNull(amount, "amount must not be null");

    if (FluentCompareTo.is(amount).equalTo(BigDecimal.ZERO)) {
      return ofDrops(UnsignedLong.ZERO);
    }

    final BigDecimal absAmount = amount.abs();
    Preconditions.checkArgument(
      FluentCompareTo.is(absAmount).greaterThanEqualTo(XrpAmount.MIN_XRP_BD),
      String.format("Amount must be greater-than-or-equal-to %s XRP", XrpAmount.MIN_XRP_BD)
    );
    Preconditions.checkArgument(
      FluentCompareTo.is(absAmount).lessThanOrEqualTo(MAX_XRP_BD),
      String.format("Amount must be less-than-or-equal-to %s XRP", MAX_XRP_BD)
    );

    final boolean isNegative = amount.signum() < 0;
    final UnsignedLong drops = UnsignedLong.valueOf(absAmount.scaleByPowerOfTen(6).toBigIntegerExact());
    return ofDrops(isNegative ? -(drops.longValue()) : drops.longValue());
  }

  @Override
  @Derived
  @Auxiliary
  @JsonIgnore
  default boolean isNegative() {
    return Amount.super.isNegative();
  }

  /**
   * The absolute drop count as an {@link UnsignedLong} (sign stripped).
   *
   * @return An {@link UnsignedLong} representing the magnitude.
   */
  @Auxiliary
  @JsonIgnore
  default UnsignedLong unsignedLongValue() {
    return isNegative() ? UnsignedLong.valueOf(value().substring(1)) : UnsignedLong.valueOf(value());
  }

  /**
   * Converts this drop amount to a {@link BigDecimal} denominated in whole XRP units. For example, {@code 1_000_000}
   * drops returns {@code 1.0}.
   *
   * @return A {@link BigDecimal} in whole XRP.
   */
  @Auxiliary
  @JsonIgnore
  default BigDecimal toXrp() {
    final BigDecimal drops = new BigDecimal(unsignedLongValue().bigIntegerValue())
      .divide(BigDecimal.valueOf(1_000_000L), MathContext.DECIMAL128);
    return isNegative() ? drops.negate() : drops;
  }

  /**
   * Adds another {@link XrpAmount} to this amount.
   *
   * @param other An {@link XrpAmount} to add to this. Must not be null.
   *
   * @return The sum of this amount and {@code other}, as an {@link XrpAmount}.
   */
  @Auxiliary
  @JsonIgnore
  default XrpAmount plus(final XrpAmount other) {
    Objects.requireNonNull(other, "other must not be null");
    final long a = isNegative() ? -(unsignedLongValue().longValue()) : unsignedLongValue().longValue();
    final long b =
      other.isNegative() ? -(other.unsignedLongValue().longValue()) : other.unsignedLongValue().longValue();
    return ofDrops(a + b);
  }

  /**
   * Subtracts another {@link XrpAmount} from this amount.
   *
   * @param other An {@link XrpAmount} to subtract from this. Must not be null.
   *
   * @return The difference of this amount and {@code other}, as an {@link XrpAmount}.
   */
  @Auxiliary
  @JsonIgnore
  default XrpAmount minus(final XrpAmount other) {
    Objects.requireNonNull(other, "other must not be null");
    final long a = isNegative() ? -(unsignedLongValue().longValue()) : unsignedLongValue().longValue();
    final long b =
      other.isNegative() ? -(other.unsignedLongValue().longValue()) : other.unsignedLongValue().longValue();
    return ofDrops(a - b);
  }

  /**
   * Multiplies this amount by another {@link XrpAmount}.
   *
   * <p>The sign of the result follows standard multiplication rules: if exactly one operand is
   * negative the result is negative; if both operands share the same sign the result is positive.
   *
   * @param other An {@link XrpAmount} to multiply by. Must not be null.
   *
   * @return The product of this amount and {@code other}, as an {@link XrpAmount}.
   */
  @Auxiliary
  @JsonIgnore
  default XrpAmount times(final XrpAmount other) {
    Objects.requireNonNull(other, "other must not be null");
    final UnsignedLong product = unsignedLongValue().times(other.unsignedLongValue());
    // XOR: result is negative iff exactly one operand is negative.
    final boolean resultNegative = isNegative() != other.isNegative();
    return resultNegative ? ofDrops(-(product.longValue())) : ofDrops(product);
  }
}
