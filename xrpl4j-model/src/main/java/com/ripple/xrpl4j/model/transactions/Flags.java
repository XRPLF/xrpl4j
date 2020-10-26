package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;

/**
 * A wrapper around a long value containing various XRPL Transaction Flags.
 *
 * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
 */
public class Flags {

  public static final Flags UNSET = new Flags(0);

  private final long value;

  private Flags(long value) {
    this.value = value;
  }

  public static Flags of(long value) {
    return new Flags(value);
  }

  public static Flags of(Flags flag, Flags... others) {
    return flag.bitwiseOr(
      Arrays.stream(others).reduce(Flags::bitwiseOr).orElse(UNSET)
    );
  }

  @JsonValue
  public long getValue() {
    return value;
  }

  /**
   * Performs a bitwise OR on this {@link Flags} and another {@link Flags}
   * @param other The {@link Flags} to perform the OR with.
   * @return The {@link Flags} resulting from the OR operation.
   */
  public Flags bitwiseOr(Flags other) {
    return Flags.of(this.value | other.value);
  }

  /**
   * Performs a bitwise AND on this {@link Flags} and another {@link Flags}
   * @param other The {@link Flags} to perform the AND with.
   * @return The {@link Flags} resulting from the AND operation.
   */
  public Flags bitwiseAnd(Flags other) {
    return Flags.of(this.value & other.value);
  }

  /**
   * Determines if a specific transaction flag is set by performing a bitwise AND on this {@link Flags} and the
   * {@link Flags} in question, and checking if the result of that operation is equal to the given flag.
   *
   * @param flag The {@link Flags} that this method determines is set or not.
   * @return true if the flag is set, false if not.
   */
  public boolean isSet(Flags flag) {
    return this.bitwiseAnd(flag).equals(flag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    Flags flags = (Flags) o;
    return getValue() == flags.getValue();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  /**
   * A set of static Universal {@link Flags} which could apply to any transaction.
   */
  public static class Universal extends Flags {

    public static final Universal FULLY_CANONICAL_SIG = new Universal(0x80000000L);

    private Universal(long value) {
      super(value);
    }

    /**
     * Flags indicating that a fully-canonical signature is required.
     * This flag is highly recommended.
     *
     * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
     */
    public boolean tfFullyCanonicalSig() {
      return this.isSet(Flags.Universal.FULLY_CANONICAL_SIG);
    }
  }

  /**
   * A set of static {@link Flags} which can be set on {@link com.ripple.xrpl4j.model.transactions.Payment} transactions.
   */
  public static class Payment extends Universal {

    public static final Payment NO_DIRECT_RIPPLE = new Payment(0x00010000L);
    public static final Payment PARTIAL_PAYMENT = new Payment(0x00020000L);
    public static final Payment LIMIT_QUALITY = new Payment(0x00040000L);

    /**
     * Flag indicated to only use paths included in the {@link com.ripple.xrpl4j.model.transactions.Payment#paths()} field.
     * This is intended to force the transaction to take arbitrage opportunities. Most clients do not need this.
     */
    public boolean tfNoDirectRipple() {
      return this.isSet(Flags.Payment.NO_DIRECT_RIPPLE);
    }

    /**
     * If the specified {@link com.ripple.xrpl4j.model.transactions.Payment#amount()} cannot be sent without spending more than {@link com.ripple.xrpl4j.model.transactions.Payment#sendMax()},
     * reduce the received amount instead of failing outright.
     *
     * @see "https://xrpl.org/partial-payments.html"
     */
    public boolean tfPartialPayment() {
      return this.isSet(Flags.Payment.PARTIAL_PAYMENT);
    }

    /**
     * Only take paths where all the conversions have an input:output ratio that is equal or better than the ratio of
     * {@link com.ripple.xrpl4j.model.transactions.Payment#amount()}:{@link com.ripple.xrpl4j.model.transactions.Payment#sendMax()}.
     * @return
     */
    public boolean tfLimitQuality() {
      return this.isSet(Flags.Payment.LIMIT_QUALITY);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static Payment of(long value) {
      return new Payment(value);
    }

    private static Payment of(boolean tfFullyCanonicalSig, boolean tfNoDirectRipple, boolean tfPartialPayment, boolean tfLimitQuality) {
      return new Payment(of(
        tfFullyCanonicalSig ? Universal.FULLY_CANONICAL_SIG : UNSET,
        tfNoDirectRipple ? NO_DIRECT_RIPPLE : UNSET,
        tfPartialPayment ? PARTIAL_PAYMENT : UNSET,
        tfLimitQuality ? LIMIT_QUALITY : UNSET
      ).getValue());
    }

    /**
     * A builder class for {@link Payment} flags.
     */
    public static class Builder {
      private boolean tfFullyCanonicalSig = true;
      private boolean tfNoDirectRipple = false;
      private boolean tfPartialPayment = false;
      private boolean tfLimitQuality = false;

      public Builder fullyCanonicalSig(boolean value) {
        this.tfFullyCanonicalSig = value;
        return this;
      }

      public Builder noDirectRipple(boolean value) {
        this.tfNoDirectRipple = value;
        return this;
      }

      public Builder partialPayment(boolean value) {
        this.tfPartialPayment = value;
        return this;
      }

      public Builder limitQuality(boolean value) {
        this.tfLimitQuality = value;
        return this;
      }

      public Payment build() {
        return Payment.of(tfFullyCanonicalSig, tfNoDirectRipple, tfPartialPayment, tfLimitQuality);
      }
    }

    private Payment(long value) {
      super(value);
    }
  }
}
