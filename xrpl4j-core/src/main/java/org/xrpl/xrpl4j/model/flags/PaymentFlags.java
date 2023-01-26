package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.transactions.Payment;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link Payment} transactions.
 */
public class PaymentFlags extends TransactionFlags {

  /**
   * Constant {@link PaymentFlags} for an unset flag.
   */
  public static final PaymentFlags UNSET = new PaymentFlags(0);

  /**
   * Constant {@link PaymentFlags} for the {@code tfNoDirectRipple} flag.
   */
  protected static final PaymentFlags NO_DIRECT_RIPPLE = new PaymentFlags(0x00010000L);

  /**
   * Constant {@link PaymentFlags} for the {@code tfPartialPayment} flag.
   */
  protected static final PaymentFlags PARTIAL_PAYMENT = new PaymentFlags(0x00020000L);

  /**
   * Constant {@link PaymentFlags} for the {@code tfLimitQuality} flag.
   */
  protected static final PaymentFlags LIMIT_QUALITY = new PaymentFlags(0x00040000L);

  private PaymentFlags(long value) {
    super(value);
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Construct {@link PaymentFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link PaymentFlags}.
   *
   * @return New {@link PaymentFlags}.
   */
  public static PaymentFlags of(long value) {
    return new PaymentFlags(value);
  }

  private static PaymentFlags of(boolean tfFullyCanonicalSig, boolean tfNoDirectRipple, boolean tfPartialPayment,
                                                             boolean tfLimitQuality) {
    return new PaymentFlags(of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfNoDirectRipple ? NO_DIRECT_RIPPLE : UNSET,
      tfPartialPayment ? PARTIAL_PAYMENT : UNSET,
      tfLimitQuality ? LIMIT_QUALITY : UNSET
    ).getValue());
  }

  /**
   * Do not use the default path; only use paths included in the {@link Payment#paths()} field. This is intended
   * to force the transaction to take arbitrage opportunities. Most clients do not need this.
   *
   * @return {@code true} if {@code tfNoDirectRipple} is set, otherwise {@code false}.
   */
  public boolean tfNoDirectRipple() {
    return this.isSet(PaymentFlags.NO_DIRECT_RIPPLE);
  }

  /**
   * If the specified {@link Payment#amount()} cannot be sent without spending
   * more than {@link Payment#sendMax()}, reduce the received amount instead of
   * failing outright.
   *
   * @return {@code true} if {@code tfPartialPayment} is set, otherwise {@code false}.
   * @see "https://xrpl.org/partial-payments.html"
   */
  public boolean tfPartialPayment() {
    return this.isSet(PaymentFlags.PARTIAL_PAYMENT);
  }

  /**
   * Only take paths where all the conversions have an input:output ratio that is equal or better than the ratio of
   * {@link Payment#amount()}:{@link Payment#sendMax()}.
   *
   * @return {@code true} if {@code tfLimitQuality} is set, otherwise {@code false}.
   */
  public boolean tfLimitQuality() {
    return this.isSet(PaymentFlags.LIMIT_QUALITY);
  }

  /**
   * A builder class for {@link PaymentFlags} flags.
   */
  public static class Builder {

    private boolean tfFullyCanonicalSig = true;
    private boolean tfNoDirectRipple = false;
    private boolean tfPartialPayment = false;
    private boolean tfLimitQuality = false;


    /**
     * Set {@code tfFullyCanonicalSig} to the given value.
     *
     * @param tfFullyCanonicalSig A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
      this.tfFullyCanonicalSig = tfFullyCanonicalSig;
      return this;
    }

    /**
     * Set {@code tfNoDirectRipple} to the given value.
     *
     * @param tfNoDirectRipple A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfNoDirectRipple(boolean tfNoDirectRipple) {
      this.tfNoDirectRipple = tfNoDirectRipple;
      return this;
    }

    /**
     * Set {@code tfPartialPayment} to the given value.
     *
     * @param tfPartialPayment A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfPartialPayment(boolean tfPartialPayment) {
      this.tfPartialPayment = tfPartialPayment;
      return this;
    }

    /**
     * Set {@code tfLimitQuality} to the given value.
     *
     * @param tfLimitQuality A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLimitQuality(boolean tfLimitQuality) {
      this.tfLimitQuality = tfLimitQuality;
      return this;
    }

    /**
     * Build a new {@link PaymentFlags} from the current boolean values.
     *
     * @return A new {@link PaymentFlags}.
     */
    public PaymentFlags build() {
      return PaymentFlags.of(tfFullyCanonicalSig, tfNoDirectRipple, tfPartialPayment, tfLimitQuality);
    }
  }
}
