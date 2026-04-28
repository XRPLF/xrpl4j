package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set in the {@code MutableFlags} field of
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate} transactions.
 *
 * <p>These flags (prefixed with {@code tmf}) declare which fields or flags of the created
 * {@code MPTokenIssuance} may be mutated after issuance via {@code MPTokenIssuanceSet}.
 *
 * @see <a href="https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0094-dynamic-MPT">XLS-94</a>
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceCreateMutableFlags extends Flags {

  /**
   * Indicates flag {@code lsfMPTCanLock} can be changed. Hex: {@code 0x00000002}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_CAN_LOCK =
    new MpTokenIssuanceCreateMutableFlags(0x00000002);

  /**
   * Indicates flag {@code lsfMPTRequireAuth} can be changed. Hex: {@code 0x00000004}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_REQUIRE_AUTH =
    new MpTokenIssuanceCreateMutableFlags(0x00000004);

  /**
   * Indicates flag {@code lsfMPTCanEscrow} can be changed. Hex: {@code 0x00000008}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_CAN_ESCROW =
    new MpTokenIssuanceCreateMutableFlags(0x00000008);

  /**
   * Indicates flag {@code lsfMPTCanTrade} can be changed. Hex: {@code 0x00000010}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_CAN_TRADE =
    new MpTokenIssuanceCreateMutableFlags(0x00000010);

  /**
   * Indicates flag {@code lsfMPTCanTransfer} can be changed. Hex: {@code 0x00000020}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_CAN_TRANSFER =
    new MpTokenIssuanceCreateMutableFlags(0x00000020);

  /**
   * Indicates flag {@code lsfMPTCanClawback} can be changed. Hex: {@code 0x00000040}.
   */
  public static final MpTokenIssuanceCreateMutableFlags CAN_MUTATE_CAN_CLAWBACK =
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

  private MpTokenIssuanceCreateMutableFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceCreateMutableFlags() {
  }

  /**
   * Construct {@link MpTokenIssuanceCreateMutableFlags} for the given raw value.
   *
   * @param value The long-number encoded flags value.
   *
   * @return A new {@link MpTokenIssuanceCreateMutableFlags}.
   */
  public static MpTokenIssuanceCreateMutableFlags of(long value) {
    return new MpTokenIssuanceCreateMutableFlags(value);
  }

  private static MpTokenIssuanceCreateMutableFlags of(
    boolean tmfMPTCanMutateCanLock,
    boolean tmfMPTCanMutateRequireAuth,
    boolean tmfMPTCanMutateCanEscrow,
    boolean tmfMPTCanMutateCanTrade,
    boolean tmfMPTCanMutateCanTransfer,
    boolean tmfMPTCanMutateCanClawback,
    boolean tmfMPTCanMutateMetadata,
    boolean tmfMPTCanMutateTransferFee
  ) {
    return new MpTokenIssuanceCreateMutableFlags(
      Flags.of(
        tmfMPTCanMutateCanLock ? CAN_MUTATE_CAN_LOCK : UNSET,
        tmfMPTCanMutateRequireAuth ? CAN_MUTATE_REQUIRE_AUTH : UNSET,
        tmfMPTCanMutateCanEscrow ? CAN_MUTATE_CAN_ESCROW : UNSET,
        tmfMPTCanMutateCanTrade ? CAN_MUTATE_CAN_TRADE : UNSET,
        tmfMPTCanMutateCanTransfer ? CAN_MUTATE_CAN_TRANSFER : UNSET,
        tmfMPTCanMutateCanClawback ? CAN_MUTATE_CAN_CLAWBACK : UNSET,
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
   * Whether the {@code tmfMPTCanMutateCanLock} flag is set, indicating {@code lsfMPTCanLock} can be changed.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateCanLock() {
    return this.isSet(CAN_MUTATE_CAN_LOCK);
  }

  /**
   * Whether the {@code tmfMPTCanMutateRequireAuth} flag is set, indicating {@code lsfMPTRequireAuth} can be changed.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateRequireAuth() {
    return this.isSet(CAN_MUTATE_REQUIRE_AUTH);
  }

  /**
   * Whether the {@code tmfMPTCanMutateCanEscrow} flag is set, indicating {@code lsfMPTCanEscrow} can be changed.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateCanEscrow() {
    return this.isSet(CAN_MUTATE_CAN_ESCROW);
  }

  /**
   * Whether the {@code tmfMPTCanMutateCanTrade} flag is set, indicating {@code lsfMPTCanTrade} can be changed.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateCanTrade() {
    return this.isSet(CAN_MUTATE_CAN_TRADE);
  }

  /**
   * Whether the {@code tmfMPTCanMutateCanTransfer} flag is set, indicating {@code lsfMPTCanTransfer} can be changed.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateCanTransfer() {
    return this.isSet(CAN_MUTATE_CAN_TRANSFER);
  }

  /**
   * Whether the {@code tmfMPTCanMutateCanClawback} flag is set, indicating {@code lsfMPTCanClawback} can be changed.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean tmfMptCanMutateCanClawback() {
    return this.isSet(CAN_MUTATE_CAN_CLAWBACK);
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

    private boolean tmfMptCanMutateCanLock = false;
    private boolean tmfMptCanMutateRequireAuth = false;
    private boolean tmfMptCanMutateCanEscrow = false;
    private boolean tmfMptCanMutateCanTrade = false;
    private boolean tmfMptCanMutateCanTransfer = false;
    private boolean tmfMptCanMutateCanClawback = false;
    private boolean tmfMptCanMutateMetadata = false;
    private boolean tmfMptCanMutateTransferFee = false;

    /**
     * Set {@code tmfMptCanMutateCanLock}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateCanLock(boolean value) {
      this.tmfMptCanMutateCanLock = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateRequireAuth}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateRequireAuth(boolean value) {
      this.tmfMptCanMutateRequireAuth = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateCanEscrow}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateCanEscrow(boolean value) {
      this.tmfMptCanMutateCanEscrow = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateCanTrade}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateCanTrade(boolean value) {
      this.tmfMptCanMutateCanTrade = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateCanTransfer}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateCanTransfer(boolean value) {
      this.tmfMptCanMutateCanTransfer = value;
      return this;
    }

    /**
     * Set {@code tmfMptCanMutateCanClawback}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tmfMptCanMutateCanClawback(boolean value) {
      this.tmfMptCanMutateCanClawback = value;
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
        tmfMptCanMutateCanLock,
        tmfMptCanMutateRequireAuth,
        tmfMptCanMutateCanEscrow,
        tmfMptCanMutateCanTrade,
        tmfMptCanMutateCanTransfer,
        tmfMptCanMutateCanClawback,
        tmfMptCanMutateMetadata,
        tmfMptCanMutateTransferFee
      );
    }
  }
}
