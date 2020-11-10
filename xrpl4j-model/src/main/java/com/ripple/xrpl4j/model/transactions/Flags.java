package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;

/**
 * A wrapper around a long value containing various XRPL Transaction Flags.
 *
 * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
 */
public class Flags {

  public static final Flags UNSET = new Flags(0);

  private final long value;

  private Flags(long value) {
    this.value = value;
  }

  public static Flags of(long value) {
    return new Flags(value);
  }

  public static Flags of(Flags flag, Flags... others) {
    return flag.bitwiseOr(
        Arrays.stream(others).reduce(Flags::bitwiseOr).orElse(UNSET)
    );
  }

  @JsonValue
  public long getValue() {
    return value;
  }

  /**
   * Performs a bitwise OR on this {@link Flags} and another {@link Flags}
   *
   * @param other The {@link Flags} to perform the OR with.
   * @return The {@link Flags} resulting from the OR operation.
   */
  Flags bitwiseOr(Flags other) {
    return Flags.of(this.value | other.value);
  }

  /**
   * Performs a bitwise AND on this {@link Flags} and another {@link Flags}
   *
   * @param other The {@link Flags} to perform the AND with.
   * @return The {@link Flags} resulting from the AND operation.
   */
  Flags bitwiseAnd(Flags other) {
    return Flags.of(this.value & other.value);
  }

