package org.xrpl.xrpl4j.model.flags;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;

import java.util.Arrays;
import java.util.Objects;

/**
 * A wrapper around a long value containing various XRPL Transaction Flags.
 *
 * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
 */
public class Flags {

  /**
   * Constant for an unset flag.
   */
  public static final Flags UNSET = new Flags(0);

  private final long value;

  private Flags(long value) {
    this.value = value;
  }

  /**
   * Construct {@link Flags} for the given value.
   *
   * @param value A long flags value.
   *
   * @return A new {@link Flags}.
   */
  public static Flags of(long value) {
    return new Flags(value);
  }

  /**
   * Construct {@link Flags} from one or more {@link Flags} by performing a bitwise OR on all.
   *
   * @param flag   The first {@link Flags}.
   * @param others Zero or more other {@link Flags} to include.
   *
   * @return A new {@link Flags}.
   */
  public static Flags of(Flags flag, Flags... others) {
    return flag.bitwiseOr(
      Arrays.stream(others).reduce(Flags::bitwiseOr).orElse(UNSET)
    );
  }

  /**
   * Get the underlying value of this {@link Flags}.
   *
   * @return The underlying {@code long} value of this {@link Flags}.
   */
  @JsonValue
  public long getValue() {
    return value;
  }

  /**
   * Performs a bitwise OR on this {@link Flags} and another {@link Flags}.
   *
   * @param other The {@link Flags} to perform the OR with.
   *
   * @return The {@link Flags} resulting from the OR operation.
   */
  Flags bitwiseOr(Flags other) {
    return Flags.of(this.value | other.value);
  }

  /**
   * Performs a bitwise AND on this {@link Flags} and another {@link Flags}.
   *
   * @param other The {@link Flags} to perform the AND with.
   *
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
   *
   * @return true if the flag is set, false if not.
   */
  // TODO: Unit test.
  public boolean isSet(Flags flag) {
    return !flag.equals(Flags.UNSET) && this.bitwiseAnd(flag).equals(flag);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    Flags flags = (Flags) obj;
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
   * A set of static {@link Flags} which could apply to any {@link org.xrpl.xrpl4j.model.transactions.Transaction}.
   */
  public static class TransactionFlags extends Flags {

    /**
     * Corresponds to the {@code tfFullyCanonicalSig} flag.
     */
    protected static final TransactionFlags FULLY_CANONICAL_SIG = new TransactionFlags(0x80000000L);

    private TransactionFlags(long value) {
      super(value);
    }

    /**
     * Flags indicating that a fully-canonical signature is required. This flag is highly recommended.
     *
     * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
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

      /**
       * Set {@code tfFullyCanonicalSig} to the given boolean value.
       *
       * @param tfFullyCanonicalSig A boolean value.
       *
       * @return A {@link TransactionFlags.Builder}.
       */
      public TransactionFlags.Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Build a {@link TransactionFlags}.
       *
       * @return {@link TransactionFlags}.
       */
      public TransactionFlags build() {
        return new TransactionFlags(
          tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG.getValue() : UNSET.getValue()
        );
      }
    }
  }

  /**
   * A set of static {@link TransactionFlags} which can be set on {@link Payment} transactions.
   */
  public static class PaymentFlags extends TransactionFlags {

    /**
     * Constant {@link PaymentFlags} for an unset flag.
     */
    public static final PaymentFlags UNSET = new PaymentFlags(0);

    /**
     * Constant {@link PaymentFlags} for the {@code tfNoDirectRipple} flag.
     */
    protected static final PaymentFlags NO_DIRECT_RIPPLE = new PaymentFlags(0x00010000L);

    /**
     * Constant {@link PaymentFlags} for the {@code tfPartialPayment} flag.
     */
    protected static final PaymentFlags PARTIAL_PAYMENT = new PaymentFlags(0x00020000L);

    /**
     * Constant {@link PaymentFlags} for the {@code tfLimitQuality} flag.
     */
    protected static final PaymentFlags LIMIT_QUALITY = new PaymentFlags(0x00040000L);

    private PaymentFlags(long value) {
      super(value);
    }

    /**
     * Create a new {@link PaymentFlags.Builder}.
     *
     * @return A new {@link PaymentFlags.Builder}.
     */
    public static PaymentFlags.Builder builder() {
      return new PaymentFlags.Builder();
    }

    /**
     * Construct {@link PaymentFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link PaymentFlags}.
     *
     * @return New {@link PaymentFlags}.
     */
    public static PaymentFlags of(long value) {
      return new PaymentFlags(value);
    }

    private static PaymentFlags of(boolean tfFullyCanonicalSig, boolean tfNoDirectRipple, boolean tfPartialPayment,
                                   boolean tfLimitQuality) {
      return new PaymentFlags(of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfNoDirectRipple ? NO_DIRECT_RIPPLE : UNSET,
        tfPartialPayment ? PARTIAL_PAYMENT : UNSET,
        tfLimitQuality ? LIMIT_QUALITY : UNSET
      ).getValue());
    }

    /**
     * Do not use the default path; only use paths included in the {@link Payment#paths()} field. This is intended
     * to force the transaction to take arbitrage opportunities. Most clients do not need this.
     *
     * @return {@code true} if {@code tfNoDirectRipple} is set, otherwise {@code false}.
     */
    public boolean tfNoDirectRipple() {
      return this.isSet(PaymentFlags.NO_DIRECT_RIPPLE);
    }

