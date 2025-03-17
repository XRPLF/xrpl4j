package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link TransactionFlags} which can be set on
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet} transactions.
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceSetFlags extends TransactionFlags {

  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTLock} flag.
   */
  public static final MpTokenIssuanceSetFlags LOCK = new MpTokenIssuanceSetFlags(0x00000001);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTUnlock} flag.
   */
  public static final MpTokenIssuanceSetFlags UNLOCK = new MpTokenIssuanceSetFlags(0x00000002);

  private MpTokenIssuanceSetFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceSetFlags() {
  }

  /**
   * Construct an empty instance of {@link MpTokenIssuanceSetFlags}. Transactions with empty flags will not be
   * serialized with a {@code Flags} field.
   *
   * @return An empty {@link MpTokenIssuanceSetFlags}.
   */
  public static MpTokenIssuanceSetFlags empty() {
    return new MpTokenIssuanceSetFlags();
  }

  /**
   * If set, indicates that all MPT balances for this asset should be locked.
   *
   * @return {@code true} if {@code tfMPTLock} is set, otherwise {@code false}.
   */
  public boolean tfMptLock() {
    return this.isSet(LOCK);
  }

  /**
   * If set, indicates that all MPT balances for this asset should be unlocked.
   *
   * @return {@code true} if {@code tfMPTUnlock} is set, otherwise {@code false}.
   */
  public boolean tfMptUnlock() {
    return this.isSet(UNLOCK);
  }

}
