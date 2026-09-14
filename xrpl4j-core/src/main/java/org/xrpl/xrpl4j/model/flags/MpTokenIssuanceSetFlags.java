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

  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetCanLock} flag. One-way: enables
   * {@code lsfMPTCanLock} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_CAN_LOCK = new MpTokenIssuanceSetFlags(0x00000004);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetRequireAuth} flag. One-way: enables
   * {@code lsfMPTRequireAuth} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_REQUIRE_AUTH = new MpTokenIssuanceSetFlags(0x00000008);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetCanEscrow} flag. One-way: enables
   * {@code lsfMPTCanEscrow} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_CAN_ESCROW = new MpTokenIssuanceSetFlags(0x00000010);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetCanTrade} flag. One-way: enables
   * {@code lsfMPTCanTrade} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_CAN_TRADE = new MpTokenIssuanceSetFlags(0x00000020);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetCanTransfer} flag. One-way: enables
   * {@code lsfMPTCanTransfer} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_CAN_TRANSFER = new MpTokenIssuanceSetFlags(0x00000040);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetCanClawback} flag. One-way: enables
   * {@code lsfMPTCanClawback} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_CAN_CLAWBACK = new MpTokenIssuanceSetFlags(0x00000080);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTSetCanHoldConfidentialBalance} flag. One-way:
   * enables {@code lsfMPTCanHoldConfidentialBalance} if it has not been made immutable via {@code ImmutableFlags}.
   */
  public static final MpTokenIssuanceSetFlags SET_CAN_HOLD_CONFIDENTIAL_BALANCE =
    new MpTokenIssuanceSetFlags(0x00000100);

  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfInnerBatchTxn} flag.
   */
  public static final MpTokenIssuanceSetFlags INNER_BATCH_TXN = new MpTokenIssuanceSetFlags(
    TransactionFlags.INNER_BATCH_TXN.getValue()
  );

  private MpTokenIssuanceSetFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceSetFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static MpTokenIssuanceSetFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfMPTLock,
    boolean tfMPTUnlock,
    boolean tfMPTSetCanLock,
    boolean tfMPTSetRequireAuth,
    boolean tfMPTSetCanEscrow,
    boolean tfMPTSetCanTrade,
    boolean tfMPTSetCanTransfer,
    boolean tfMPTSetCanClawback,
    boolean tfMPTSetCanHoldConfidentialBalance,
    boolean tfInnerBatchTxn
  ) {
    return new MpTokenIssuanceSetFlags(
      TransactionFlags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfMPTLock ? LOCK : UNSET,
        tfMPTUnlock ? UNLOCK : UNSET,
        tfMPTSetCanLock ? SET_CAN_LOCK : UNSET,
        tfMPTSetRequireAuth ? SET_REQUIRE_AUTH : UNSET,
        tfMPTSetCanEscrow ? SET_CAN_ESCROW : UNSET,
        tfMPTSetCanTrade ? SET_CAN_TRADE : UNSET,
        tfMPTSetCanTransfer ? SET_CAN_TRANSFER : UNSET,
        tfMPTSetCanClawback ? SET_CAN_CLAWBACK : UNSET,
        tfMPTSetCanHoldConfidentialBalance ? SET_CAN_HOLD_CONFIDENTIAL_BALANCE : UNSET,
        tfInnerBatchTxn ? INNER_BATCH_TXN : UNSET
      ).getValue()
    );
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

  /**
   * If set, enables {@code lsfMPTCanLock} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetCanLock} is set, otherwise {@code false}.
   */
  public boolean tfMptSetCanLock() {
    return this.isSet(SET_CAN_LOCK);
  }

  /**
   * If set, enables {@code lsfMPTRequireAuth} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetRequireAuth} is set, otherwise {@code false}.
   */
  public boolean tfMptSetRequireAuth() {
    return this.isSet(SET_REQUIRE_AUTH);
  }

  /**
   * If set, enables {@code lsfMPTCanEscrow} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetCanEscrow} is set, otherwise {@code false}.
   */
  public boolean tfMptSetCanEscrow() {
    return this.isSet(SET_CAN_ESCROW);
  }

  /**
   * If set, enables {@code lsfMPTCanTrade} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetCanTrade} is set, otherwise {@code false}.
   */
  public boolean tfMptSetCanTrade() {
    return this.isSet(SET_CAN_TRADE);
  }

  /**
   * If set, enables {@code lsfMPTCanTransfer} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetCanTransfer} is set, otherwise {@code false}.
   */
  public boolean tfMptSetCanTransfer() {
    return this.isSet(SET_CAN_TRANSFER);
  }

  /**
   * If set, enables {@code lsfMPTCanClawback} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetCanClawback} is set, otherwise {@code false}.
   */
  public boolean tfMptSetCanClawback() {
    return this.isSet(SET_CAN_CLAWBACK);
  }

  /**
   * If set, enables {@code lsfMPTCanHoldConfidentialBalance} on the issuance (one-way; a no-op if already enabled).
   *
   * @return {@code true} if {@code tfMPTSetCanHoldConfidentialBalance} is set, otherwise {@code false}.
   */
  public boolean tfMptSetCanHoldConfidentialBalance() {
    return this.isSet(SET_CAN_HOLD_CONFIDENTIAL_BALANCE);
  }

  /**
   * Whether the {@code tfInnerBatchTxn} flag is set.
   *
   * @return {@code true} if {@code tfInnerBatchTxn} is set, otherwise {@code false}.
   */
  public boolean tfInnerBatchTxn() {
    return this.isSet(INNER_BATCH_TXN);
  }

  /**
   * A builder class for {@link MpTokenIssuanceSetFlags}.
   */
  public static class Builder {

    private boolean tfMptLock = false;
    private boolean tfMptUnlock = false;
    private boolean tfMptSetCanLock = false;
    private boolean tfMptSetRequireAuth = false;
    private boolean tfMptSetCanEscrow = false;
    private boolean tfMptSetCanTrade = false;
    private boolean tfMptSetCanTransfer = false;
    private boolean tfMptSetCanClawback = false;
    private boolean tfMptSetCanHoldConfidentialBalance = false;
    private boolean tfInnerBatchTxn = false;

    /**
     * Set {@code tfMptLock} to the given value.
     *
     * @param tfMptLock A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptLock(boolean tfMptLock) {
      this.tfMptLock = tfMptLock;
      return this;
    }

    /**
     * Set {@code tfMptUnlock} to the given value.
     *
     * @param tfMptUnlock A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptUnlock(boolean tfMptUnlock) {
      this.tfMptUnlock = tfMptUnlock;
      return this;
    }

    /**
     * Set {@code tfMptSetCanLock} to the given value.
     *
     * @param tfMptSetCanLock A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetCanLock(boolean tfMptSetCanLock) {
      this.tfMptSetCanLock = tfMptSetCanLock;
      return this;
    }

    /**
     * Set {@code tfMptSetRequireAuth} to the given value.
     *
     * @param tfMptSetRequireAuth A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetRequireAuth(boolean tfMptSetRequireAuth) {
      this.tfMptSetRequireAuth = tfMptSetRequireAuth;
      return this;
    }

    /**
     * Set {@code tfMptSetCanEscrow} to the given value.
     *
     * @param tfMptSetCanEscrow A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetCanEscrow(boolean tfMptSetCanEscrow) {
      this.tfMptSetCanEscrow = tfMptSetCanEscrow;
      return this;
    }

    /**
     * Set {@code tfMptSetCanTrade} to the given value.
     *
     * @param tfMptSetCanTrade A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetCanTrade(boolean tfMptSetCanTrade) {
      this.tfMptSetCanTrade = tfMptSetCanTrade;
      return this;
    }

    /**
     * Set {@code tfMptSetCanTransfer} to the given value.
     *
     * @param tfMptSetCanTransfer A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetCanTransfer(boolean tfMptSetCanTransfer) {
      this.tfMptSetCanTransfer = tfMptSetCanTransfer;
      return this;
    }

    /**
     * Set {@code tfMptSetCanClawback} to the given value.
     *
     * @param tfMptSetCanClawback A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetCanClawback(boolean tfMptSetCanClawback) {
      this.tfMptSetCanClawback = tfMptSetCanClawback;
      return this;
    }

    /**
     * Set {@code tfMptSetCanHoldConfidentialBalance} to the given value.
     *
     * @param tfMptSetCanHoldConfidentialBalance A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptSetCanHoldConfidentialBalance(boolean tfMptSetCanHoldConfidentialBalance) {
      this.tfMptSetCanHoldConfidentialBalance = tfMptSetCanHoldConfidentialBalance;
      return this;
    }

    /**
     * Set {@code tfInnerBatchTxn} to the given value.
     *
     * @param tfInnerBatchTxn A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfInnerBatchTxn(boolean tfInnerBatchTxn) {
      this.tfInnerBatchTxn = tfInnerBatchTxn;
      return this;
    }

    /**
     * Build a new {@link MpTokenIssuanceSetFlags} from the current boolean values.
     *
     * @return A new {@link MpTokenIssuanceSetFlags}.
     */
    public MpTokenIssuanceSetFlags build() {
      return MpTokenIssuanceSetFlags.of(
        true,
        tfMptLock,
        tfMptUnlock,
        tfMptSetCanLock,
        tfMptSetRequireAuth,
        tfMptSetCanEscrow,
        tfMptSetCanTrade,
        tfMptSetCanTransfer,
        tfMptSetCanClawback,
        tfMptSetCanHoldConfidentialBalance,
        tfInnerBatchTxn
      );
    }
  }

}
