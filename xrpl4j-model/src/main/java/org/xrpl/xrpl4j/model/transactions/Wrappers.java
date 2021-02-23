package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Wrapped immutable classes for providing type-safe objects.
 */
public class Wrappers {

  /**
   * A wrapped {@link String} representing an address on the XRPL.
   */
  @Value.Immutable(intern = true)
  @Wrapped
  @JsonSerialize(as = Address.class)
  @JsonDeserialize(as = Address.class)
  abstract static class _Address extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} representing an X-Address on the XRPL.
   */
  @Value.Immutable(intern = true)
  @Wrapped
  @JsonSerialize(as = XAddress.class)
  @JsonDeserialize(as = XAddress.class)
  abstract static class _XAddress extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing the Hex representation of a 256-bit Hash.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = Hash256.class)
  @JsonDeserialize(as = Hash256.class)
  abstract static class _Hash256 extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    @Value.Check
    public void validateLength() {
      Preconditions.checkArgument(this.value().length() == 64, "Hash256 Strings must be 64 characters long.");
    }

    @Override
    public boolean equals(Object obj) {
      if (!Hash256.class.isAssignableFrom(obj.getClass())) {
        return false;
      }

      return this.value().toUpperCase(Locale.ENGLISH).equals(((Hash256) obj).value().toUpperCase(Locale.ENGLISH));
    }

    @Override
    public int hashCode() {
      return value().toUpperCase(Locale.ENGLISH).hashCode();
    }
  }

  /**
   * A {@link CurrencyAmount} for the XRP currency (non-issued). {@link XrpCurrencyAmount}s are a {@link String}
   * representation of an unsigned integer representing the amount in XRP drops.
   */
  @Value.Immutable(intern = true)
  @Wrapped
  @JsonSerialize(as = XrpCurrencyAmount.class)
  @JsonDeserialize(as = XrpCurrencyAmount.class)
  abstract static class _XrpCurrencyAmount extends Wrapper<UnsignedLong> implements Serializable, CurrencyAmount {

    static final long ONE_XRP_IN_DROPS = 1_000_000L;
    static final long MAX_XRP = 100_000_000_000L; // <-- per https://xrpl.org/rippleapi-reference.html#value
    static final long MAX_XRP_IN_DROPS = MAX_XRP * ONE_XRP_IN_DROPS;
    static final BigDecimal SMALLEST_XRP = new BigDecimal("0.000001");
    static final DecimalFormat FORMATTER = new DecimalFormat("###,###");

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops A long representing the number of drops of XRP of this amount.
     *
     * @return An {@link XrpCurrencyAmount} of {@code drops}.
     */
    public static XrpCurrencyAmount ofDrops(long drops) {
      return ofDrops(UnsignedLong.valueOf(drops));
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops An {@link UnsignedLong} representing the number of drops of XRP of this amount.
     *
     * @return An {@link XrpCurrencyAmount} of {@code drops}.
     */
    public static XrpCurrencyAmount ofDrops(UnsignedLong drops) {
      return XrpCurrencyAmount.of(drops);
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using decimal amount of XRP.
     *
     * @param amount A {@link BigDecimal} amount of XRP.
     *
     * @return An {@link XrpCurrencyAmount} of the amount of drops in {@code amount}.
     */
    public static XrpCurrencyAmount ofXrp(BigDecimal amount) {
      if (FluentCompareTo.is(amount).notEqualTo(BigDecimal.ZERO)) {
        Preconditions.checkArgument(FluentCompareTo.is(amount).greaterThanEqualTo(SMALLEST_XRP));
      }
      return ofDrops(UnsignedLong.valueOf(amount.scaleByPowerOfTen(6).toBigIntegerExact()));
    }

    /**
     * Convert this XRP amount into a decimal representing a value denominated in whole XRP units. For example, a value
     * of `1.0` represents 1 unit of XRP; a value of `0.5` represents a half of an XRP unit.
     *
     * @return A {@link BigDecimal} representing this value denominated in whole XRP units.
     */
    public BigDecimal toXrp() {
      return new BigDecimal(this.value().bigIntegerValue())
        .divide(BigDecimal.valueOf(ONE_XRP_IN_DROPS), MathContext.DECIMAL128);
    }

    /**
     * Adds another {@link XrpCurrencyAmount} to this amount.
     *
     * @param other An {@link XrpCurrencyAmount} to add to this.
     *
     * @return The sum of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount plus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().plus(other.value()));
    }

    /**
     * Subtract another {@link XrpCurrencyAmount} from this amount.
     *
     * @param other An {@link XrpCurrencyAmount} to subtract from this.
     *
     * @return The difference of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount minus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().minus(other.value()));
    }

    /**
     * Multiplies this amount by another {@link XrpCurrencyAmount}.
     *
     * @param other An {@link XrpCurrencyAmount} to multiply to this by.
     *
     * @return The product of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount times(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().times(other.value()));
    }

    @Override
    public String toString() {
      return this.value().toString();
    }

    @Value.Check
    protected void check() {
      Preconditions.checkState(
        FluentCompareTo.is(value()).lessThanOrEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS)),
        String.format(
          "XRP Amounts may not exceed %s drops (100B XRP, denominated in Drops)", FORMATTER.format(MAX_XRP_IN_DROPS))
      );
    }

  }

  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = Marker.class)
  @JsonDeserialize(as = Marker.class)
  abstract static class _Marker extends Wrapper<String> implements Serializable {

    @Override
    @JsonRawValue
    public String toString() {
      return this.value();
    }

  }
}
