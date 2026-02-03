package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject}s.
 */
public class MpTokenIssuanceFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final MpTokenIssuanceFlags UNSET = new MpTokenIssuanceFlags(0);

  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTLocked} account flag.
   */
  public static final MpTokenIssuanceFlags LOCKED = new MpTokenIssuanceFlags(0x00000001);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTCanLock} account flag.
   */
  public static final MpTokenIssuanceFlags CAN_LOCK = new MpTokenIssuanceFlags(0x00000002);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTRequireAuth} account flag.
   */
  public static final MpTokenIssuanceFlags REQUIRE_AUTH = new MpTokenIssuanceFlags(0x00000004);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTCanEscrow} account flag.
   */
  public static final MpTokenIssuanceFlags CAN_ESCROW = new MpTokenIssuanceFlags(0x00000008);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTCanTrade} account flag.
   */
  public static final MpTokenIssuanceFlags CAN_TRADE = new MpTokenIssuanceFlags(0x00000010);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTCanTransfer} account flag.
   */
  public static final MpTokenIssuanceFlags CAN_TRANSFER = new MpTokenIssuanceFlags(0x00000020);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTCanClawback} account flag.
   */
  public static final MpTokenIssuanceFlags CAN_CLAWBACK = new MpTokenIssuanceFlags(0x00000040);
  /**
   * Constant {@link MpTokenIssuanceFlags} for the {@code lsfMPTCanPrivacy} account flag.
   */
  public static final MpTokenIssuanceFlags CAN_PRIVACY = new MpTokenIssuanceFlags(0x00000080);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link MpTokenIssuanceFlags}.
   */
  private MpTokenIssuanceFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link MpTokenIssuanceFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link MpTokenIssuanceFlags}.
   * @return New {@link MpTokenIssuanceFlags}.
   */
  public static MpTokenIssuanceFlags of(long value) {
    return new MpTokenIssuanceFlags(value);
  }

  /**
   * If set, indicates that all balances are locked.
   *
   * @return {@code true} if {@code lsfMPTLocked} is set, otherwise {@code false}.
   */
  public boolean lsfMptLocked() {
    return this.isSet(MpTokenIssuanceFlags.LOCKED);
  }

  /**
   * If set, indicates that the issuer can lock an individual balance or all balances of this MPT. If not set, the MPT
   * cannot be locked in any way.
   *
   * @return {@code true} if {@code lsfMPTCanLock} is set, otherwise {@code false}.
   */
  public boolean lsfMptCanLock() {
    return this.isSet(MpTokenIssuanceFlags.CAN_LOCK);
  }

  /**
   * If set, indicates that individual holders must be authorized. This enables issuers to limit who can hold their
   * assets.
   *
   * @return {@code true} if {@code lsfMPTRequireAuth} is set, otherwise {@code false}.
   */
  public boolean lsfMptRequireAuth() {
    return this.isSet(MpTokenIssuanceFlags.REQUIRE_AUTH);
  }

  /**
   * If set, indicates that individual holders can place their balances into an escrow.
   *
   * @return {@code true} if {@code lsfMPTCanEscrow} is set, otherwise {@code false}.
   */
  public boolean lsfMptCanEscrow() {
    return this.isSet(MpTokenIssuanceFlags.CAN_ESCROW);
  }

  /**
   * If set, indicates that individual holders can trade their balances using the XRP Ledger DEX or AMM.
   *
   * @return {@code true} if {@code lsfMPTCanTrade} is set, otherwise {@code false}.
   */
  public boolean lsfMptCanTrade() {
    return this.isSet(MpTokenIssuanceFlags.CAN_TRADE);
  }

  /**
   * If set, indicates that tokens held by non-issuers may be transferred to other accounts. If not set, indicates that
   * tokens held by non-issuers may not be transferred except back to the issuer; this enables use-cases like store
   * credit.
   *
   * @return {@code true} if {@code lsfMPTCanTransfer} is set, otherwise {@code false}.
   */
  public boolean lsfMptCanTransfer() {
    return this.isSet(MpTokenIssuanceFlags.CAN_TRANSFER);
  }

  /**
   * If set, indicates that the issuer may use the Clawback transaction to clawback value from individual holders.
   *
   * @return {@code true} if {@code lsfMPTCanClawback} is set, otherwise {@code false}.
   */
  public boolean lsfMptCanClawback() {
    return this.isSet(MpTokenIssuanceFlags.CAN_CLAWBACK);
  }

  /**
   * If set, indicates that the MPT supports confidential transfers using privacy-preserving cryptography.
   *
   * @return {@code true} if {@code lsfMPTCanPrivacy} is set, otherwise {@code false}.
   */
  public boolean lsfMptCanPrivacy() {
    return this.isSet(MpTokenIssuanceFlags.CAN_PRIVACY);
  }

}
