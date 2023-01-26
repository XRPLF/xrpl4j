package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;

/**
 * A set of static {@link TransactionFlags} which can be set on
 * {@link PaymentChannelClaim} transactions.
 */
public class PaymentChannelClaimFlags extends TransactionFlags {

  /**
   * Constant {@link PaymentChannelClaimFlags} for the {@code tfRenew} flag.
   */
  protected static final PaymentChannelClaimFlags RENEW = new PaymentChannelClaimFlags(0x00010000);

  /**
   * Constant {@link PaymentChannelClaimFlags} for the {@code tfClose} flag.
   */
  protected static final PaymentChannelClaimFlags CLOSE = new PaymentChannelClaimFlags(0x00020000);

  private PaymentChannelClaimFlags(long value) {
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

  private static PaymentChannelClaimFlags of(boolean tfFullyCanonicalSig, boolean tfRenew, boolean tfClose) {
    return new PaymentChannelClaimFlags(
      TransactionFlags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfRenew ? RENEW : UNSET,
        tfClose ? CLOSE : UNSET
      ).getValue()
    );
  }

  /**
   * Construct {@link PaymentChannelClaimFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link PaymentChannelClaimFlags}.
   *
   * @return New {@link PaymentChannelClaimFlags}.
   */
  public static PaymentChannelClaimFlags of(long value) {
    return new PaymentChannelClaimFlags(value);
  }

  /**
   * Require a fully canonical transaction signature.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * Clear the {@link PayChannelObject#expiration()} time (different from {@link PayChannelObject#cancelAfter()}
   * time.) Only the source address of the payment channel can use this flag.
   *
   * @return {@code true} if {@code tfRenew} is set, otherwise {@code false}.
   */
  public boolean tfRenew() {
    return this.isSet(RENEW);
  }

  /**
   * Request to close the channel.
   *
   * <p>Only the {@link PayChannelObject#account()} and {@link PayChannelObject#destination()} addresses can use
   * this flag.</p>
   *
   * <p>This flag closes the channel immediately if it has no more XRP allocated to it after processing the
   * current claim, or if the {@link PayChannelObject#destination()} address uses it. If the source address uses
   * this flag when the channel still holds XRP, this schedules the channel to close after
   * {@link PayChannelObject#settleDelay()} seconds have passed. (Specifically, this sets the
   * {@link PayChannelObject#expiration()} of the channel to the close time of the previous ledger plus the channel's
   * {@link PayChannelObject#settleDelay()} time, unless the channel already has an earlier
   * {@link PayChannelObject#expiration()} time.)</p>
   *
   * <p>If the {@link PayChannelObject#destination()} address uses this flag when the channel still holds XRP,
   * any XRP that remains after processing the claim is returned to the source address.</p>
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfClose() {
    return this.isSet(CLOSE);
  }

  /**
   * A builder class for {@link PaymentChannelClaimFlags}.
   */
  public static class Builder {
    boolean tfFullyCanonicalSig = true;
    boolean tfRenew = false;
    boolean tfClose = false;

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
     * Set {@code tfRenew} to the given value.
     *
     * @param tfRenew A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfRenew(boolean tfRenew) {
      this.tfRenew = tfRenew;
      return this;
    }

    /**
     * Set {@code tfClose} to the given value.
     *
     * @param tfClose A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfClose(boolean tfClose) {
      this.tfClose = tfClose;
      return this;
    }

    /**
     * Build a new {@link PaymentChannelClaimFlags} from the current boolean values.
     *
     * @return A new {@link PaymentChannelClaimFlags}.
     */
    public PaymentChannelClaimFlags build() {
      return PaymentChannelClaimFlags.of(tfFullyCanonicalSig, tfRenew, tfClose);
    }
  }
}
