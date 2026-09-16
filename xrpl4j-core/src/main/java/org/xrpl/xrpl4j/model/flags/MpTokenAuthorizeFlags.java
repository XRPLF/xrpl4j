package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link TransactionFlags} which can be set on
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize} transactions.
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenAuthorizeFlags extends TransactionFlags {

  /**
   * Constant {@link MpTokenAuthorizeFlags} for the {@code tfMPTLock} flag.
   */
  public static final MpTokenAuthorizeFlags UNAUTHORIZE = new MpTokenAuthorizeFlags(0x00000001);

  /**
   * Constant {@link MpTokenAuthorizeFlags} for the {@code tfInnerBatchTxn} flag.
   */
  public static final MpTokenAuthorizeFlags INNER_BATCH_TXN = new MpTokenAuthorizeFlags(
    TransactionFlags.INNER_BATCH_TXN.getValue());

  private MpTokenAuthorizeFlags(long value) {
    super(value);
  }

  private MpTokenAuthorizeFlags() {
  }

  /**
   * Construct an empty instance of {@link MpTokenAuthorizeFlags}. Transactions with empty flags will not be serialized
   * with a {@code Flags} field.
   *
   * @return An empty {@link MpTokenAuthorizeFlags}.
   */
  public static MpTokenAuthorizeFlags empty() {
    return new MpTokenAuthorizeFlags();
  }

  /**
   * If set and transaction is submitted by a holder, it indicates that the holder no longer wants to hold the MPToken,
   * which will be deleted as a result. If the the holder's MPToken has non-zero balance while trying to set this flag,
   * the transaction will fail. On the other hand, if set and transaction is submitted by an issuer, it would mean that
   * the issuer wants to unauthorize the holder (only applicable for allow-listing), which would unset the
   * lsfMPTAuthorized flag on the MPToken.
   *
   * @return {@code true} if {@code tfMPTUnauthorize} is set, otherwise {@code false}.
   */
  public boolean tfMptUnauthorize() {
    return this.isSet(UNAUTHORIZE);
  }

  /**
   * Whether the {@code tfInnerBatchTxn} flag is set.
   *
   * @return {@code true} if {@code tfInnerBatchTxn} is set, otherwise {@code false}.
   */
  public boolean tfInnerBatchTxn() {
    return this.isSet(INNER_BATCH_TXN);
  }

}
