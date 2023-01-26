package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.transactions.OfferCreate;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link OfferCreate} transactions.
 */
public class OfferCreateFlags extends TransactionFlags {

  /**
   * Constant {@link OfferCreateFlags} for the {@code tfPassive} flag.
   */
  protected static final OfferCreateFlags PASSIVE = new OfferCreateFlags(0x00010000L);

  /**
   * Constant {@link OfferCreateFlags} for the {@code tfImmediateOrCancel} flag.
   */
  protected static final OfferCreateFlags IMMEDIATE_OR_CANCEL = new OfferCreateFlags(0x00020000L);

  /**
   * Constant {@link OfferCreateFlags} for the {@code tfFillOrKill} flag.
   */
  protected static final OfferCreateFlags FILL_OR_KILL = new OfferCreateFlags(0x00040000L);

  /**
   * Constant {@link OfferCreateFlags} for the {@code tfSell} flag.
   */
  protected static final OfferCreateFlags SELL = new OfferCreateFlags(0x00080000L);

  private OfferCreateFlags(long value) {
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
   * Construct {@link OfferCreateFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link OfferCreateFlags}.
   *
   * @return New {@link OfferCreateFlags}.
   */
  public static OfferCreateFlags of(long value) {
    return new OfferCreateFlags(value);
  }

  private static OfferCreateFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    long value = Flags.of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfPassive ? PASSIVE : UNSET,
      tfImmediateOrCancel ? IMMEDIATE_OR_CANCEL : UNSET,
      tfFillOrKill ? FILL_OR_KILL : UNSET,
      tfSell ? SELL : UNSET
    ).getValue();
    return new OfferCreateFlags(value);
  }

  /**
   * If enabled, the offer does not consume offers that exactly match it, and instead becomes an
   * Offer object in the ledger. It still consumes offers that cross it.
   *
   * @return {@code true} if {@code tfPassive} is set, otherwise {@code false}.
   */
  public boolean tfPassive() {
    return this.isSet(OfferCreateFlags.PASSIVE);
  }

  /**
   * Treat the offer as an Immediate or Cancel order . If enabled, the offer never becomes a ledger object:
   * it only tries to match existing offers in the ledger. If the offer cannot match any offers immediately,
   * it executes "successfully" without trading any currency. In this case, the transaction has the result code
   * tesSUCCESS, but creates no Offer objects in the ledger.
   *
   * @return {@code true} if {@code tfImmediateOrCancel} is set, otherwise {@code false}.
   */
  public boolean tfImmediateOrCancel() {
    return this.isSet(OfferCreateFlags.IMMEDIATE_OR_CANCEL);
  }

  /**
   * Treat the offer as a Fill or Kill order . Only try to match existing offers in the ledger, and only do so if
   * the entire TakerPays quantity can be obtained. If the fix1578 amendment is enabled and the offer cannot be
   * executed when placed, the transaction has the result code tecKILLED; otherwise, the transaction uses the result
   * code tesSUCCESS even when it was killed without trading any currency.
   *
   * @return {@code true} if {@code tfFillOrKill} is set, otherwise {@code false}.
   */
  public boolean tfFillOrKill() {
    return this.isSet(OfferCreateFlags.FILL_OR_KILL);
  }

  /**
   * Exchange the entire TakerGets amount, even if it means obtaining more than the TakerPays amount in exchange.
   *
   * @return {@code true} if {@code tfSell} is set, otherwise {@code false}.
   */
  public boolean tfSell() {
    return this.isSet(OfferCreateFlags.SELL);
  }


  /**
   * A builder class for {@link OfferCreateFlags} flags.
   */
  public static class Builder {

    private boolean tfFullyCanonicalSig = true;
    private boolean tfPassive = false;
    private boolean tfImmediateOrCancel = false;
    private boolean tfFillOrKill = false;
    private boolean tfSell = false;

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
     * Set {@code tfPassive} to the given value.
     *
     * @param tfPassive A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfPassive(boolean tfPassive) {
      this.tfPassive = tfPassive;
      return this;
    }

    /**
     * Set {@code tfImmediateOrCancel} to the given value.
     *
     * @param tfImmediateOrCancel A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfImmediateOrCancel(boolean tfImmediateOrCancel) {
      this.tfImmediateOrCancel = tfImmediateOrCancel;
      return this;
    }

    /**
     * Set {@code tfFillOrKill} to the given value.
     *
     * @param tfFillOrKill A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfFillOrKill(boolean tfFillOrKill) {
      this.tfFillOrKill = tfFillOrKill;
      return this;
    }

    /**
     * Set {@code tfSell} to the given value.
     *
     * @param tfSell A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSell(boolean tfSell) {
      this.tfSell = tfSell;
      return this;
    }

    /**
     * Build a new {@link OfferCreateFlags} from the current boolean values.
     *
     * @return A new {@link OfferCreateFlags}.
     */
    public OfferCreateFlags build() {
      return OfferCreateFlags.of(
        tfFullyCanonicalSig,
        tfPassive,
        tfImmediateOrCancel,
        tfFillOrKill,
        tfSell
      );
    }
  }
}
