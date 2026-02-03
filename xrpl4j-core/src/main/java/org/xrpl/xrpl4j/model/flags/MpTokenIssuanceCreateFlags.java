package org.xrpl.xrpl4j.model.flags;

/**
 * A set of static {@link TransactionFlags} which can be set on
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate} transactions.
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceCreateFlags extends TransactionFlags {

  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTCanLock} flag.
   */
  protected static final MpTokenIssuanceCreateFlags CAN_LOCK = new MpTokenIssuanceCreateFlags(0x00000002);
  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTRequireAuth} flag.
   */
  protected static final MpTokenIssuanceCreateFlags REQUIRE_AUTH = new MpTokenIssuanceCreateFlags(0x00000004);
  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTCanEscrow} flag.
   */
  protected static final MpTokenIssuanceCreateFlags CAN_ESCROW = new MpTokenIssuanceCreateFlags(0x00000008);
  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTCanTrade} flag.
   */
  protected static final MpTokenIssuanceCreateFlags CAN_TRADE = new MpTokenIssuanceCreateFlags(0x00000010);
  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTCanTransfer} flag.
   */
  protected static final MpTokenIssuanceCreateFlags CAN_TRANSFER = new MpTokenIssuanceCreateFlags(0x00000020);
  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTCanClawback} flag.
   */
  protected static final MpTokenIssuanceCreateFlags CAN_CLAWBACK = new MpTokenIssuanceCreateFlags(0x00000040);
  /**
   * Constant {@link MpTokenIssuanceCreateFlags} for the {@code tfMPTCanPrivacy} flag.
   */
  protected static final MpTokenIssuanceCreateFlags CAN_PRIVACY = new MpTokenIssuanceCreateFlags(0x00000080);


  private MpTokenIssuanceCreateFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceCreateFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static MpTokenIssuanceCreateFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfMPTCanLock,
    boolean tfMPTRequireAuth,
    boolean tfMPTCanEscrow,
    boolean tfMPTCanTrade,
    boolean tfMPTCanTransfer,
    boolean tfMPTCanClawback,
    boolean tfMPTCanPrivacy
  ) {
    return new MpTokenIssuanceCreateFlags(
      TransactionFlags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfMPTCanLock ? CAN_LOCK : UNSET,
        tfMPTRequireAuth ? REQUIRE_AUTH : UNSET,
        tfMPTCanEscrow ? CAN_ESCROW : UNSET,
        tfMPTCanTransfer ? CAN_TRANSFER : UNSET,
        tfMPTCanTrade ? CAN_TRADE : UNSET,
        tfMPTCanClawback ? CAN_CLAWBACK : UNSET,
        tfMPTCanPrivacy ? CAN_PRIVACY : UNSET
      ).getValue()
    );
  }

  /**
   * Construct an empty instance of {@link MpTokenIssuanceCreateFlags}. Transactions with empty flags will not be
   * serialized with a {@code Flags} field.
   *
   * @return An empty {@link MpTokenIssuanceCreateFlags}.
   */
  public static MpTokenIssuanceCreateFlags empty() {
    return new MpTokenIssuanceCreateFlags();
  }

  /**
   * If set, indicates that the MPT can be locked both individually and globally. If not set, the MPT cannot be locked
   * in any way.
   *
   * @return {@code true} if {@code tfMPTCanLock} is set, otherwise {@code false}.
   */
  public boolean tfMptCanLock() {
    return this.isSet(CAN_LOCK);
  }

  /**
   * If set, indicates that individual holders must be authorized. This enables issuers to limit who can hold their
   * assets.
   *
   * @return {@code true} if {@code tfMPTRequireAuth} is set, otherwise {@code false}.
   */
  public boolean tfMptRequireAuth() {
    return this.isSet(REQUIRE_AUTH);
  }

  /**
   * If set, indicates that individual holders can place their balances into an escrow.
   *
   * @return {@code true} if {@code tfMPTCanEscrow} is set, otherwise {@code false}.
   */
  public boolean tfMptCanEscrow() {
    return this.isSet(CAN_ESCROW);
  }

  /**
   * If set, indicates that individual holders can trade their balances using the XRP Ledger DEX.
   *
   * @return {@code true} if {@code tfMPTCanTrade} is set, otherwise {@code false}.
   */
  public boolean tfMptCanTrade() {
    return this.isSet(CAN_TRADE);
  }

  /**
   * If set, indicates that tokens may be transferred by any account (issuer or non-issuer) to any account (issuer or
   * non-issuer). If unset, indicates that tokens may only be transferred from the issuer to any single account (or back
   * to the issuer) but that tokens may not be transferred between non-issuer accounts.
   *
   * @return {@code true} if {@code tfMPTCanTransfer} is set, otherwise {@code false}.
   */
  public boolean tfMptCanTransfer() {
    return this.isSet(CAN_TRANSFER);
  }

  /**
   * If set, indicates that the issuer may use the Clawback transaction to clawback value from individual holders.
   *
   * @return {@code true} if {@code tfMPTCanClawback} is set, otherwise {@code false}.
   */
  public boolean tfMptCanClawback() {
    return this.isSet(CAN_CLAWBACK);
  }

  /**
   * If set, indicates that the MPT supports confidential transfers using privacy-preserving cryptography.
   *
   * @return {@code true} if {@code tfMPTCanPrivacy} is set, otherwise {@code false}.
   */
  public boolean tfMptCanPrivacy() {
    return this.isSet(CAN_PRIVACY);
  }

  /**
   * A builder class for {@link MpTokenIssuanceCreateFlags}.
   */
  public static class Builder {

    private boolean tfMptCanLock = false;
    private boolean tfMptRequireAuth = false;
    private boolean tfMptCanEscrow = false;
    private boolean tfMptCanTrade = false;
    private boolean tfMptCanTransfer = false;
    private boolean tfMptCanClawback = false;
    private boolean tfMptCanPrivacy = false;

    /**
     * Set {@code tfMptCanLock} to the given value.
     *
     * @param tfMptCanLock A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptCanLock(boolean tfMptCanLock) {
      this.tfMptCanLock = tfMptCanLock;
      return this;
    }

    /**
     * Set {@code tfMptRequireAuth} to the given value.
     *
     * @param tfMptRequireAuth A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptRequireAuth(boolean tfMptRequireAuth) {
      this.tfMptRequireAuth = tfMptRequireAuth;
      return this;
    }

    /**
     * Set {@code tfMptCanEscrow} to the given value.
     *
     * @param tfMptCanEscrow A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptCanEscrow(boolean tfMptCanEscrow) {
      this.tfMptCanEscrow = tfMptCanEscrow;
      return this;
    }

    /**
     * Set {@code tfMptCanTrade} to the given value.
     *
     * @param tfMptCanTrade A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptCanTrade(boolean tfMptCanTrade) {
      this.tfMptCanTrade = tfMptCanTrade;
      return this;
    }

    /**
     * Set {@code tfMptCanTransfer} to the given value.
     *
     * @param tfMptCanTransfer A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptCanTransfer(boolean tfMptCanTransfer) {
      this.tfMptCanTransfer = tfMptCanTransfer;
      return this;
    }

    /**
     * Set {@code tfMptCanClawback} to the given value.
     *
     * @param tfMptCanClawback A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptCanClawback(boolean tfMptCanClawback) {
      this.tfMptCanClawback = tfMptCanClawback;
      return this;
    }

    /**
     * Set {@code tfMptCanPrivacy} to the given value.
     *
     * @param tfMptCanPrivacy A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfMptCanPrivacy(boolean tfMptCanPrivacy) {
      this.tfMptCanPrivacy = tfMptCanPrivacy;
      return this;
    }

    /**
     * Build a new {@link MpTokenIssuanceCreateFlags} from the current boolean values.
     *
     * @return A new {@link MpTokenIssuanceCreateFlags}.
     */
    public MpTokenIssuanceCreateFlags build() {
      return MpTokenIssuanceCreateFlags.of(
        true,
        tfMptCanLock,
        tfMptRequireAuth,
        tfMptCanEscrow,
        tfMptCanTrade,
        tfMptCanTransfer,
        tfMptCanClawback,
        tfMptCanPrivacy
      );
    }
  }
}