  /**
   * Determines if a specific transaction flag is set by performing a bitwise AND on this {@link Flags} and the {@link
   * Flags} in question, and checking if the result of that operation is equal to the given flag.
   *
   * @param flag The {@link Flags} that this method determines is set or not.
   * @return true if the flag is set, false if not.
   */
  // TODO: Unit test.
  public boolean isSet(Flags flag) {
    return !flag.equals(Flags.UNSET) && this.bitwiseAnd(flag).equals(flag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    Flags flags = (Flags) o;
    return getValue() == flags.getValue();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  /**
   * A set of static Universal {@link Flags} which could apply to any transaction.
   */
  public static class TransactionFlags extends Flags {

    private static final TransactionFlags FULLY_CANONICAL_SIG = new TransactionFlags(0x80000000L);

    private TransactionFlags(long value) {
      super(value);
    }

    /**
     * Flags indicating that a fully-canonical signature is required. This flag is highly recommended.
     *
     * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
     */
    public boolean tfFullyCanonicalSig() {
      return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
    }

    /**
     * A builder class for {@link TransactionFlags} flags.
     */
    public static class Builder {

      private boolean tfFullyCanonicalSig = true;

      public TransactionFlags.Builder fullyCanonicalSig(boolean value) {
        this.tfFullyCanonicalSig = value;
        return this;
      }

      public TransactionFlags build() {
        return new TransactionFlags(
            tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG.getValue() : UNSET.getValue()
        );
      }
    }
  }

  /**
   * A set of static {@link Flags} which can be set on {@link Payment} transactions.
   */
  public static class PaymentFlags extends TransactionFlags {

    public static final PaymentFlags UNSET = new PaymentFlags(0);

    // TODO: Consider making these private so that the only way to create flags is to use the builder. This way a
    //  developer won't accidentally set a single flag in the builder (e.g., AccountSet) that accidentally deletes other
    // flags that exist in the server. This is especially accute for AccountSet and other transaction object that have
    // pre-existing state on the ledger.
    public static final PaymentFlags NO_DIRECT_RIPPLE = new PaymentFlags(0x00010000L);
    public static final PaymentFlags PARTIAL_PAYMENT_FLAGS = new PaymentFlags(0x00020000L);
    public static final PaymentFlags LIMIT_QUALITY = new PaymentFlags(0x00040000L);

    /**
     * Flag indicated to only use paths included in the {@link com.ripple.xrpl4j.model.transactions.Payment#paths()}
     * field. This is intended to force the transaction to take arbitrage opportunities. Most clients do not need this.
     */
    public boolean tfNoDirectRipple() {
      return this.isSet(PaymentFlags.NO_DIRECT_RIPPLE);
    }

    /**
     * If the specified {@link com.ripple.xrpl4j.model.transactions.Payment#amount()} cannot be sent without spending
     * more than {@link com.ripple.xrpl4j.model.transactions.Payment#sendMax()}, reduce the received amount instead of
     * failing outright.
     *
     * @see "https://xrpl.org/partial-payments.html"
     */
    public boolean tfPartialPayment() {
      return this.isSet(PaymentFlags.PARTIAL_PAYMENT_FLAGS);
    }

    /**
     * Only take paths where all the conversions have an input:output ratio that is equal or better than the ratio of
     * {@link com.ripple.xrpl4j.model.transactions.Payment#amount()}:{@link com.ripple.xrpl4j.model.transactions.Payment#sendMax()}.
     *
     * @return
     */
    public boolean tfLimitQuality() {
      return this.isSet(PaymentFlags.LIMIT_QUALITY);
    }

    public static PaymentFlags.Builder builder() {
      return new PaymentFlags.Builder();
    }

    public static PaymentFlags of(long value) {
      return new PaymentFlags(value);
    }

    private static PaymentFlags of(boolean tfFullyCanonicalSig, boolean tfNoDirectRipple, boolean tfPartialPayment,
        boolean tfLimitQuality) {
      return new PaymentFlags(of(
          tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
          tfNoDirectRipple ? NO_DIRECT_RIPPLE : UNSET,
          tfPartialPayment ? PARTIAL_PAYMENT_FLAGS : UNSET,
          tfLimitQuality ? LIMIT_QUALITY : UNSET
      ).getValue());
    }

    /**
     * A builder class for {@link PaymentFlags} flags.
     */
    public static class Builder {

      private boolean tfFullyCanonicalSig = true;
      private boolean tfNoDirectRipple = false;
      private boolean tfPartialPayment = false;
      private boolean tfLimitQuality = false;

      public Builder fullyCanonicalSig(boolean value) {
        this.tfFullyCanonicalSig = value;
        return this;
      }

      public Builder noDirectRipple(boolean value) {
        this.tfNoDirectRipple = value;
        return this;
      }

      public Builder partialPayment(boolean value) {
        this.tfPartialPayment = value;
        return this;
      }

      public Builder limitQuality(boolean value) {
        this.tfLimitQuality = value;
        return this;
      }

      public PaymentFlags build() {
        return PaymentFlags.of(tfFullyCanonicalSig, tfNoDirectRipple, tfPartialPayment, tfLimitQuality);
      }
    }

    private PaymentFlags(long value) {
      super(value);
    }
  }

  /**
   * A set of static {@link Flags} which can be set on {@link AccountSet} transactions.
   */
  public static class AccountRootFlags extends Flags {

    public static final AccountRootFlags UNSET = new AccountRootFlags(0);

    /**
     * Enable rippling on this addresses's trust lines by default. Required for issuing addresses; discouraged for
     * others.
     */
    public static final AccountRootFlags DEFAULT_RIPPLE = new AccountRootFlags(0x00800000L);

    /**
     * This account can only receive funds from transactions it sends, and from preauthorized accounts. (It has
     * DepositAuth enabled.)
     */
    public static final AccountRootFlags DEPOSIT_AUTH = new AccountRootFlags(0x01000000);

    /**
     * Disallows use of the master key to sign transactions for this account.
     */
    public static final AccountRootFlags DISABLE_MASTER = new AccountRootFlags(0x00100000);

    /**
     * Client applications should not send XRP to this account. Not enforced by rippled.
     */
    public static final AccountRootFlags DISALLOW_XRP = new AccountRootFlags(0x00080000L);

    /**
     * All assets issued by this address are frozen.
     */
    public static final AccountRootFlags GLOBAL_FREEZE = new AccountRootFlags(0x00400000);

    /**
     * This address cannot freeze trust lines connected to it. Once enabled, cannot be disabled.
     */
    public static final AccountRootFlags NO_FREEZE = new AccountRootFlags(0x00200000);

    /**
     * The account has used its free SetRegularKey transaction.
     */
    public static final AccountRootFlags PASSWORD_SPENT = new AccountRootFlags(0x00010000);

    /**
     * This account must individually approve other users for those users to hold this account's issued currencies.
     */
    public static final AccountRootFlags REQUIRE_AUTH = new AccountRootFlags(0x00040000);

    /**
     * Requires incoming payments to specify a Destination Tag.
     */
    public static final AccountRootFlags REQUIRE_DEST_TAG = new AccountRootFlags(0x00020000);

    /**
     * Required-args Constructor.
     *
     * @param value The long-number encoded flags value of this {@link AccountRootFlags}.
     */
    private AccountRootFlags(final long value) {
      super(value);
    }

    /**
     * Flag indicated to only use paths included in the {@link com.ripple.xrpl4j.model.transactions.Payment#paths()}
     * field. This is intended to force the transaction to take arbitrage opportunities. Most clients do not need this.
     */
    public boolean lsfDefaultRipple() {
      return this.isSet(AccountRootFlags.DEFAULT_RIPPLE);
    }

    /**
     * This account can only receive funds from transactions it sends, and from preauthorized accounts. (It has
     * DepositAuth enabled.)
     */
    public boolean lsfDepositAuth() {
      return this.isSet(AccountRootFlags.DEPOSIT_AUTH);
    }

    /**
     * Disallows use of the master key to sign transactions for this account.
     */
    public boolean lsfDisableMaster() {
      return this.isSet(AccountRootFlags.DISABLE_MASTER);
    }

    /**
     * Client applications should not send XRP to this account. Not enforced by rippled.
     */
    public boolean lsfDisallowXRP() {
      return this.isSet(AccountRootFlags.DISALLOW_XRP);
    }

    /**
     * All assets issued by this address are frozen.
     */
    public boolean lsfGlobalFreeze() {
      return this.isSet(AccountRootFlags.GLOBAL_FREEZE);
    }

    /**
     * This address cannot freeze trust lines connected to it. Once enabled, cannot be disabled.
     */
    public boolean lsfNoFreeze() {
      return this.isSet(AccountRootFlags.NO_FREEZE);
    }

    /**
     * The account has used its free SetRegularKey transaction.
     */
    public boolean lsfPasswordSpent() {
      return this.isSet(AccountRootFlags.PASSWORD_SPENT);
    }

    /**
     * This account must individually approve other users for those users to hold this account's issued currencies.
     */
    public boolean lsfRequireAuth() {
      return this.isSet(AccountRootFlags.REQUIRE_AUTH);
    }

    /**
     * Requires incoming payments to specify a Destination Tag.
     */
    public boolean lsfRequireDestTag() {
      return this.isSet(AccountRootFlags.REQUIRE_DEST_TAG);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static AccountRootFlags of(long value) {
      return new AccountRootFlags(value);
    }

    public static AccountRootFlags of(
        boolean tfFullyCanonicalSig,
        boolean lsfDefaultRipple,
        boolean lsfDepositAuth,
        boolean lsfDisableMaster,
        boolean lsfDisallowXRP,
        boolean lsfGlobalFreeze,
        boolean lsfNoFreeze,
        boolean lsfPasswordSpent,
        boolean lsfRequireAuth,
        boolean lsfRequireDestTag
    ) {
      return new AccountRootFlags(
          Flags.of(
              tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
              lsfDefaultRipple ? AccountRootFlags.DEFAULT_RIPPLE : UNSET,
              lsfDepositAuth ? AccountRootFlags.DEPOSIT_AUTH : UNSET,
              lsfDisableMaster ? AccountRootFlags.DISABLE_MASTER : UNSET,
              lsfDisallowXRP ? AccountRootFlags.DISALLOW_XRP : UNSET,
              lsfGlobalFreeze ? AccountRootFlags.GLOBAL_FREEZE : UNSET,
              lsfNoFreeze ? AccountRootFlags.NO_FREEZE : UNSET,
              lsfPasswordSpent ? AccountRootFlags.PASSWORD_SPENT : UNSET,
              lsfRequireAuth ? AccountRootFlags.REQUIRE_AUTH : UNSET,
              lsfRequireDestTag ? AccountRootFlags.REQUIRE_DEST_TAG : UNSET
          ).getValue());
    }

    /**
     * A builder class for {@link PaymentFlags} flags.
     */
    public static class Builder {

      private boolean tfFullyCanonicalSig = true;
      private boolean lsfDefaultRipple = false;
      private boolean lsfDepositAuth = false;
      private boolean lsfDisableMaster = false;
      private boolean lsfDisallowXRP = false;
      private boolean lsfGlobalFreeze = false;
      private boolean lsfNoFreeze = false;
      private boolean lsfPasswordSpent = false;
      private boolean lsfRequireAuth = false;
      private boolean lsfRequireDestTag = false;

      public AccountRootFlags.Builder fullyCanonicalSig(boolean value) {
        this.tfFullyCanonicalSig = value;
        return this;
      }

      public AccountRootFlags.Builder defaultRipple(boolean value) {
        this.lsfDefaultRipple = value;
        return this;
      }

      public AccountRootFlags.Builder depositAuth(boolean value) {
        this.lsfDepositAuth = value;
        return this;
      }

      public AccountRootFlags.Builder disableMaster(boolean value) {
        this.lsfDisableMaster = value;
        return this;
      }

      public AccountRootFlags.Builder disallowXRP(boolean value) {
        this.lsfDisallowXRP = value;
        return this;
      }

      public AccountRootFlags.Builder globalFreeze(boolean value) {
        this.lsfGlobalFreeze = value;
        return this;
      }

      public AccountRootFlags.Builder noFreeze(boolean value) {
        this.lsfNoFreeze = value;
        return this;
      }

      public AccountRootFlags.Builder passwordSpent(boolean value) {
        this.lsfPasswordSpent = value;
        return this;
      }

      public AccountRootFlags.Builder requireAuth(boolean value) {
        this.lsfRequireAuth = value;
        return this;
      }

      public AccountRootFlags.Builder requireDestTag(boolean value) {
        this.lsfRequireDestTag = value;
        return this;
      }

      public AccountRootFlags build() {
        return AccountRootFlags.of(
            tfFullyCanonicalSig,
            lsfDefaultRipple, lsfDepositAuth, lsfDisableMaster, lsfDisallowXRP, lsfGlobalFreeze, lsfNoFreeze,
            lsfPasswordSpent, lsfRequireAuth, lsfRequireDestTag
        );
      }
    }
  }

  public static class SignerListFlags extends Flags {

    public static final SignerListFlags UNSET = new SignerListFlags(0);

    /**
     * If this flag is enabled, this SignerList counts as one item for purposes of the owner reserve.
     * Otherwise, this list counts as N+2 items, where N is the number of signers it contains. This flag is
     * automatically enabled if you add or update a signer list after the MultiSignReserve amendment is enabled.
     */
    public static final SignerListFlags ONE_OWNER_COUNT = new SignerListFlags(0x00010000);

    private SignerListFlags(long value) {
      super(value);
    }

    private static SignerListFlags of(boolean lsfOneOwnerCount) {
      return new SignerListFlags(Flags.of(lsfOneOwnerCount ? SignerListFlags.ONE_OWNER_COUNT : UNSET).getValue());
    }

    public boolean lsfOneOwnerCount() {
      return this.isSet(SignerListFlags.ONE_OWNER_COUNT);
    }
    public static class Builder {

      boolean lsfOneOwnerCount = false;

      public SignerListFlags.Builder lsfOneOwnerCount(boolean value) {
        this.lsfOneOwnerCount = value;
        return this;
      }
      public SignerListFlags build() {
        return SignerListFlags.of(lsfOneOwnerCount);
      }

    }
  }

  public static class TrustSetFlags extends TransactionFlags {

    public static final TrustSetFlags UNSET = new TrustSetFlags(0);

    public static final TrustSetFlags SET_F_AUTH = new TrustSetFlags(0x00010000);
    public static final TrustSetFlags SET_NO_RIPPLE = new TrustSetFlags(0x00020000);
    public static final TrustSetFlags CLEAR_NO_RIPPLE = new TrustSetFlags(0x00040000);
    public static final TrustSetFlags SET_FREEZE = new TrustSetFlags(0x00100000);
    public static final TrustSetFlags CLEAR_FREEZE = new TrustSetFlags(0x00200000);

    public static TrustSetFlags.Builder builder() {
      return new TrustSetFlags.Builder();
    }

    private TrustSetFlags(long value) {
      super(value);
    }

    private static TrustSetFlags of(
      boolean tfFullyCanonicalSig,
      boolean tfSetfAuth,
      boolean tfSetNoRipple,
      boolean tfClearNoRipple,
      boolean tfSetFreeze,
      boolean tfClearFreeze
    ) {
      return new TrustSetFlags(
        Flags.of(
          tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
          tfSetfAuth ? SET_F_AUTH : UNSET,
          tfSetNoRipple ? SET_NO_RIPPLE : UNSET,
          tfClearNoRipple ? CLEAR_NO_RIPPLE : UNSET,
          tfSetFreeze ? SET_FREEZE : UNSET,
          tfClearFreeze ? CLEAR_FREEZE : UNSET).getValue()
      );
    }

    public boolean tfFullyCanonicalSig() {
      return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
    }

    public boolean tfSetfAuth() {
      return this.isSet(SET_F_AUTH);
    }

    public boolean tfSetNoRipple() {
      return this.isSet(SET_NO_RIPPLE);
    }

    public boolean tfClearNoRipple() {
      return this.isSet(CLEAR_NO_RIPPLE);
    }

    public boolean tfSetFreeze() {
      return this.isSet(SET_FREEZE);
    }

    public boolean tfClearFreeze() {
      return this.isSet(CLEAR_FREEZE);
    }

    public static class Builder {
      boolean tfFullyCanonicalSig = true;
      boolean tfSetfAuth = false;
      boolean tfSetNoRipple = false;
      boolean tfClearNoRipple = false;
      boolean tfSetFreeze = false;
      boolean tfClearFreeze = false;

      public Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      public TrustSetFlags.Builder tfSetfAuth(boolean tfSetfAuth) {
        this.tfSetfAuth = tfSetfAuth;
        return this;
      }

      public TrustSetFlags.Builder tfSetNoRipple(boolean tfSetNoRipple) {
        this.tfSetNoRipple = tfSetNoRipple;
        return this;
      }

      public TrustSetFlags.Builder tfClearNoRipple(boolean tfClearNoRipple) {
        this.tfClearNoRipple = tfClearNoRipple;
        return this;
      }

      public TrustSetFlags.Builder tfSetFreeze(boolean tfSetFreeze) {
        this.tfSetFreeze = tfSetFreeze;
        return this;
      }

      public TrustSetFlags.Builder tfClearFreeze(boolean tfClearFreeze) {
        this.tfClearFreeze = tfClearFreeze;
        return this;
      }

      public TrustSetFlags build() {
        return TrustSetFlags.of(
          tfFullyCanonicalSig,
          tfSetfAuth,
          tfSetNoRipple,
          tfClearNoRipple,
          tfSetFreeze,
          tfClearFreeze
        );
      }
    }
  }
}
