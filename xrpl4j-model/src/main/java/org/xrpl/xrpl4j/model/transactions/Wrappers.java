package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;
import java.math.BigDecimal;

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

      return this.value().toUpperCase().equals(((Hash256) obj).value().toUpperCase());
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

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops
     * @return
     */
    public static XrpCurrencyAmount ofDrops(long drops) {
      return ofDrops(UnsignedLong.valueOf(drops));
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops
     * @return
     */
    public static XrpCurrencyAmount ofDrops(UnsignedLong drops) {
      return XrpCurrencyAmount.of(drops);
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using decimal amount of XRP.
     *
     * @param amount
     * @return
     */
    public static XrpCurrencyAmount ofXrp(BigDecimal amount) {
      return ofDrops(amount.scaleByPowerOfTen(6).toBigIntegerExact().longValue());
    }

    @Override
    public String toString() {
      return this.value().toString();
    }

    public XrpCurrencyAmount plus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().plus(other.value()));
    }

    public XrpCurrencyAmount minus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().minus(other.value()));
    }

    public XrpCurrencyAmount times(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().times(other.value()));
    }
  }
}
