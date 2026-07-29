package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set in the {@code MutableFlags} field of
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate} transactions.
 *
 * <p>These flags (prefixed with {@code tmf}) declare which capabilities of the created
 * {@code MPTokenIssuance} may be mutated after issuance via {@code MPTokenIssuanceSet}. Only
 * capabilities that were declared mutable at creation time may be enabled, and a capability
 * becomes immutable once enabled. The {@code CanMutate*} flags declare that the corresponding
 * field may be modified.
 *
 * @see <a href="https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0094-dynamic-MPT">XLS-94</a>
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceCreateMutableFlags extends Flags {

  /**
   * Indicates flag {@code lsfMPTCanLock} can be enabled. Hex: {@code 0x00000002}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_ENABLE_CAN_LOCK =
    new MpTokenIssuanceCreateMutableFlags(0x00000002);

  /**
   * Indicates flag {@code lsfMPTRequireAuth} can be enabled. Hex: {@code 0x00000004}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_ENABLE_REQUIRE_AUTH =
    new MpTokenIssuanceCreateMutableFlags(0x00000004);

  /**
   * Indicates flag {@code lsfMPTCanEscrow} can be enabled. Hex: {@code 0x00000008}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_ENABLE_CAN_ESCROW =
    new MpTokenIssuanceCreateMutableFlags(0x00000008);

  /**
   * Indicates flag {@code lsfMPTCanTrade} can be enabled. Hex: {@code 0x00000010}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_ENABLE_CAN_TRADE =
    new MpTokenIssuanceCreateMutableFlags(0x00000010);

  /**
   * Indicates flag {@code lsfMPTCanTransfer} can be enabled. Hex: {@code 0x00000020}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_ENABLE_CAN_TRANSFER =
    new MpTokenIssuanceCreateMutableFlags(0x00000020);

  /**
   * Indicates flag {@code lsfMPTCanClawback} can be enabled. Hex: {@code 0x00000040}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_ENABLE_CAN_CLAWBACK =
    new MpTokenIssuanceCreateMutableFlags(0x00000040);

  /**
   * Allows field {@code MPTokenMetadata} to be modified. Hex: {@code 0x00010000}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_METADATA =
    new MpTokenIssuanceCreateMutableFlags(0x00010000);

  /**
   * Allows field {@code TransferFee} to be modified. Hex: {@code 0x00020000}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_TRANSFER_FEE =
    new MpTokenIssuanceCreateMutableFlags(0x00020000);

  /**
   * All valid bits for the {@code MutableFlags} field on {@code MPTokenIssuanceCreate}.
   * Bit {@code 0x00000001} is reserved (mirrors {@code lsfMPTLocked}) and is excluded.
   */
  public static final long VALID_MASK =
    CAN_ENABLE_CAN_LOCK.getValue() |
    CAN_ENABLE_REQUIRE_AUTH.getValue() |
    CAN_ENABLE_CAN_ESCROW.getValue() |
    CAN_ENABLE_CAN_TRADE.getValue() |
    CAN_ENABLE_CAN_TRANSFER.getValue() |
    CAN_ENABLE_CAN_CLAWBACK.getValue() |
    CAN_MUTATE_METADATA.getValue() |
    CAN_MUTATE_TRANSFER_FEE.getValue();

  private MpTokenIssuanceCreateMutableFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceCreateMutableFlags() {
  }

  /**
   * Construct {@link MpTokenIssuanceCreateMutableFlags} for the given raw value.
   *
   * <p><strong>Note:</strong> Bit {@code 0x00000001} is reserved — it mirrors {@code lsfMPTLocked} in the ledger
   * {@code Flags} field and is not a valid bit for {@code MutableFlags}. Passing a value with this bit set will be
   * rejected by rippled ({@code temINVALID_FLAG}) and will trigger an {@link IllegalStateException} when the
   * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate} containing this value is built.
   *
   * @param value The long-number encoded flags value.
   *
   * @return A new {@link MpTokenIssuanceCreateMutableFlags}.
   */
  public static MpTokenIssuanceCreateMutableFlags of(long value) {
    return new MpTokenIssuanceCreateMutableFlags(value);
  }

  private static MpTokenIssuanceCreateMutableFlags of(
    boolean tmfMPTCanEnableCanLock,
    boolean tmfMPTCanEnableRequireAuth,
    boolean tmfMPTCanEnableCanEscrow,
    boolean tmfMPTCanEnableCanTrade,
    boolean tmfMPTCanEnableCanTransfer,
    boolean tmfMPTCanEnableCanClawback,
    boolean tmfMPTCanMutateMetadata,
    boolean tmfMPTCanMutateTransferFee
  ) {
    return new MpTokenIssuanceCreateMutableFlags(
      Flags.of(
        tmfMPTCanEnableCanLock ? CAN_ENABLE_CAN_LOCK : UNSET,
        tmfMPTCanEnableRequireAuth ? CAN_ENABLE_REQUIRE_AUTH : UNSET,
        tmfMPTCanEnableCanEscrow ? CAN_ENABLE_CAN_ESCROW : UNSET,
        tmfMPTCanEnableCanTrade ? CAN_ENABLE_CAN_TRADE : UNSET,
        tmfMPTCanEnableCanTransfer ? CAN_ENABLE_CAN_TRANSFER : UNSET,
        tmfMPTCanEnableCanClawback ? CAN_ENABLE_CAN_CLAWBACK : UNSET,
        tmfMPTCanMutateMetadata ? CAN_MUTATE_METADATA : UNSET,
        tmfMPTCanMutateTransferFee ? CAN_MUTATE_TRANSFER_FEE : UNSET
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
   * Whether the {@code tmfMPTCanEnableCanLock} flag is set, indicating {@code lsfMPTCanLock} can be enabled.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanEnableCanLock() {
    return this.isSet(CAN_ENABLE_CAN_LOCK);
  }

  /**
   * Whether the {@code tmfMPTCanEnableRequireAuth} flag is set, indicating {@code lsfMPTRequireAuth} can be enabled.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanEnableRequireAuth() {
    return this.isSet(CAN_ENABLE_REQUIRE_AUTH);
  }

  /**
   * Whether the {@code tmfMPTCanEnableCanEscrow} flag is set, indicating {@code lsfMPTCanEscrow} can be enabled.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanEnableCanEscrow() {
    return this.isSet(CAN_ENABLE_CAN_ESCROW);
  }

  /**
   * Whether the {@code tmfMPTCanEnableCanTrade} flag is set, indicating {@code lsfMPTCanTrade} can be enabled.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanEnableCanTrade() {
    return this.isSet(CAN_ENABLE_CAN_TRADE);
  }

  /**
   * Whether the {@code tmfMPTCanEnableCanTransfer} flag is set, indicating {@code lsfMPTCanTransfer} can be enabled.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanEnableCanTransfer() {
    return this.isSet(CAN_ENABLE_CAN_TRANSFER);
  }

  /**
   * Whether the {@code tmfMPTCanEnableCanClawback} flag is set, indicating {@code lsfMPTCanClawback} can be enabled.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanEnableCanClawback() {
    return this.isSet(CAN_ENABLE_CAN_CLAWBACK);
  }

  /**
   * Whether the {@code tmfMPTCanMutateMetadata} flag is set, allowing {@code MPTokenMetadata} to be modified.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateMetadata() {
    return this.isSet(CAN_MUTATE_METADATA);
  }

  /**
   * Whether the {@code tmfMPTCanMutateTransferFee} flag is set, allowing {@code TransferFee} to be modified.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateTransferFee() {
    return this.isSet(CAN_MUTATE_TRANSFER_FEE);
  }

  /**
   * A builder for {@link MpTokenIssuanceCreateMutableFlags}.
   */
  public static class Builder {

    private boolean tmfMptCanEnableCanLock = false;
    private boolean tmfMptCanEnableRequireAuth = false;
    private boolean tmfMptCanEnableCanEscrow = false;
    private boolean tmfMptCanEnableCanTrade = false;
    private boolean tmfMptCanEnableCanTransfer = false;
    private boolean tmfMptCanEnableCanClawback = false;
    private boolean tmfMptCanMutateMetadata = false;
    private boolean tmfMptCanMutateTransferFee = false;

    /**
     * Set {@code tmfMptCanEnableCanLock}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanEnableCanLock(boolean value) {
      this.tmfMptCanEnableCanLock = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanEnableRequireAuth}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanEnableRequireAuth(boolean value) {
      this.tmfMptCanEnableRequireAuth = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanEnableCanEscrow}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanEnableCanEscrow(boolean value) {
      this.tmfMptCanEnableCanEscrow = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanEnableCanTrade}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanEnableCanTrade(boolean value) {
      this.tmfMptCanEnableCanTrade = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanEnableCanTransfer}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanEnableCanTransfer(boolean value) {
      this.tmfMptCanEnableCanTransfer = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanEnableCanClawback}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanEnableCanClawback(boolean value) {
      this.tmfMptCanEnableCanClawback = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateMetadata}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateMetadata(boolean value) {
      this.tmfMptCanMutateMetadata = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateTransferFee}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateTransferFee(boolean value) {
      this.tmfMptCanMutateTransferFee = value;
      return this;
    }

    /**
     * Build a new {@link MpTokenIssuanceCreateMutableFlags} from the current boolean values.
     *
     * @return A new {@link MpTokenIssuanceCreateMutableFlags}.
     */
    public MpTokenIssuanceCreateMutableFlags build() {
      return MpTokenIssuanceCreateMutableFlags.of(
        tmfMptCanEnableCanLock,
        tmfMptCanEnableRequireAuth,
        tmfMptCanEnableCanEscrow,
        tmfMptCanEnableCanTrade,
        tmfMptCanEnableCanTransfer,
        tmfMptCanEnableCanClawback,
        tmfMptCanMutateMetadata,
        tmfMptCanMutateTransferFee
      );
    }
  }
}
