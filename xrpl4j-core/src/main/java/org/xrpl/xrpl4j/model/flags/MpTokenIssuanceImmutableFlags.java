package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set in the {@code ImmutableFlags} field of
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate} and
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet} transactions, and which mirror the
 * {@code ImmutableFlags} field recorded on {@link org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject} ledger
 * objects.
 *
 * <p>{@code MPTokenIssuance} fields and flags are mutable by default. Setting one of these bits (via
 * {@code MPTokenIssuanceCreate} or {@code MPTokenIssuanceSet}) permanently locks the corresponding field or
 * flag so that it can never be changed again. Bits set via {@code MPTokenIssuanceSet} merge with (rather than
 * overwrite) any bits already recorded on the ledger object.
 *
 * @see <a href="https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0094-dynamic-MPT">XLS-94</a>
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceImmutableFlags extends Flags {

  /**
   * Indicates that {@code lsfMPTCanLock} is now permanently immutable. Hex: {@code 0x00000002}.
   */
  public static final MpTokenIssuanceImmutableFlags CAN_LOCK = new MpTokenIssuanceImmutableFlags(0x00000002);

  /**
   * Indicates that {@code lsfMPTRequireAuth} is now permanently immutable. Hex: {@code 0x00000004}.
   */
  public static final MpTokenIssuanceImmutableFlags REQUIRE_AUTH = new MpTokenIssuanceImmutableFlags(0x00000004);

  /**
   * Indicates that {@code lsfMPTCanEscrow} is now permanently immutable. Hex: {@code 0x00000008}.
   */
  public static final MpTokenIssuanceImmutableFlags CAN_ESCROW = new MpTokenIssuanceImmutableFlags(0x00000008);

  /**
   * Indicates that {@code lsfMPTCanTrade} is now permanently immutable. Hex: {@code 0x00000010}.
   */
  public static final MpTokenIssuanceImmutableFlags CAN_TRADE = new MpTokenIssuanceImmutableFlags(0x00000010);

  /**
   * Indicates that {@code lsfMPTCanTransfer} is now permanently immutable. Hex: {@code 0x00000020}.
   */
  public static final MpTokenIssuanceImmutableFlags CAN_TRANSFER = new MpTokenIssuanceImmutableFlags(0x00000020);

  /**
   * Indicates that {@code lsfMPTCanClawback} is now permanently immutable. Hex: {@code 0x00000040}.
   */
  public static final MpTokenIssuanceImmutableFlags CAN_CLAWBACK = new MpTokenIssuanceImmutableFlags(0x00000040);

  /**
   * Indicates that {@code lsfMPTCanHoldConfidentialBalance} is now permanently immutable. Hex: {@code 0x00000080}.
   */
  public static final MpTokenIssuanceImmutableFlags CAN_HOLD_CONFIDENTIAL_BALANCE =
    new MpTokenIssuanceImmutableFlags(0x00000080);

  /**
   * Indicates that the {@code MPTokenMetadata} field is now permanently immutable. Hex: {@code 0x00010000}.
   */
  public static final MpTokenIssuanceImmutableFlags METADATA = new MpTokenIssuanceImmutableFlags(0x00010000);

  /**
   * Indicates that the {@code TransferFee} field is now permanently immutable. Hex: {@code 0x00020000}.
   */
  public static final MpTokenIssuanceImmutableFlags TRANSFER_FEE = new MpTokenIssuanceImmutableFlags(0x00020000);

  /**
   * All valid bits for the {@code ImmutableFlags} field.
   */
  public static final long VALID_MASK =
    CAN_LOCK.getValue() |
    REQUIRE_AUTH.getValue() |
    CAN_ESCROW.getValue() |
    CAN_TRADE.getValue() |
    CAN_TRANSFER.getValue() |
    CAN_CLAWBACK.getValue() |
    CAN_HOLD_CONFIDENTIAL_BALANCE.getValue() |
    METADATA.getValue() |
    TRANSFER_FEE.getValue();

  private MpTokenIssuanceImmutableFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceImmutableFlags() {
  }

  /**
   * Construct {@link MpTokenIssuanceImmutableFlags} for the given raw value.
   *
   * @param value The long-number encoded flags value.
   *
   * @return A new {@link MpTokenIssuanceImmutableFlags}.
   */
  public static MpTokenIssuanceImmutableFlags of(long value) {
    return new MpTokenIssuanceImmutableFlags(value);
  }

  private static MpTokenIssuanceImmutableFlags of(
    boolean lsifMPTCanLock,
    boolean lsifMPTRequireAuth,
    boolean lsifMPTCanEscrow,
    boolean lsifMPTCanTrade,
    boolean lsifMPTCanTransfer,
    boolean lsifMPTCanClawback,
    boolean lsifMPTCanHoldConfidentialBalance,
    boolean lsifMPTMetadata,
    boolean lsifMPTTransferFee
  ) {
    return new MpTokenIssuanceImmutableFlags(
      Flags.of(
        lsifMPTCanLock ? CAN_LOCK : UNSET,
        lsifMPTRequireAuth ? REQUIRE_AUTH : UNSET,
        lsifMPTCanEscrow ? CAN_ESCROW : UNSET,
        lsifMPTCanTrade ? CAN_TRADE : UNSET,
        lsifMPTCanTransfer ? CAN_TRANSFER : UNSET,
        lsifMPTCanClawback ? CAN_CLAWBACK : UNSET,
        lsifMPTCanHoldConfidentialBalance ? CAN_HOLD_CONFIDENTIAL_BALANCE : UNSET,
        lsifMPTMetadata ? METADATA : UNSET,
        lsifMPTTransferFee ? TRANSFER_FEE : UNSET
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
   * Whether the {@code lsifMPTCanLock} flag is set, indicating {@code lsfMPTCanLock} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptCanLock() {
    return this.isSet(CAN_LOCK);
  }

  /**
   * Whether the {@code lsifMPTRequireAuth} flag is set, indicating {@code lsfMPTRequireAuth} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptRequireAuth() {
    return this.isSet(REQUIRE_AUTH);
  }

  /**
   * Whether the {@code lsifMPTCanEscrow} flag is set, indicating {@code lsfMPTCanEscrow} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptCanEscrow() {
    return this.isSet(CAN_ESCROW);
  }

  /**
   * Whether the {@code lsifMPTCanTrade} flag is set, indicating {@code lsfMPTCanTrade} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptCanTrade() {
    return this.isSet(CAN_TRADE);
  }

  /**
   * Whether the {@code lsifMPTCanTransfer} flag is set, indicating {@code lsfMPTCanTransfer} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptCanTransfer() {
    return this.isSet(CAN_TRANSFER);
  }

  /**
   * Whether the {@code lsifMPTCanClawback} flag is set, indicating {@code lsfMPTCanClawback} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptCanClawback() {
    return this.isSet(CAN_CLAWBACK);
  }

  /**
   * Whether the {@code lsifMPTCanHoldConfidentialBalance} flag is set, indicating
   * {@code lsfMPTCanHoldConfidentialBalance} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptCanHoldConfidentialBalance() {
    return this.isSet(CAN_HOLD_CONFIDENTIAL_BALANCE);
  }

  /**
   * Whether the {@code lsifMPTMetadata} flag is set, indicating {@code MPTokenMetadata} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptMetadata() {
    return this.isSet(METADATA);
  }

  /**
   * Whether the {@code lsifMPTTransferFee} flag is set, indicating {@code TransferFee} is now immutable.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsifMptTransferFee() {
    return this.isSet(TRANSFER_FEE);
  }

  /**
   * A builder for {@link MpTokenIssuanceImmutableFlags}.
   */
  public static class Builder {

    private boolean lsifMptCanLock = false;
    private boolean lsifMptRequireAuth = false;
    private boolean lsifMptCanEscrow = false;
    private boolean lsifMptCanTrade = false;
    private boolean lsifMptCanTransfer = false;
    private boolean lsifMptCanClawback = false;
    private boolean lsifMptCanHoldConfidentialBalance = false;
    private boolean lsifMptMetadata = false;
    private boolean lsifMptTransferFee = false;

    /**
     * Set {@code lsifMptCanLock}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptCanLock(boolean value) {
      this.lsifMptCanLock = value;
      return this;
    }

    /**
     * Set {@code lsifMptRequireAuth}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptRequireAuth(boolean value) {
      this.lsifMptRequireAuth = value;
      return this;
    }

    /**
     * Set {@code lsifMptCanEscrow}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptCanEscrow(boolean value) {
      this.lsifMptCanEscrow = value;
      return this;
    }

    /**
     * Set {@code lsifMptCanTrade}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptCanTrade(boolean value) {
      this.lsifMptCanTrade = value;
      return this;
    }

    /**
     * Set {@code lsifMptCanTransfer}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptCanTransfer(boolean value) {
      this.lsifMptCanTransfer = value;
      return this;
    }

    /**
     * Set {@code lsifMptCanClawback}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptCanClawback(boolean value) {
      this.lsifMptCanClawback = value;
      return this;
    }

    /**
     * Set {@code lsifMptCanHoldConfidentialBalance}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptCanHoldConfidentialBalance(boolean value) {
      this.lsifMptCanHoldConfidentialBalance = value;
      return this;
    }

    /**
     * Set {@code lsifMptMetadata}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptMetadata(boolean value) {
      this.lsifMptMetadata = value;
      return this;
    }

    /**
     * Set {@code lsifMptTransferFee}.
     *
     * @param value A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder lsifMptTransferFee(boolean value) {
      this.lsifMptTransferFee = value;
      return this;
    }

    /**
     * Build a new {@link MpTokenIssuanceImmutableFlags} from the current boolean values.
     *
     * @return A new {@link MpTokenIssuanceImmutableFlags}.
     */
    public MpTokenIssuanceImmutableFlags build() {
      return MpTokenIssuanceImmutableFlags.of(
        lsifMptCanLock,
        lsifMptRequireAuth,
        lsifMptCanEscrow,
        lsifMptCanTrade,
        lsifMptCanTransfer,
        lsifMptCanClawback,
        lsifMptCanHoldConfidentialBalance,
        lsifMptMetadata,
        lsifMptTransferFee
      );
    }
  }
}