    /**
     * If the specified {@link Payment#amount()} cannot be sent without spending
     * more than {@link Payment#sendMax()}, reduce the received amount instead of
     * failing outright.
     *
     * @return {@code true} if {@code tfPartialPayment} is set, otherwise {@code false}.
     * @see "https://xrpl.org/partial-payments.html"
     */
    public boolean tfPartialPayment() {
      return this.isSet(PaymentFlags.PARTIAL_PAYMENT);
    }

    /**
     * Only take paths where all the conversions have an input:output ratio that is equal or better than the ratio of
     * {@link Payment#amount()}:{@link Payment#sendMax()}.
     *
     * @return {@code true} if {@code tfLimitQuality} is set, otherwise {@code false}.
     */
    public boolean tfLimitQuality() {
      return this.isSet(PaymentFlags.LIMIT_QUALITY);
    }

    /**
     * A builder class for {@link PaymentFlags} flags.
     */
    public static class Builder {

      private boolean tfFullyCanonicalSig = true;
      private boolean tfNoDirectRipple = false;
      private boolean tfPartialPayment = false;
      private boolean tfLimitQuality = false;


      /**
       * Set {@code tfFullyCanonicalSig} to the given value.
       *
       * @param tfFullyCanonicalSig A boolean value.
       *
       * @return The same {@link PaymentFlags.Builder}.
       */
      public Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Set {@code tfNoDirectRipple} to the given value.
       *
       * @param tfNoDirectRipple A boolean value.
       *
       * @return The same {@link PaymentFlags.Builder}.
       */
      public Builder tfNoDirectRipple(boolean tfNoDirectRipple) {
        this.tfNoDirectRipple = tfNoDirectRipple;
        return this;
      }

      /**
       * Set {@code tfPartialPayment} to the given value.
       *
       * @param tfPartialPayment A boolean value.
       *
       * @return The same {@link PaymentFlags.Builder}.
       */
      public Builder tfPartialPayment(boolean tfPartialPayment) {
        this.tfPartialPayment = tfPartialPayment;
        return this;
      }

      /**
       * Set {@code tfLimitQuality} to the given value.
       *
       * @param tfLimitQuality A boolean value.
       *
       * @return The same {@link PaymentFlags.Builder}.
       */
      public Builder tfLimitQuality(boolean tfLimitQuality) {
        this.tfLimitQuality = tfLimitQuality;
        return this;
      }

