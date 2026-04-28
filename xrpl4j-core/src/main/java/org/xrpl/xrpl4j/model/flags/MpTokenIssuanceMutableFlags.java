package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set in the {@code MutableFlags} field of
 * {@link org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject} ledger objects.
 *
 * <p>These on-ledger flags (prefixed with {@code lsmf}) record which fields or flags of the
 * {@code MPTokenIssuance} were declared mutable at creation time and may be modified via
 * {@code MPTokenIssuanceSet}.
 *
 * @see <a href="https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0094-dynamic-MPT">XLS-94</a>
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceMutableFlags extends Flags {

  /**
   * Indicates flag {@code lsfMPTCanLock} can be changed. Hex: {@code 0x00000002}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_CAN_LOCK =
    new MpTokenIssuanceMutableFlags(0x00000002);

  /**
   * Indicates flag {@code lsfMPTRequireAuth} can be changed. Hex: {@code 0x00000004}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_REQUIRE_AUTH =
    new MpTokenIssuanceMutableFlags(0x00000004);

  /**
   * Indicates flag {@code lsfMPTCanEscrow} can be changed. Hex: {@code 0x00000008}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_CAN_ESCROW =
    new MpTokenIssuanceMutableFlags(0x00000008);

  /**
   * Indicates flag {@code lsfMPTCanTrade} can be changed. Hex: {@code 0x00000010}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_CAN_TRADE =
    new MpTokenIssuanceMutableFlags(0x00000010);

  /**
   * Indicates flag {@code lsfMPTCanTransfer} can be changed. Hex: {@code 0x00000020}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_CAN_TRANSFER =
    new MpTokenIssuanceMutableFlags(0x00000020);

  /**
   * Indicates flag {@code lsfMPTCanClawback} can be changed. Hex: {@code 0x00000040}.
   */
  public static final MpTokenIssuanceMutableFlags CAN_MUTATE_CAN_CLAWBACK =
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

  /**
   * Whether the {@code lsmfMPTCanMutateCanLock} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateCanLock() {
    return this.isSet(CAN_MUTATE_CAN_LOCK);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateRequireAuth} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateRequireAuth() {
    return this.isSet(CAN_MUTATE_REQUIRE_AUTH);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateCanEscrow} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateCanEscrow() {
    return this.isSet(CAN_MUTATE_CAN_ESCROW);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateCanTrade} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateCanTrade() {
    return this.isSet(CAN_MUTATE_CAN_TRADE);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateCanTransfer} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateCanTransfer() {
    return this.isSet(CAN_MUTATE_CAN_TRANSFER);
  }

  /**
   * Whether the {@code lsmfMPTCanMutateCanClawback} flag is set.
   *
   * @return {@code true} if set, otherwise {@code false}.
   */
  public boolean lsmfMptCanMutateCanClawback() {
    return this.isSet(CAN_MUTATE_CAN_CLAWBACK);
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
}
