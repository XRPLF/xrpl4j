package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.model.transactions.immutables.Wrapped;
import com.ripple.xrpl4j.model.transactions.immutables.Wrapper;
import org.immutables.value.Value;

import java.io.Serializable;

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
  @Value.Immutable(intern = true)
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
      Preconditions.checkArgument(this.value().length() == 32, "Hash256 Strings must be 32 bytes long.");
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

    @Override
    public String toString() {
      return this.value();
    }
  }
}