      /**
       * Build a new {@link PaymentFlags} from the current boolean values.
       *
       * @return A new {@link PaymentFlags}.
       */
      public PaymentFlags build() {
        return PaymentFlags.of(tfFullyCanonicalSig, tfNoDirectRipple, tfPartialPayment, tfLimitQuality);
      }
    }
  }

  /**
   * A set of static {@link Flags} which can be set on {@link AccountSet} transactions.
   */
  public static class AccountRootFlags extends Flags {

    /**
     * Constant for an unset flag.
     */
    public static final AccountRootFlags UNSET = new AccountRootFlags(0);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfDefaultRipple} account flag.
     */
    public static final AccountRootFlags DEFAULT_RIPPLE = new AccountRootFlags(0x00800000L);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfDepositAuth} account flag.
     */
    public static final AccountRootFlags DEPOSIT_AUTH = new AccountRootFlags(0x01000000);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfDisableMaster} account flag.
     */
    public static final AccountRootFlags DISABLE_MASTER = new AccountRootFlags(0x00100000);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfDisallowXRP} account flag.
     */
    public static final AccountRootFlags DISALLOW_XRP = new AccountRootFlags(0x00080000L);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfGlobalFreeze} account flag.
     */
    public static final AccountRootFlags GLOBAL_FREEZE = new AccountRootFlags(0x00400000);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfNoFreeze} account flag.
     */
    public static final AccountRootFlags NO_FREEZE = new AccountRootFlags(0x00200000);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfPasswordSpent} account flag.
     */
    public static final AccountRootFlags PASSWORD_SPENT = new AccountRootFlags(0x00010000);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfRequireAuth} account flag.
     */
    public static final AccountRootFlags REQUIRE_AUTH = new AccountRootFlags(0x00040000);

    /**
     * Constant {@link AccountRootFlags} for the {@code lsfRequireDestTag} account flag.
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
     * Construct {@link AccountRootFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link AccountRootFlags}.
     *
     * @return New {@link AccountRootFlags}.
     */
    public static AccountRootFlags of(long value) {
      return new AccountRootFlags(value);
    }

    /**
     * Enable rippling on this addresses's trust lines by default. Required for issuing addresses; discouraged for
     * others.
     *
     * @return {@code true} if {@code lsfDefaultRipple} is set, otherwise {@code false}.
     */
    public boolean lsfDefaultRipple() {
      return this.isSet(AccountRootFlags.DEFAULT_RIPPLE);
    }

    /**
     * This account can only receive funds from transactions it sends, and from preauthorized accounts. (It has
     * DepositAuth enabled.)
     *
     * @return {@code true} if {@code lsfDepositAuth} is set, otherwise {@code false}.
     */
    public boolean lsfDepositAuth() {
      return this.isSet(AccountRootFlags.DEPOSIT_AUTH);
    }

    /**
     * Disallows use of the master key to sign transactions for this account.
     *
     * @return {@code true} if {@code lsfDisableMaster} is set, otherwise {@code false}.
     */
    public boolean lsfDisableMaster() {
      return this.isSet(AccountRootFlags.DISABLE_MASTER);
    }

    /**
     * Client applications should not send XRP to this account. Not enforced by rippled.
     *
     * @return {@code true} if {@code lsfDisallowXrp} is set, otherwise {@code false}.
     */
    public boolean lsfDisallowXrp() {
      return this.isSet(AccountRootFlags.DISALLOW_XRP);
    }

    /**
     * All assets issued by this address are frozen.
     *
     * @return {@code true} if {@code lsfGlobalFreeze} is set, otherwise {@code false}.
     */
    public boolean lsfGlobalFreeze() {
      return this.isSet(AccountRootFlags.GLOBAL_FREEZE);
    }

    /**
     * This address cannot freeze trust lines connected to it. Once enabled, cannot be disabled.
     *
     * @return {@code true} if {@code lsfNoFreeze} is set, otherwise {@code false}.
     */
    public boolean lsfNoFreeze() {
      return this.isSet(AccountRootFlags.NO_FREEZE);
    }

    /**
     * The account has used its free SetRegularKey transaction.
     *
     * @return {@code true} if {@code lsfPasswordSpent} is set, otherwise {@code false}.
     */
    public boolean lsfPasswordSpent() {
      return this.isSet(AccountRootFlags.PASSWORD_SPENT);
    }

    /**
     * This account must individually approve other users for those users to hold this account's issued currencies.
     *
     * @return {@code true} if {@code lsfRequireAuth} is set, otherwise {@code false}.
     */
    public boolean lsfRequireAuth() {
      return this.isSet(AccountRootFlags.REQUIRE_AUTH);
    }

    /**
     * Requires incoming payments to specify a Destination Tag.
     *
     * @return {@code true} if {@code lsfRequireDestTag} is set, otherwise {@code false}.
     */
    public boolean lsfRequireDestTag() {
      return this.isSet(AccountRootFlags.REQUIRE_DEST_TAG);
    }
  }

  /**
   * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.SignerListObject}s.
   */
  public static class SignerListFlags extends Flags {

    /**
     * Constant for an unset flag.
     */
    public static final SignerListFlags UNSET = new SignerListFlags(0);

    /**
     * Constant {@link SignerListFlags} for the {@code lsfOneOwner} flag.
     */
    public static final SignerListFlags ONE_OWNER_COUNT = new SignerListFlags(0x00010000);

    private SignerListFlags(long value) {
      super(value);
    }

    /**
     * Construct {@link SignerListFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link SignerListFlags}.
     *
     * @return New {@link SignerListFlags}.
     */
    public static SignerListFlags of(long value) {
      return new SignerListFlags(value);
    }

    /**
     * If this flag is enabled, this SignerList counts as one item for purposes of the owner reserve.
     * Otherwise, this list counts as N+2 items, where N is the number of signers it contains. This flag is
     * automatically enabled if you add or update a signer list after the MultiSignReserve amendment is enabled.
     *
     * @return {@code true} if {@code lsfOneOwnerCount} is set, otherwise {@code false}.
     */
    public boolean lsfOneOwnerCount() {
      return this.isSet(SignerListFlags.ONE_OWNER_COUNT);
    }
  }

  /**
   * {@link TransactionFlags} for {@link AccountSet} transactions. Note that using these directly is
   * discouraged, but can be useful when setting multiple flags for an account.
   */
  public static class AccountSetTransactionFlags extends TransactionFlags {
    /**
     * Constant for an unset flag.
     */
    protected static final AccountSetTransactionFlags UNSET = new AccountSetTransactionFlags(0);

    /**
     * Constant for the {@code tfRequireDestTag} flag.
     */
    protected static final AccountSetTransactionFlags REQUIRE_DEST_TAG = new AccountSetTransactionFlags(0x00010000);

    /**
     * Constant for the {@code tfOptionalDestTag} flag.
     */
    protected static final AccountSetTransactionFlags OPTIONAL_DEST_TAG = new AccountSetTransactionFlags(0x00020000);

    /**
     * Constant for the {@code tfRequireAuth} flag.
     */
    protected static final AccountSetTransactionFlags REQUIRE_AUTH = new AccountSetTransactionFlags(0x00040000);

    /**
     * Constant for the {@code tfOptionalAuth} flag.
     */
    protected static final AccountSetTransactionFlags OPTIONAL_AUTH = new AccountSetTransactionFlags(0x00080000);

    /**
     * Constant for the {@code tfDisallowXRP} flag.
     */
    protected static final AccountSetTransactionFlags DISALLOW_XRP = new AccountSetTransactionFlags(0x00100000);

    /**
     * Constant for the {@code tfAllowXRP} flag.
     */
    protected static final AccountSetTransactionFlags ALLOW_XRP = new AccountSetTransactionFlags(0x00200000);

    private AccountSetTransactionFlags(long value) {
      super(value);
    }

    private static AccountSetTransactionFlags of(
      boolean tfFullyCanonicalSig,
      boolean tfRequireDestTag,
      boolean tfOptionalDestTag,
      boolean tfRequireAuth,
      boolean tfOptionalAuth,
      boolean tfDisallowXrp,
      boolean tfAllowXrp
    ) {
      Preconditions.checkArgument(
        !(tfRequireDestTag && tfOptionalDestTag),
        "tfRequireDestTag and tfOptionalDestTag cannot both be set to true."
      );

      Preconditions.checkArgument(
        !(tfRequireAuth && tfOptionalAuth),
        "tfRequireAuth and tfOptionalAuth cannot both be set to true."
      );

      Preconditions.checkArgument(
        !(tfDisallowXrp && tfAllowXrp),
        "tfDisallowXrp and tfAllowXrp cannot both be set to true."
      );
      return new AccountSetTransactionFlags(
        Flags.of(
          tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
          tfRequireDestTag ? REQUIRE_DEST_TAG : UNSET,
          tfOptionalDestTag ? OPTIONAL_DEST_TAG : UNSET,
          tfRequireAuth ? REQUIRE_AUTH : UNSET,
          tfOptionalAuth ? OPTIONAL_AUTH : UNSET,
          tfDisallowXrp ? DISALLOW_XRP : UNSET,
          tfAllowXrp ? ALLOW_XRP : UNSET
        ).getValue()
      );
    }

    /**
     * Construct {@link AccountSetTransactionFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link AccountSetTransactionFlags}.
     *
     * @return New {@link AccountSetTransactionFlags}.
     */
    public static AccountSetTransactionFlags of(long value) {
      AccountSetTransactionFlags flags = new AccountSetTransactionFlags(value);

      Preconditions.checkArgument(
        !(flags.tfRequireDestTag() && flags.tfOptionalDestTag()),
        "tfRequireDestTag and tfOptionalDestTag cannot both be set to true."
      );

      Preconditions.checkArgument(
        !(flags.tfRequireAuth() && flags.tfOptionalAuth()),
        "tfRequireAuth and tfOptionalAuth cannot both be set to true."
      );

      Preconditions.checkArgument(
        !(flags.tfDisallowXrp() && flags.tfAllowXrp()),
        "tfDisallowXrp and tfAllowXrp cannot both be set to true."
      );

      return flags;
    }

    /**
     * Create a new {@link AccountSetTransactionFlags.Builder}.
     *
     * @return A new {@link AccountSetTransactionFlags.Builder}.
     */
    public static AccountSetTransactionFlags.Builder builder() {
      return new AccountSetTransactionFlags.Builder();
    }

    /**
     * Require a fully canonical signature.
     *
     * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
     */
    public boolean tfFullyCanonicalSig() {
      return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
    }

    /**
     * Whether or not the {@code tfRequireDestTag} flag is set.
     *
     * @return {@code true} if {@code tfRequireDestTag} is set, otherwise {@code false}.
     */
    public boolean tfRequireDestTag() {
      return this.isSet(REQUIRE_DEST_TAG);
    }

    /**
     * Whether or not the {@code tfOptionalDestTag} flag is set.
     *
     * @return {@code true} if {@code tfOptionalDestTag} is set, otherwise {@code false}.
     */
    public boolean tfOptionalDestTag() {
      return this.isSet(OPTIONAL_DEST_TAG);
    }

    /**
     * Whether or not the {@code tfRequireAuth} flag is set.
     *
     * @return {@code true} if {@code tfRequireAuth} is set, otherwise {@code false}.
     */
    public boolean tfRequireAuth() {
      return this.isSet(REQUIRE_AUTH);
    }

    /**
     * Whether or not the {@code tfOptionalAuth} flag is set.
     *
     * @return {@code true} if {@code tfOptionalAuth} is set, otherwise {@code false}.
     */
    public boolean tfOptionalAuth() {
      return this.isSet(OPTIONAL_AUTH);
    }

    /**
     * Whether or not the {@code tfDisallowXrp} flag is set.
     *
     * @return {@code true} if {@code tfDisallowXrp} is set, otherwise {@code false}.
     */
    public boolean tfDisallowXrp() {
      return this.isSet(DISALLOW_XRP);
    }

    /**
     * Whether or not the {@code tfAllowXrp} flag is set.
     *
     * @return {@code true} if {@code tfAllowXrp} is set, otherwise {@code false}.
     */
    public boolean tfAllowXrp() {
      return this.isSet(ALLOW_XRP);
    }

    /**
     * A builder class for {@link AccountSetTransactionFlags}.
     */
    public static class Builder {
      private boolean tfFullyCanonicalSig = true;
      private boolean tfRequireDestTag = false;
      private boolean tfOptionalDestTag = false;
      private boolean tfRequireAuth = false;
      private boolean tfOptionalAuth = false;
      private boolean tfDisallowXrp = false;
      private boolean tfAllowXrp = false;

      /**
       * Set {@code tfFullyCanonicalSig} to the given value.
       *
       * @param tfFullyCanonicalSig A boolean value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Set {@code tfRequireDestTag} to the given value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfRequireDestTag() {
        this.tfRequireDestTag = true;
        return this;
      }

      /**
       * Set {@code tfOptionalDestTag} to the given value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfOptionalDestTag() {
        this.tfOptionalDestTag = true;
        return this;
      }

      /**
       * Set {@code tfRequireAuth} to the given value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfRequireAuth() {
        this.tfRequireAuth = true;
        return this;
      }

      /**
       * Set {@code tfOptionalAuth} to the given value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfOptionalAuth() {
        this.tfOptionalAuth = true;
        return this;
      }

      /**
       * Set {@code tfDisallowXrp} to the given value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfDisallowXrp() {
        this.tfDisallowXrp = true;
        return this;
      }

      /**
       * Set {@code tfAllowXrp} to the given value.
       *
       * @return The same {@link AccountSetTransactionFlags.Builder}.
       */
      public AccountSetTransactionFlags.Builder tfAllowXrp() {
        this.tfAllowXrp = true;
        return this;
      }

      /**
       * Build a new {@link AccountSetTransactionFlags} from the current boolean values.
       *
       * @return A new {@link AccountSetTransactionFlags}.
       */
      public AccountSetTransactionFlags build() {
        return AccountSetTransactionFlags.of(
          tfFullyCanonicalSig,
          tfRequireDestTag,
          tfOptionalDestTag,
          tfRequireAuth,
          tfOptionalAuth,
          tfDisallowXrp,
          tfAllowXrp
        );
      }
    }
  }

  /**
   * A set of static {@link TransactionFlags} which can be set on {@link org.xrpl.xrpl4j.model.transactions.TrustSet}
   * transactions.
   */
  public static class TrustSetFlags extends TransactionFlags {

    /**
     * Constant for an unset flag.
     */
    protected static final TrustSetFlags UNSET = new TrustSetFlags(0);

    /**
     * Constant {@link TrustSetFlags} for the {@code tfSetfAuth} flag.
     */
    protected static final TrustSetFlags SET_F_AUTH = new TrustSetFlags(0x00010000);

    /**
     * Constant {@link TrustSetFlags} for the {@code tfSetNoRipple} flag.
     */
    protected static final TrustSetFlags SET_NO_RIPPLE = new TrustSetFlags(0x00020000);

    /**
     * Constant {@link TrustSetFlags} for the {@code tfClearNoRipple} flag.
     */
    protected static final TrustSetFlags CLEAR_NO_RIPPLE = new TrustSetFlags(0x00040000);

    /**
     * Constant {@link TrustSetFlags} for the {@code tfSetFreeze} flag.
     */
    protected static final TrustSetFlags SET_FREEZE = new TrustSetFlags(0x00100000);

    /**
     * Constant {@link TrustSetFlags} for the {@code tfClearFreeze} flag.
     */
    protected static final TrustSetFlags CLEAR_FREEZE = new TrustSetFlags(0x00200000);

    private TrustSetFlags(long value) {
      super(value);
    }

    /**
     * Create a new {@link TrustSetFlags.Builder}.
     *
     * @return A new {@link TrustSetFlags.Builder}.
     */
    public static TrustSetFlags.Builder builder() {
      return new TrustSetFlags.Builder();
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

    /**
     * Construct {@link TrustSetFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link TrustSetFlags}.
     *
     * @return New {@link TrustSetFlags}.
     */
    public static TrustSetFlags of(long value) {
      return new TrustSetFlags(value);
    }

    /**
     * Require a fully canonical signature.
     *
     * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
     */
    public boolean tfFullyCanonicalSig() {
      return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
    }

    /**
     * Authorize the other party to hold currency issued by this account. (No effect unless using
     * {@link org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag#REQUIRE_AUTH}). Cannot be unset.
     *
     * @return {@code true} if {@code tfSetfAuth} is set, otherwise {@code false}.
     */
    public boolean tfSetfAuth() {
      return this.isSet(SET_F_AUTH);
    }

    /**
     * Enable the No Ripple flag, which blocks rippling between two trust lines of the same currency if this
     * flag is enabled on both.
     *
     * @return {@code true} if {@code tfSetNoRipple} is set, otherwise {@code false}.
     */
    public boolean tfSetNoRipple() {
      return this.isSet(SET_NO_RIPPLE);
    }

    /**
     * Disable the No Ripple flag, allowing rippling on this trust line.
     *
     * @return {@code true} if {@code tfClearNoRipple} is set, otherwise {@code false}.
     */
    public boolean tfClearNoRipple() {
      return this.isSet(CLEAR_NO_RIPPLE);
    }

    /**
     * <a href="https://xrpl.org/freezes.html">Freeze</a> the trust line.
     *
     * @return {@code true} if {@code tfSetFreeze} is set, otherwise {@code false}.
     */
    public boolean tfSetFreeze() {
      return this.isSet(SET_FREEZE);
    }

    /**
     * <a href="https://xrpl.org/freezes.html">Unfreeze</a> the trust line.
     *
     * @return {@code true} if {@code tfClearFreeze} is set, otherwise {@code false}.
     */
    public boolean tfClearFreeze() {
      return this.isSet(CLEAR_FREEZE);
    }

    /**
     * A builder class for {@link TrustSetFlags}.
     */
    public static class Builder {
      private boolean tfFullyCanonicalSig = true;
      private boolean tfSetfAuth = false;
      private boolean tfSetNoRipple = false;
      private boolean tfClearNoRipple = false;
      private boolean tfSetFreeze = false;
      private boolean tfClearFreeze = false;

      /**
       * Set {@code tfFullyCanonicalSig} to the given value.
       *
       * @param tfFullyCanonicalSig A boolean value.
       *
       * @return The same {@link TrustSetFlags.Builder}.
       */
      public TrustSetFlags.Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Set {@code tfSetfAuth} to the given value.
       *
       * @param tfSetfAuth A boolean value.
       *
       * @return The same {@link TrustSetFlags.Builder}.
       */
      public TrustSetFlags.Builder tfSetfAuth(boolean tfSetfAuth) {
        this.tfSetfAuth = tfSetfAuth;
        return this;
      }

      /**
       * Set {@code tfSetNoRipple} to {@code true}.
       *
       * @return The same {@link TrustSetFlags.Builder}.
       */
      public TrustSetFlags.Builder tfSetNoRipple() {
        this.tfSetNoRipple = true;
        return this;
      }

      /**
       * Set {@code tfClearNoRipple} to {@code true}.
       *
       * @return The same {@link TrustSetFlags.Builder}.
       */
      public TrustSetFlags.Builder tfClearNoRipple() {
        this.tfClearNoRipple = true;
        return this;
      }

      /**
       * Set {@code tfSetFreeze} to {@code true}.
       *
       * @return The same {@link TrustSetFlags.Builder}.
       */
      public TrustSetFlags.Builder tfSetFreeze() {
        this.tfSetFreeze = true;
        return this;
      }

      /**
       * Set {@code tfClearFreeze} to {@code true}.
       *
       * @return The same {@link TrustSetFlags.Builder}.
       */
      public TrustSetFlags.Builder tfClearFreeze() {
        this.tfClearFreeze = true;
        return this;
      }

      /**
       * Build a new {@link TrustSetFlags} from the current boolean values.
       *
       * @return A new {@link TrustSetFlags}.
       */
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

  /**
   * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}s.
   */
  public static class RippleStateFlags extends Flags {

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfLowReserve} flag.
     */
    public static final RippleStateFlags LOW_RESERVE = new RippleStateFlags(0x00010000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfHighReserve} flag.
     */
    public static final RippleStateFlags HIGH_RESERVE = new RippleStateFlags(0x00020000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfLowAuth} flag.
     */
    public static final RippleStateFlags LOW_AUTH = new RippleStateFlags(0x00040000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfHighAuth} flag.
     */
    public static final RippleStateFlags HIGH_AUTH = new RippleStateFlags(0x00080000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfLowNoRipple} flag.
     */
    public static final RippleStateFlags LOW_NO_RIPPLE = new RippleStateFlags(0x00100000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfHighNoRipple} flag.
     */
    public static final RippleStateFlags HIGH_NO_RIPPLE = new RippleStateFlags(0x00200000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfLowFreeze} flag.
     */
    public static final RippleStateFlags LOW_FREEZE = new RippleStateFlags(0x00400000);

    /**
     * Constant {@link RippleStateFlags} for the {@code lsfHighFreeze} flag.
     */
    public static final RippleStateFlags HIGH_FREEZE = new RippleStateFlags(0x00800000);

    private RippleStateFlags(long value) {
      super(value);
    }

    /**
     * Construct {@link RippleStateFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link RippleStateFlags}.
     *
     * @return New {@link RippleStateFlags}.
     */
    public static RippleStateFlags of(long value) {
      return new RippleStateFlags(value);
    }

    /**
     * The corresponding {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}
     * <a href="https://xrpl.org/ripplestate.html#contributing-to-the-owner-reserve">contributes to the low
     * account's owner reserve</a>.
     *
     * @return {@code true} if {@code lsfLowReserve} is set, otherwise {@code false}.
     */
    public boolean lsfLowReserve() {
      return this.isSet(LOW_RESERVE);
    }

    /**
     * The corresponding {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}
     * <a href="https://xrpl.org/ripplestate.html#contributing-to-the-owner-reserve">contributes to the high
     * account's owner reserve</a>.
     *
     * @return {@code true} if {@code lsfHighReserve} is set, otherwise {@code false}.
     */
    public boolean lsfHighReserve() {
      return this.isSet(HIGH_RESERVE);
    }

    /**
     * The low account has authorized the high account to hold the low account's issued currency.
     *
     * @return {@code true} if {@code lsfLowAuth} is set, otherwise {@code false}.
     */
    public boolean lsfLowAuth() {
      return this.isSet(LOW_AUTH);
    }

    /**
     * The high account has authorized the low account to hold the high account's issued currency.
     *
     * @return {@code true} if {@code lsfHighAuth} is set, otherwise {@code false}.
     */
    public boolean lsfHighAuth() {
      return this.isSet(HIGH_AUTH);
    }

    /**
     * The low account has <a href="https://xrpl.org/rippling.html">disabled rippling</a> from this trust line.
     *
     * @return {@code true} if {@code lsfLowNoRipple} is set, otherwise {@code false}.
     */
    public boolean lsfLowNoRipple() {
      return this.isSet(LOW_NO_RIPPLE);
    }

    /**
     * The high account has <a href="https://xrpl.org/rippling.html">disabled rippling</a> from this trust line.
     *
     * @return {@code true} if {@code lsfHighNoRipple} is set, otherwise {@code false}.
     */
    public boolean lsfHighNoRipple() {
      return this.isSet(HIGH_NO_RIPPLE);
    }

    /**
     * The low account has frozen the trust line, preventing the high account from transferring the asset.
     *
     * @return {@code true} if {@code lsfLowFreeze} is set, otherwise {@code false}.
     */
    public boolean lsfLowFreeze() {
      return this.isSet(LOW_FREEZE);
    }

    /**
     * The high account has frozen the trust line, preventing the low account from transferring the asset.
     *
     * @return {@code true} if {@code lsfHighFreeze} is set, otherwise {@code false}.
     */
    public boolean lsfHighFreeze() {
      return this.isSet(HIGH_FREEZE);
    }
  }

  /**
   * A set of static {@link TransactionFlags} which can be set on {@link OfferCreate} transactions.
   */
  public static class OfferCreateFlags extends TransactionFlags {

    /**
     * Constant {@link OfferCreateFlags} for the {@code tfPassive} flag.
     */
    protected static final OfferCreateFlags PASSIVE = new OfferCreateFlags(0x00010000L);

    /**
     * Constant {@link OfferCreateFlags} for the {@code tfImmediateOrCancel} flag.
     */
    protected static final OfferCreateFlags IMMEDIATE_OR_CANCEL = new OfferCreateFlags(0x00020000L);

    /**
     * Constant {@link OfferCreateFlags} for the {@code tfFillOrKill} flag.
     */
    protected static final OfferCreateFlags FILL_OR_KILL = new OfferCreateFlags(0x00040000L);

    /**
     * Constant {@link OfferCreateFlags} for the {@code tfSell} flag.
     */
    protected static final OfferCreateFlags SELL = new OfferCreateFlags(0x00080000L);

    private OfferCreateFlags(long value) {
      super(value);
    }

    /**
     * Create a new {@link OfferCreateFlags.Builder}.
     *
     * @return A new {@link OfferCreateFlags.Builder}.
     */
    public static OfferCreateFlags.Builder builder() {
      return new OfferCreateFlags.Builder();
    }

    /**
     * Construct {@link OfferCreateFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link OfferCreateFlags}.
     *
     * @return New {@link OfferCreateFlags}.
     */
    public static OfferCreateFlags of(long value) {
      return new OfferCreateFlags(value);
    }

    private static OfferCreateFlags of(
      boolean tfFullyCanonicalSig,
      boolean tfPassive,
      boolean tfImmediateOrCancel,
      boolean tfFillOrKill,
      boolean tfSell
    ) {
      long value = Flags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfPassive ? PASSIVE : UNSET,
        tfImmediateOrCancel ? IMMEDIATE_OR_CANCEL : UNSET,
        tfFillOrKill ? FILL_OR_KILL : UNSET,
        tfSell ? SELL : UNSET
      ).getValue();
      return new OfferCreateFlags(value);
    }

    /**
     * If enabled, the offer does not consume offers that exactly match it, and instead becomes an
     * Offer object in the ledger. It still consumes offers that cross it.
     *
     * @return {@code true} if {@code tfPassive} is set, otherwise {@code false}.
     */
    public boolean tfPassive() {
      return this.isSet(OfferCreateFlags.PASSIVE);
    }

    /**
     * Treat the offer as an Immediate or Cancel order . If enabled, the offer never becomes a ledger object:
     * it only tries to match existing offers in the ledger. If the offer cannot match any offers immediately,
     * it executes "successfully" without trading any currency. In this case, the transaction has the result code
     * tesSUCCESS, but creates no Offer objects in the ledger.
     *
     * @return {@code true} if {@code tfImmediateOrCancel} is set, otherwise {@code false}.
     */
    public boolean tfImmediateOrCancel() {
      return this.isSet(OfferCreateFlags.IMMEDIATE_OR_CANCEL);
    }

    /**
     * Treat the offer as a Fill or Kill order . Only try to match existing offers in the ledger, and only do so if
     * the entire TakerPays quantity can be obtained. If the fix1578 amendment is enabled and the offer cannot be
     * executed when placed, the transaction has the result code tecKILLED; otherwise, the transaction uses the result
     * code tesSUCCESS even when it was killed without trading any currency.
     *
     * @return {@code true} if {@code tfFillOrKill} is set, otherwise {@code false}.
     */
    public boolean tfFillOrKill() {
      return this.isSet(OfferCreateFlags.FILL_OR_KILL);
    }

    /**
     * Exchange the entire TakerGets amount, even if it means obtaining more than the TakerPays amount in exchange.
     *
     * @return {@code true} if {@code tfSell} is set, otherwise {@code false}.
     */
    public boolean tfSell() {
      return this.isSet(OfferCreateFlags.SELL);
    }


    /**
     * A builder class for {@link OfferCreateFlags} flags.
     */
    public static class Builder {

      private boolean tfFullyCanonicalSig = true;
      private boolean tfPassive = false;
      private boolean tfImmediateOrCancel = false;
      private boolean tfFillOrKill = false;
      private boolean tfSell = false;

      /**
       * Set {@code tfFullyCanonicalSig} to the given value.
       *
       * @param tfFullyCanonicalSig A boolean value.
       *
       * @return The same {@link OfferCreateFlags.Builder}.
       */
      public OfferCreateFlags.Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Set {@code tfPassive} to the given value.
       *
       * @param tfPassive A boolean value.
       *
       * @return The same {@link OfferCreateFlags.Builder}.
       */
      public OfferCreateFlags.Builder tfPassive(boolean tfPassive) {
        this.tfPassive = tfPassive;
        return this;
      }

      /**
       * Set {@code tfImmediateOrCancel} to the given value.
       *
       * @param tfImmediateOrCancel A boolean value.
       *
       * @return The same {@link OfferCreateFlags.Builder}.
       */
      public OfferCreateFlags.Builder tfImmediateOrCancel(boolean tfImmediateOrCancel) {
        this.tfImmediateOrCancel = tfImmediateOrCancel;
        return this;
      }

      /**
       * Set {@code tfFillOrKill} to the given value.
       *
       * @param tfFillOrKill A boolean value.
       *
       * @return The same {@link OfferCreateFlags.Builder}.
       */
      public OfferCreateFlags.Builder tfFillOrKill(boolean tfFillOrKill) {
        this.tfFillOrKill = tfFillOrKill;
        return this;
      }

      /**
       * Set {@code tfSell} to the given value.
       *
       * @param tfSell A boolean value.
       *
       * @return The same {@link OfferCreateFlags.Builder}.
       */
      public OfferCreateFlags.Builder tfSell(boolean tfSell) {
        this.tfSell = tfSell;
        return this;
      }

      /**
       * Build a new {@link OfferCreateFlags} from the current boolean values.
       *
       * @return A new {@link OfferCreateFlags}.
       */
      public OfferCreateFlags build() {
        return OfferCreateFlags.of(
          tfFullyCanonicalSig,
          tfPassive,
          tfImmediateOrCancel,
          tfFillOrKill,
          tfSell
        );
      }
    }
  }

  /**
   * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.OfferObject}s.
   */
  public static class OfferFlags extends Flags {

    /**
     * Constant {@link OfferFlags} for the {@code lsfPassive} flag.
     */
    protected static final OfferFlags PASSIVE = new OfferFlags(0x00010000);

    /**
     * Constant {@link OfferFlags} for the {@code lsfSell} flag.
     */
    protected static final OfferFlags SELL = new OfferFlags(0x00020000);

    private OfferFlags(long value) {
      super(value);
    }

    /**
     * Construct {@link OfferFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link OfferFlags}.
     *
     * @return New {@link OfferFlags}.
     */
    public static OfferFlags of(long value) {
      return new OfferFlags(value);
    }

    /**
     * The object was placed as a passive offer. This has no effect on the object in the ledger.
     *
     * @return {@code true} if {@code lsfPassive} is set, otherwise {@code false}.
     */
    public boolean lsfPassive() {
      return this.isSet(PASSIVE);
    }

    /**
     * The object was placed as a sell offer. This has no effect on the object in the ledger (because tfSell only
     * matters if you get a better rate than you asked for, which cannot happen after the object enters the ledger).
     *
     * @return {@code true} if {@code lsfSell} is set, otherwise {@code false}.
     */
    public boolean lsfSell() {
      return this.isSet(SELL);
    }
  }

  /**
   * A set of static {@link TransactionFlags} which can be set on
   * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim} transactions.
   */
  public static class PaymentChannelClaimFlags extends TransactionFlags {

    /**
     * Constant {@link PaymentChannelClaimFlags} for the {@code tfRenew} flag.
     */
    protected static final PaymentChannelClaimFlags RENEW = new PaymentChannelClaimFlags(0x00010000);

    /**
     * Constant {@link PaymentChannelClaimFlags} for the {@code tfClose} flag.
     */
    protected static final PaymentChannelClaimFlags CLOSE = new PaymentChannelClaimFlags(0x00020000);

    private PaymentChannelClaimFlags(long value) {
      super(value);
    }

    /**
     * Create a new {@link PaymentChannelClaimFlags.Builder}.
     *
     * @return A new {@link PaymentChannelClaimFlags.Builder}.
     */
    public static PaymentChannelClaimFlags.Builder builder() {
      return new Builder();
    }

    private static PaymentChannelClaimFlags of(boolean tfFullyCanonicalSig, boolean tfRenew, boolean tfClose) {
      return new PaymentChannelClaimFlags(
        TransactionFlags.of(
          tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
          tfRenew ? RENEW : UNSET,
          tfClose ? CLOSE : UNSET
        ).getValue()
      );
    }

    /**
     * Construct {@link PaymentChannelClaimFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link PaymentChannelClaimFlags}.
     *
     * @return New {@link PaymentChannelClaimFlags}.
     */
    public static PaymentChannelClaimFlags of(long value) {
      return new PaymentChannelClaimFlags(value);
    }

    /**
     * Require a fully canonical transaction signature.
     *
     * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
     */
    public boolean tfFullyCanonicalSig() {
      return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
    }

    /**
     * Clear the {@link PayChannelObject#expiration()} time (different from {@link PayChannelObject#cancelAfter()}
     * time.) Only the source address of the payment channel can use this flag.
     *
     * @return {@code true} if {@code tfRenew} is set, otherwise {@code false}.
     */
    public boolean tfRenew() {
      return this.isSet(RENEW);
    }

    /**
     * Request to close the channel.
     *
     * <p>Only the {@link PayChannelObject#account()} and {@link PayChannelObject#destination()} addresses can use
     * this flag.</p>
     *
     * <p>This flag closes the channel immediately if it has no more XRP allocated to it after processing the
     * current claim, or if the {@link PayChannelObject#destination()} address uses it. If the source address uses
     * this flag when the channel still holds XRP, this schedules the channel to close after
     * {@link PayChannelObject#settleDelay()} seconds have passed. (Specifically, this sets the
     * {@link PayChannelObject#expiration()} of the channel to the close time of the previous ledger plus the channel's
     * {@link PayChannelObject#settleDelay()} time, unless the channel already has an earlier
     * {@link PayChannelObject#expiration()} time.)</p>
     *
     * <p>If the {@link PayChannelObject#destination()} address uses this flag when the channel still holds XRP,
     * any XRP that remains after processing the claim is returned to the source address.</p>
     *
     * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
     */
    public boolean tfClose() {
      return this.isSet(CLOSE);
    }

    /**
     * A builder class for {@link PaymentChannelClaimFlags}.
     */
    public static class Builder {
      boolean tfFullyCanonicalSig = true;
      boolean tfRenew = false;
      boolean tfClose = false;

      /**
       * Set {@code tfFullyCanonicalSig} to the given value.
       *
       * @param tfFullyCanonicalSig A boolean value.
       *
       * @return The same {@link PaymentChannelClaimFlags.Builder}.
       */
      public PaymentChannelClaimFlags.Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Set {@code tfRenew} to the given value.
       *
       * @param tfRenew A boolean value.
       *
       * @return The same {@link PaymentChannelClaimFlags.Builder}.
       */
      public PaymentChannelClaimFlags.Builder tfRenew(boolean tfRenew) {
        this.tfRenew = tfRenew;
        return this;
      }

      /**
       * Set {@code tfClose} to the given value.
       *
       * @param tfClose A boolean value.
       *
       * @return The same {@link PaymentChannelClaimFlags.Builder}.
       */
      public PaymentChannelClaimFlags.Builder tfClose(boolean tfClose) {
        this.tfClose = tfClose;
        return this;
      }

      /**
       * Build a new {@link PaymentChannelClaimFlags} from the current boolean values.
       *
       * @return A new {@link PaymentChannelClaimFlags}.
       */
      public PaymentChannelClaimFlags build() {
        return PaymentChannelClaimFlags.of(tfFullyCanonicalSig, tfRenew, tfClose);
      }
    }
  }
}
