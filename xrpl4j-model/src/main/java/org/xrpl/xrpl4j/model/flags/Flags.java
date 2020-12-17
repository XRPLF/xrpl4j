package org.xrpl.xrpl4j.model.flags;

import com.fasterxml.jackson.annotation.JsonValue;
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

  public static final Flags UNSET = new Flags(0);

  private final long value;

  private Flags(long value) {
    this.value = value;
  }

  /**
   * Construct {@link Flags} for the given value.
   *
   * @param value A long flags value.
   * @return A new {@link Flags}.
   */
  public static Flags of(long value) {
    return new Flags(value);
  }

  /**
   * Construct {@link Flags} from one or more {@link Flags} by performing a bitwise OR on all.
   *
   * @param flag The first {@link Flags}.
   * @param others Zero or more other {@link Flags} to include.
   *
   * @return A new {@link Flags}.
   */
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
   * Performs a bitwise OR on this {@link Flags} and another {@link Flags}.
   *
   * @param other The {@link Flags} to perform the OR with.
   * @return The {@link Flags} resulting from the OR operation.
   */
  Flags bitwiseOr(Flags other) {
    return Flags.of(this.value | other.value);
  }

  /**
   * Performs a bitwise AND on this {@link Flags} and another {@link Flags}.
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

    public static final PaymentFlags UNSET = new PaymentFlags(0);

    /**
     * Static {@link PaymentFlags} value constants.
     */
    protected static final PaymentFlags NO_DIRECT_RIPPLE = new PaymentFlags(0x00010000L);
    protected static final PaymentFlags PARTIAL_PAYMENT = new PaymentFlags(0x00020000L);
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

    public static final AccountRootFlags UNSET = new AccountRootFlags(0);

    public static final AccountRootFlags DEFAULT_RIPPLE = new AccountRootFlags(0x00800000L);

    protected static final AccountRootFlags DEPOSIT_AUTH = new AccountRootFlags(0x01000000);

    protected static final AccountRootFlags DISABLE_MASTER = new AccountRootFlags(0x00100000);

    protected static final AccountRootFlags DISALLOW_XRP = new AccountRootFlags(0x00080000L);

    protected static final AccountRootFlags GLOBAL_FREEZE = new AccountRootFlags(0x00400000);

    protected static final AccountRootFlags NO_FREEZE = new AccountRootFlags(0x00200000);

    protected static final AccountRootFlags PASSWORD_SPENT = new AccountRootFlags(0x00010000);

    protected static final AccountRootFlags REQUIRE_AUTH = new AccountRootFlags(0x00040000);

    protected static final AccountRootFlags REQUIRE_DEST_TAG = new AccountRootFlags(0x00020000);

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
     * @return New {@link AccountRootFlags}.
     */
    public static AccountRootFlags of(long value) {
      return new AccountRootFlags(value);
    }

    private static AccountRootFlags of(
        boolean lsfDefaultRipple,
        boolean lsfDepositAuth,
        boolean lsfDisableMaster,
        boolean lsfDisallowXrp,
        boolean lsfGlobalFreeze,
        boolean lsfNoFreeze,
        boolean lsfPasswordSpent,
        boolean lsfRequireAuth,
        boolean lsfRequireDestTag
    ) {
      return new AccountRootFlags(
          Flags.of(
              lsfDefaultRipple ? AccountRootFlags.DEFAULT_RIPPLE : UNSET,
              lsfDepositAuth ? AccountRootFlags.DEPOSIT_AUTH : UNSET,
              lsfDisableMaster ? AccountRootFlags.DISABLE_MASTER : UNSET,
              lsfDisallowXrp ? AccountRootFlags.DISALLOW_XRP : UNSET,
              lsfGlobalFreeze ? AccountRootFlags.GLOBAL_FREEZE : UNSET,
              lsfNoFreeze ? AccountRootFlags.NO_FREEZE : UNSET,
              lsfPasswordSpent ? AccountRootFlags.PASSWORD_SPENT : UNSET,
              lsfRequireAuth ? AccountRootFlags.REQUIRE_AUTH : UNSET,
              lsfRequireDestTag ? AccountRootFlags.REQUIRE_DEST_TAG : UNSET
          ).getValue());
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

    public static final SignerListFlags UNSET = new SignerListFlags(0);

    /**
     * Static flag value constants.
     */
    public static final SignerListFlags ONE_OWNER_COUNT = new SignerListFlags(0x00010000);

    private SignerListFlags(long value) {
      super(value);
    }

    private static SignerListFlags of(boolean lsfOneOwnerCount) {
      return new SignerListFlags(Flags.of(lsfOneOwnerCount ? SignerListFlags.ONE_OWNER_COUNT : UNSET).getValue());
    }

    /**
     * Construct {@link SignerListFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link SignerListFlags}.
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
   * A set of static {@link TransactionFlags} which can be set on {@link org.xrpl.xrpl4j.model.transactions.TrustSet}
   * transactions.
   */
  public static class TrustSetFlags extends TransactionFlags {

    /**
     * Flag value constants.
     */
    public static final TrustSetFlags UNSET = new TrustSetFlags(0);
    protected static final TrustSetFlags SET_F_AUTH = new TrustSetFlags(0x00010000);
    protected static final TrustSetFlags SET_NO_RIPPLE = new TrustSetFlags(0x00020000);
    protected static final TrustSetFlags CLEAR_NO_RIPPLE = new TrustSetFlags(0x00040000);
    protected static final TrustSetFlags SET_FREEZE = new TrustSetFlags(0x00100000);
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
     * Static flag value constants.
     */
    public static final RippleStateFlags LOW_RESERVE = new RippleStateFlags(0x00010000);
    public static final RippleStateFlags HIGH_RESERVE = new RippleStateFlags(0x00020000);
    public static final RippleStateFlags LOW_AUTH = new RippleStateFlags(0x00040000);
    public static final RippleStateFlags HIGH_AUTH = new RippleStateFlags(0x00080000);
    public static final RippleStateFlags LOW_NO_RIPPLE = new RippleStateFlags(0x00100000);
    public static final RippleStateFlags HIGH_NO_RIPPLE = new RippleStateFlags(0x00200000);
    public static final RippleStateFlags LOW_FREEZE = new RippleStateFlags(0x00400000);
    public static final RippleStateFlags HIGH_FREEZE = new RippleStateFlags(0x00800000);

    private RippleStateFlags(long value) {
      super(value);
    }

    private static RippleStateFlags of(
        boolean lsfLowReserve,
        boolean lsfHighReserve,
        boolean lsfLowAuth,
        boolean lsfHighAuth,
        boolean lsfLowNoRipple,
        boolean lsfHighNoRipple,
        boolean lsfLowFreeze,
        boolean lsfHighFreeze
    ) {
      return new RippleStateFlags(
          Flags.of(
              lsfLowReserve ? LOW_RESERVE : UNSET,
              lsfHighReserve ? HIGH_RESERVE : UNSET,
              lsfLowAuth ? LOW_AUTH : UNSET,
              lsfHighAuth ? HIGH_AUTH : UNSET,
              lsfLowNoRipple ? LOW_NO_RIPPLE : UNSET,
              lsfHighNoRipple ? HIGH_NO_RIPPLE : UNSET,
              lsfLowFreeze ? LOW_FREEZE : UNSET,
              lsfHighFreeze ? HIGH_FREEZE : UNSET
          ).getValue()
      );
    }

    /**
     * Construct {@link RippleStateFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link RippleStateFlags}.
     * @return New {@link RippleStateFlags}.
     */
    public static RippleStateFlags of(long value) {
      return new RippleStateFlags(value);
    }

    /**
     * The corresponding {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}
     * <a href="https://xrpl.org/ripplestate.html#contributing-to-the-owner-reserve">contributes to the low
     * account's owner reserve.</a>
     *
     * @return {@code true} if {@code lsfLowReserve} is set, otherwise {@code false}.
     */
    public boolean lsfLowReserve() {
      return this.isSet(LOW_RESERVE);
    }

    /**
     * The corresponding {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}
     * <a href="https://xrpl.org/ripplestate.html#contributing-to-the-owner-reserve">contributes to the high
     * account's owner reserve.</a>
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
  public static class OfferFlags extends TransactionFlags {

    /**
     * Static flag value constants.
     */
    protected static final OfferFlags PASSIVE = new OfferFlags(0x00010000L);
    protected static final OfferFlags IMMEDIATE_OR_CANCEL = new OfferFlags(0x00020000L);
    protected static final OfferFlags FILL_OR_KILL = new OfferFlags(0x00040000L);
    protected static final OfferFlags SELL = new OfferFlags(0x00080000L);

    private OfferFlags(long value) {
      super(value);
    }

    /**
     * Create a new {@link OfferFlags.Builder}.
     *
     * @return A new {@link OfferFlags.Builder}.
     */
    public static OfferFlags.Builder builder() {
      return new OfferFlags.Builder();
    }

    /**
     * Construct {@link OfferFlags} with a given value.
     *
     * @param value The long-number encoded flags value of this {@link OfferFlags}.
     * @return New {@link OfferFlags}.
     */
    public static OfferFlags of(long value) {
      return new OfferFlags(value);
    }

    private static OfferFlags of(
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
      return new OfferFlags(value);
    }

    /**
     * If enabled, the offer does not consume offers that exactly match it, and instead becomes an
     * Offer object in the ledger. It still consumes offers that cross it.
     *
     * @return {@code true} if {@code tfPassive} is set, otherwise {@code false}.
     */
    public boolean tfPassive() {
      return this.isSet(OfferFlags.PASSIVE);
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
      return this.isSet(OfferFlags.IMMEDIATE_OR_CANCEL);
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
      return this.isSet(OfferFlags.FILL_OR_KILL);
    }

    /**
     * Exchange the entire TakerGets amount, even if it means obtaining more than the TakerPays amount in exchange.
     *
     * @return {@code true} if {@code tfSell} is set, otherwise {@code false}.
     */
    public boolean tfSell() {
      return this.isSet(OfferFlags.SELL);
    }


    /**
     * A builder class for {@link OfferFlags} flags.
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
       * @return The same {@link OfferFlags.Builder}.
       */
      public OfferFlags.Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      /**
       * Set {@code tfPassive} to the given value.
       *
       * @param tfPassive A boolean value.
       *
       * @return The same {@link OfferFlags.Builder}.
       */
      public OfferFlags.Builder tfPassive(boolean tfPassive) {
        this.tfPassive = tfPassive;
        return this;
      }

      /**
       * Set {@code tfImmediateOrCancel} to the given value.
       *
       * @param tfImmediateOrCancel A boolean value.
       *
       * @return The same {@link OfferFlags.Builder}.
       */
      public OfferFlags.Builder tfImmediateOrCancel(boolean tfImmediateOrCancel) {
        this.tfImmediateOrCancel = tfImmediateOrCancel;
        return this;
      }

      /**
       * Set {@code tfFillOrKill} to the given value.
       *
       * @param tfFillOrKill A boolean value.
       *
       * @return The same {@link OfferFlags.Builder}.
       */
      public OfferFlags.Builder tfFillOrKill(boolean tfFillOrKill) {
        this.tfFillOrKill = tfFillOrKill;
        return this;
      }

      /**
       * Set {@code tfSell} to the given value.
       *
       * @param tfSell A boolean value.
       *
       * @return The same {@link OfferFlags.Builder}.
       */
      public OfferFlags.Builder tfSell(boolean tfSell) {
        this.tfSell = tfSell;
        return this;
      }

      /**
       * Build a new {@link OfferFlags} from the current boolean values.
       *
       * @return A new {@link OfferFlags}.
       */
      public OfferFlags build() {
        return OfferFlags.of(
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
   * A set of static {@link TransactionFlags} which can be set on {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim}
   * transactions.
   */
  public static class PaymentChannelClaimFlags extends TransactionFlags {

    /**
     *
     */
    protected static final PaymentChannelClaimFlags RENEW = new PaymentChannelClaimFlags(0x00010000);
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
