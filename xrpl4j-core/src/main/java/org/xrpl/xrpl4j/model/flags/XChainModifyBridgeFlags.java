package org.xrpl.xrpl4j.model.flags;

public class XChainModifyBridgeFlags extends TransactionFlags {

  /**
   * Constant {@link XChainModifyBridgeFlags} for an unset flag.
   */
  public static final XChainModifyBridgeFlags UNSET = new XChainModifyBridgeFlags(0);

  /**
   * Constant {@link XChainModifyBridgeFlags} for the {@code tfClearAccountCreateAmount} flag.
   */
  public static final XChainModifyBridgeFlags CLEAR_ACCOUNT_CREATE_AMOUNT = new XChainModifyBridgeFlags(0x00010000);

  private XChainModifyBridgeFlags(long value) {
    super(value);
  }

  private XChainModifyBridgeFlags() {

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
   * Construct {@link XChainModifyBridgeFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link XChainModifyBridgeFlags}.
   *
   * @return New {@link XChainModifyBridgeFlags}.
   */
  public static XChainModifyBridgeFlags of(long value) {
    return new XChainModifyBridgeFlags(value);
  }

  private static XChainModifyBridgeFlags of(boolean tfFullyCanonicalSig, boolean tfClearAccountCreateAmount) {
    return new XChainModifyBridgeFlags(of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfClearAccountCreateAmount ? CLEAR_ACCOUNT_CREATE_AMOUNT : UNSET
    ).getValue());
  }

  /**
   * Construct an empty instance of {@link XChainModifyBridgeFlags}. Transactions with empty flags will
   * not be serialized with a {@code Flags} field.
   *
   * @return An empty {@link XChainModifyBridgeFlags}.
   */
  public static XChainModifyBridgeFlags empty() {
    return new XChainModifyBridgeFlags();
  }

  /**
   * Clears the MinAccountCreateAmount of the bridge.
   *
   * @return {@code true} if {@code tfClearAccountCreateAmount} is set, otherwise {@code false}.
   */
  public boolean tfClearAccountCreateAmount() {
    return this.isSet(XChainModifyBridgeFlags.CLEAR_ACCOUNT_CREATE_AMOUNT);
  }

  /**
   * A builder class for {@link XChainModifyBridgeFlags} flags.
   */
  public static class Builder {

    private boolean tfClearAccountCreateAmount = false;

    /**
     * Set {@code tfClearAccountCreateAmount} to the given value.
     *
     * @param tfClearAccountCreateAmount A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfClearAccountCreateAmount(boolean tfClearAccountCreateAmount) {
      this.tfClearAccountCreateAmount = tfClearAccountCreateAmount;
      return this;
    }

    /**
     * Build a new {@link XChainModifyBridgeFlags} from the current boolean values.
     *
     * @return A new {@link XChainModifyBridgeFlags}.
     */
    public XChainModifyBridgeFlags build() {
      return XChainModifyBridgeFlags.of(true, tfClearAccountCreateAmount);
    }
  }
}
