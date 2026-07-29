package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set in the {@code MutableFlags} field of
 * {@link org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject} ledger objects.
 *
 * <p>These on-ledger flags (prefixed with {@code lsmf}) record which fields or flags of the
 * {@code MPTokenIssuance} were declared mutable at creation time and may be modified via
 * {@code MPTokenIssuanceSet}. The {@code CanEnable*} flags record that the corresponding capability may
 * later be enabled (once) by the issuer, while the {@code CanMutate*} flags record that the corresponding
 * field may be modified.
 *
 * @see <a href="https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0094-dynamic-MPT">XLS-94</a>
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceMutableFlags extends Flags {

  /**
   * Indicates flag {@code lsfMPTCanLock} can be enabled. Hex: {@code 0x00000002}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_ENABLE_CAN_LOCK =
    new MpTokenIssuanceMutableFlags(0x00000002);

  /**
   * Indicates flag {@code lsfMPTRequireAuth} can be enabled. Hex: {@code 0x00000004}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_ENABLE_REQUIRE_AUTH =
    new MpTokenIssuanceMutableFlags(0x00000004);

  /**
   * Indicates flag {@code lsfMPTCanEscrow} can be enabled. Hex: {@code 0x00000008}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_ENABLE_CAN_ESCROW =
    new MpTokenIssuanceMutableFlags(0x00000008);

  /**
   * Indicates flag {@code lsfMPTCanTrade} can be enabled. Hex: {@code 0x00000010}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_ENABLE_CAN_TRADE =
    new MpTokenIssuanceMutableFlags(0x00000010);

  /**
   * Indicates flag {@code lsfMPTCanTransfer} can be enabled. Hex: {@code 0x00000020}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_ENABLE_CAN_TRANSFER =
    new MpTokenIssuanceMutableFlags(0x00000020);

  /**
   * Indicates flag {@code lsfMPTCanClawback} can be enabled. Hex: {@code 0x00000040}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_ENABLE_CAN_CLAWBACK =
    new MpTokenIssuanceMutableFlags(0x00000040);

  /**
   * Allows field {@code MPTokenMetadata} to be modified. Hex: {@code 0x00010000}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_METADATA =
    new MpTokenIssuanceMutableFlags(0x00010000);

  /**
   * Allows field {@code TransferFee} to be modified. Hex: {@code 0x00020000}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_TRANSFER_FEE =
    new MpTokenIssuanceMutableFlags(0x00020000);

  private MpTokenIssuanceMutableFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceMutableFlags() {
  }

  /**
   * Construct {@link MpTokenIssuanceMutableFlags} for the given raw value.
   *
   * @param value The long-number encoded flags value.
   *
   * @return A new {@link MpTokenIssuanceMutableFlags}.
   */
  public static MpTokenIssuanceMutableFlags of(long value) {
    return new MpTokenIssuanceMutableFlags(value);
  }

  private static MpTokenIssuanceMutableFlags of(
    boolean lsmfMPTCanEnableCanLock,
    boolean lsmfMPTCanEnableRequireAuth,
    boolean lsmfMPTCanEnableCanEscrow,
    boolean lsmfMPTCanEnableCanTrade,
    boolean lsmfMPTCanEnableCanTransfer,
    boolean lsmfMPTCanEnableCanClawback,
    boolean lsmfMPTCanMutateMetadata,
    boolean lsmfMPTCanMutateTransferFee
  ) {
    return new MpTokenIssuanceMutableFlags(
      Flags.of(
        lsmfMPTCanEnableCanLock ? CAN_ENABLE_CAN_LOCK : UNSET,
        lsmfMPTCanEnableRequireAuth ? CAN_ENABLE_REQUIRE_AUTH : UNSET,
        lsmfMPTCanEnableCanEscrow ? CAN_ENABLE_CAN_ESCROW : UNSET,
        lsmfMPTCanEnableCanTrade ? CAN_ENABLE_CAN_TRADE : UNSET,
        lsmfMPTCanEnableCanTransfer ? CAN_ENABLE_CAN_TRANSFER : UNSET,
        lsmfMPTCanEnableCanClawback ? CAN_ENABLE_CAN_CLAWBACK : UNSET,
        lsmfMPTCanMutateMetadata ? CAN_MUTATE_METADATA : UNSET,
        lsmfMPTCanMutateTransferFee ? CAN_MUTATE_TRANSFER_FEE : UNSET
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
   * Whether the {@code lsmfMPTCanEnableCanLock} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanEnableCanLock() {
    return this.isSet(CAN_ENABLE_CAN_LOCK);
  }

  /**
   * Whether the {@code lsmfMPTCanEnableRequireAuth} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanEnableRequireAuth() {
    return this.isSet(CAN_ENABLE_REQUIRE_AUTH);
  }

  /**
   * Whether the {@code lsmfMPTCanEnableCanEscrow} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanEnableCanEscrow() {
    return this.isSet(CAN_ENABLE_CAN_ESCROW);
  }

  /**
   * Whether the {@code lsmfMPTCanEnableCanTrade} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanEnableCanTrade() {
    return this.isSet(CAN_ENABLE_CAN_TRADE);
  }

  /**
   * Whether the {@code lsmfMPTCanEnableCanTransfer} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanEnableCanTransfer() {
    return this.isSet(CAN_ENABLE_CAN_TRANSFER);
  }

  /**
   * Whether the {@code lsmfMPTCanEnableCanClawback} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanEnableCanClawback() {
    return this.isSet(CAN_ENABLE_CAN_CLAWBACK);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateMetadata} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateMetadata() {
    return this.isSet(CAN_MUTATE_METADATA);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateTransferFee} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateTransferFee() {
    return this.isSet(CAN_MUTATE_TRANSFER_FEE);
  }

  /**
   * A builder for {@link MpTokenIssuanceMutableFlags}.
   */
  public static class Builder {

    private boolean lsmfMptCanEnableCanLock = false;
    private boolean lsmfMptCanEnableRequireAuth = false;
    private boolean lsmfMptCanEnableCanEscrow = false;
    private boolean lsmfMptCanEnableCanTrade = false;
    private boolean lsmfMptCanEnableCanTransfer = false;
    private boolean lsmfMptCanEnableCanClawback = false;
    private boolean lsmfMptCanMutateMetadata = false;
    private boolean lsmfMptCanMutateTransferFee = false;

    /**
     * Set {@code lsmfMptCanEnableCanLock}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanEnableCanLock(boolean value) {
      this.lsmfMptCanEnableCanLock = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanEnableRequireAuth}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanEnableRequireAuth(boolean value) {
      this.lsmfMptCanEnableRequireAuth = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanEnableCanEscrow}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanEnableCanEscrow(boolean value) {
      this.lsmfMptCanEnableCanEscrow = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanEnableCanTrade}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanEnableCanTrade(boolean value) {
      this.lsmfMptCanEnableCanTrade = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanEnableCanTransfer}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanEnableCanTransfer(boolean value) {
      this.lsmfMptCanEnableCanTransfer = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanEnableCanClawback}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanEnableCanClawback(boolean value) {
      this.lsmfMptCanEnableCanClawback = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanMutateMetadata}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanMutateMetadata(boolean value) {
      this.lsmfMptCanMutateMetadata = value;
      return this;
    }

    /**
     * Set {@code lsmfMptCanMutateTransferFee}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsmfMptCanMutateTransferFee(boolean value) {
      this.lsmfMptCanMutateTransferFee = value;
      return this;
    }

    /**
     * Build a new {@link MpTokenIssuanceMutableFlags} from the current boolean values.
     *
     * @return A new {@link MpTokenIssuanceMutableFlags}.
     */
    public MpTokenIssuanceMutableFlags build() {
      return MpTokenIssuanceMutableFlags.of(
        lsmfMptCanEnableCanLock,
        lsmfMptCanEnableRequireAuth,
        lsmfMptCanEnableCanEscrow,
        lsmfMptCanEnableCanTrade,
        lsmfMptCanEnableCanTransfer,
        lsmfMptCanEnableCanClawback,
        lsmfMptCanMutateMetadata,
        lsmfMptCanMutateTransferFee
      );
    }
  }
}
