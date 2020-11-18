package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.model.transactions.immutables.Wrapped;
import com.ripple.xrpl4j.model.transactions.immutables.Wrapper;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.io.Serializable;
import java.math.BigInteger;

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
  static abstract class _Address extends Wrapper<String> implements Serializable {

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
  static abstract class _Hash256 extends Wrapper<String> implements Serializable {

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
  static abstract class _XrpCurrencyAmount extends Wrapper<String> implements Serializable, CurrencyAmount {

    /**
     * Construct a new immutable {@code XrpCurrencyAmount} instance.
     *
     * @param value The value for the {@code value} attribute
     * @return An immutable XrpCurrencyAmount instance
     */
    public static XrpCurrencyAmount of(int value) {
      return XrpCurrencyAmount.of(value + "");
    }

    @Override
    public String toString() {
      return this.value();
    }

    @Derived
    public BigInteger asBigInteger() {
      try {
        return BigInteger.valueOf(Long.parseLong(this.value()));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("XrpCurrencyAmount must be a whole number in drops.");
      }
    }

    @Value.Check
    void check() {
      try {
        asBigInteger();
      } catch (Exception e) {
        throw new IllegalStateException("Fee must be an integer number in drops.");
      }
    }
  }
}
