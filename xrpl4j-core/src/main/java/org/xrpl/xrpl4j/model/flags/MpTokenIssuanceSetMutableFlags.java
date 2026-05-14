package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set in the {@code MutableFlags} field of
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet} transactions.
 *
 * <p>These flags (prefixed with {@code tmf}) set or clear specific flags on an existing
 * {@code MPTokenIssuance} that were declared mutable at creation time.
 *
 * @see <a href="https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0094-dynamic-MPT">XLS-94</a>
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceSetMutableFlags extends Flags {

  /**
   * Sets the {@code lsfMPTCanLock} flag. Enables the token to be locked both individually and globally.
   * Hex: {@code 0x00000001}.
   */
  public static final MpTokenIssuanceSetMutableFlags SET_CAN_LOCK =
    new MpTokenIssuanceSetMutableFlags(0x00000001);

  /**
   * Clears the {@code lsfMPTCanLock} flag. Disables both individual and global locking of the token.
   * Hex: {@code 0x00000002}.
   */
  public static final MpTokenIssuanceSetMutableFlags CLEAR_CAN_LOCK =
    new MpTokenIssuanceSetMutableFlags(0x00000002);

  /**
   * Sets the {@code lsfMPTRequireAuth} flag. Requires individual holders to be authorized.
   * Hex: {@code 0x00000004}.
   */
  public static final MpTokenIssuanceSetMutableFlags SET_REQUIRE_AUTH =
    new MpTokenIssuanceSetMutableFlags(0x00000004);

  /**
   * Clears the {@code lsfMPTRequireAuth} flag. Holders are not required to be authorized.
   * Hex: {@code 0x00000008}.
   */
  public static final MpTokenIssuanceSetMutableFlags CLEAR_REQUIRE_AUTH =
    new MpTokenIssuanceSetMutableFlags(0x00000008);

  /**
   * Sets the {@code lsfMPTCanEscrow} flag. Allows holders to place balances into escrow.
   * Hex: {@code 0x00000010}.
   */
  public static final MpTokenIssuanceSetMutableFlags SET_CAN_ESCROW =
    new MpTokenIssuanceSetMutableFlags(0x00000010);

  /**
   * Clears the {@code lsfMPTCanEscrow} flag. Disallows holders from placing balances into escrow.
   * Hex: {@code 0x00000020}.
   */
  public static final MpTokenIssuanceSetMutableFlags CLEAR_CAN_ESCROW =
    new MpTokenIssuanceSetMutableFlags(0x00000020);

  /**
   * Sets the {@code lsfMPTCanTrade} flag. Allows holders to trade balances on the XRPL DEX.
   * Hex: {@code 0x00000040}.
   */
  public static final MpTokenIssuanceSetMutableFlags SET_CAN_TRADE =
    new MpTokenIssuanceSetMutableFlags(0x00000040);

  /**
   * Clears the {@code lsfMPTCanTrade} flag. Disallows holders from trading balances on the XRPL DEX.
   * Hex: {@code 0x00000080}.
   */
  public static final MpTokenIssuanceSetMutableFlags CLEAR_CAN_TRADE =
    new MpTokenIssuanceSetMutableFlags(0x00000080);

  /**
   * Sets the {@code lsfMPTCanTransfer} flag. Allows tokens to be transferred to non-issuer accounts.
   * Hex: {@code 0x00000100}.
   */
  public static final MpTokenIssuanceSetMutableFlags SET_CAN_TRANSFER =
    new MpTokenIssuanceSetMutableFlags(0x00000100);

  /**
   * Clears the {@code lsfMPTCanTransfer} flag. Disallows transfers to non-issuer accounts.
   * Hex: {@code 0x00000200}.
   */
  public static final MpTokenIssuanceSetMutableFlags CLEAR_CAN_TRANSFER =
    new MpTokenIssuanceSetMutableFlags(0x00000200);

  /**
   * Sets the {@code lsfMPTCanClawback} flag. Enables the issuer to claw back tokens.
   * Hex: {@code 0x00000400}.
   */
  public static final MpTokenIssuanceSetMutableFlags SET_CAN_CLAWBACK =
    new MpTokenIssuanceSetMutableFlags(0x00000400);

  /**
   * Clears the {@code lsfMPTCanClawback} flag. The token cannot be clawed back.
   * Hex: {@code 0x00000800}.
   */
  public static final MpTokenIssuanceSetMutableFlags CLEAR_CAN_CLAWBACK =
    new MpTokenIssuanceSetMutableFlags(0x00000800);

  private MpTokenIssuanceSetMutableFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceSetMutableFlags() {
  }

  /**
   * Construct {@link MpTokenIssuanceSetMutableFlags} for the given raw value.
   *
   * @param value The long-number encoded flags value.
   *
   * @return A new {@link MpTokenIssuanceSetMutableFlags}.
   */
  public static MpTokenIssuanceSetMutableFlags of(long value) {
    return new MpTokenIssuanceSetMutableFlags(value);
  }

  private static MpTokenIssuanceSetMutableFlags of(
    boolean tmfMPTSetCanLock,
    boolean tmfMPTClearCanLock,
    boolean tmfMPTSetRequireAuth,
    boolean tmfMPTClearRequireAuth,
    boolean tmfMPTSetCanEscrow,
    boolean tmfMPTClearCanEscrow,
    boolean tmfMPTSetCanTrade,
    boolean tmfMPTClearCanTrade,
    boolean tmfMPTSetCanTransfer,
    boolean tmfMPTClearCanTransfer,
    boolean tmfMPTSetCanClawback,
    boolean tmfMPTClearCanClawback
  ) {
    return new MpTokenIssuanceSetMutableFlags(
      Flags.of(
        tmfMPTSetCanLock ? SET_CAN_LOCK : UNSET,
        tmfMPTClearCanLock ? CLEAR_CAN_LOCK : UNSET,
        tmfMPTSetRequireAuth ? SET_REQUIRE_AUTH : UNSET,
        tmfMPTClearRequireAuth ? CLEAR_REQUIRE_AUTH : UNSET,
        tmfMPTSetCanEscrow ? SET_CAN_ESCROW : UNSET,
        tmfMPTClearCanEscrow ? CLEAR_CAN_ESCROW : UNSET,
        tmfMPTSetCanTrade ? SET_CAN_TRADE : UNSET,
        tmfMPTClearCanTrade ? CLEAR_CAN_TRADE : UNSET,
        tmfMPTSetCanTransfer ? SET_CAN_TRANSFER : UNSET,
        tmfMPTClearCanTransfer ? CLEAR_CAN_TRANSFER : UNSET,
        tmfMPTSetCanClawback ? SET_CAN_CLAWBACK : UNSET,
        tmfMPTClearCanClawback ? CLEAR_CAN_CLAWBACK : UNSET
      ).getValue()
    );
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
   * Whether the {@code tmfMPTSetCanLock} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptSetCanLock() {
    return this.isSet(SET_CAN_LOCK);
  }

  /**
   * Whether the {@code tmfMPTClearCanLock} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptClearCanLock() {
    return this.isSet(CLEAR_CAN_LOCK);
  }

  /**
   * Whether the {@code tmfMPTSetRequireAuth} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptSetRequireAuth() {
    return this.isSet(SET_REQUIRE_AUTH);
  }

  /**
   * Whether the {@code tmfMPTClearRequireAuth} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptClearRequireAuth() {
    return this.isSet(CLEAR_REQUIRE_AUTH);
  }

  /**
   * Whether the {@code tmfMPTSetCanEscrow} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptSetCanEscrow() {
    return this.isSet(SET_CAN_ESCROW);
  }

  /**
   * Whether the {@code tmfMPTClearCanEscrow} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptClearCanEscrow() {
    return this.isSet(CLEAR_CAN_ESCROW);
  }

  /**
   * Whether the {@code tmfMPTSetCanTrade} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptSetCanTrade() {
    return this.isSet(SET_CAN_TRADE);
  }

  /**
   * Whether the {@code tmfMPTClearCanTrade} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptClearCanTrade() {
    return this.isSet(CLEAR_CAN_TRADE);
  }

  /**
   * Whether the {@code tmfMPTSetCanTransfer} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptSetCanTransfer() {
    return this.isSet(SET_CAN_TRANSFER);
  }

  /**
   * Whether the {@code tmfMPTClearCanTransfer} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptClearCanTransfer() {
    return this.isSet(CLEAR_CAN_TRANSFER);
  }

  /**
   * Whether the {@code tmfMPTSetCanClawback} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptSetCanClawback() {
    return this.isSet(SET_CAN_CLAWBACK);
  }

  /**
   * Whether the {@code tmfMPTClearCanClawback} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptClearCanClawback() {
    return this.isSet(CLEAR_CAN_CLAWBACK);
  }

  /**
   * A builder for {@link MpTokenIssuanceSetMutableFlags}.
   */
  public static class Builder {

    private boolean tmfMptSetCanLock = false;
    private boolean tmfMptClearCanLock = false;
    private boolean tmfMptSetRequireAuth = false;
    private boolean tmfMptClearRequireAuth = false;
    private boolean tmfMptSetCanEscrow = false;
    private boolean tmfMptClearCanEscrow = false;
    private boolean tmfMptSetCanTrade = false;
    private boolean tmfMptClearCanTrade = false;
    private boolean tmfMptSetCanTransfer = false;
    private boolean tmfMptClearCanTransfer = false;
    private boolean tmfMptSetCanClawback = false;
    private boolean tmfMptClearCanClawback = false;

    /**
     * Set {@code tmfMptSetCanLock}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptSetCanLock(boolean value) {
      this.tmfMptSetCanLock = value;
      return this;
    }

    /**
     * Set {@code tmfMptClearCanLock}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptClearCanLock(boolean value) {
      this.tmfMptClearCanLock = value;
      return this;
    }

    /**
     * Set {@code tmfMptSetRequireAuth}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptSetRequireAuth(boolean value) {
      this.tmfMptSetRequireAuth = value;
      return this;
    }

    /**
     * Set {@code tmfMptClearRequireAuth}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptClearRequireAuth(boolean value) {
      this.tmfMptClearRequireAuth = value;
      return this;
    }

    /**
     * Set {@code tmfMptSetCanEscrow}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptSetCanEscrow(boolean value) {
      this.tmfMptSetCanEscrow = value;
      return this;
    }

    /**
     * Set {@code tmfMptClearCanEscrow}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptClearCanEscrow(boolean value) {
      this.tmfMptClearCanEscrow = value;
      return this;
    }

    /**
     * Set {@code tmfMptSetCanTrade}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptSetCanTrade(boolean value) {
      this.tmfMptSetCanTrade = value;
      return this;
    }

    /**
     * Set {@code tmfMptClearCanTrade}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptClearCanTrade(boolean value) {
      this.tmfMptClearCanTrade = value;
      return this;
    }

    /**
     * Set {@code tmfMptSetCanTransfer}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptSetCanTransfer(boolean value) {
      this.tmfMptSetCanTransfer = value;
      return this;
    }

    /**
     * Set {@code tmfMptClearCanTransfer}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptClearCanTransfer(boolean value) {
      this.tmfMptClearCanTransfer = value;
      return this;
    }

    /**
     * Set {@code tmfMptSetCanClawback}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptSetCanClawback(boolean value) {
      this.tmfMptSetCanClawback = value;
      return this;
    }

    /**
     * Set {@code tmfMptClearCanClawback}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptClearCanClawback(boolean value) {
      this.tmfMptClearCanClawback = value;
      return this;
    }

    /**
     * Build a new {@link MpTokenIssuanceSetMutableFlags} from the current boolean values.
     *
     * @return A new {@link MpTokenIssuanceSetMutableFlags}.
     */
    public MpTokenIssuanceSetMutableFlags build() {
      return MpTokenIssuanceSetMutableFlags.of(
        tmfMptSetCanLock,
        tmfMptClearCanLock,
        tmfMptSetRequireAuth,
        tmfMptClearRequireAuth,
        tmfMptSetCanEscrow,
        tmfMptClearCanEscrow,
        tmfMptSetCanTrade,
        tmfMptClearCanTrade,
        tmfMptSetCanTransfer,
        tmfMptClearCanTransfer,
        tmfMptSetCanClawback,
        tmfMptClearCanClawback
      );
    }
  }
}
