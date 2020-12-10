package org.xrpl.xrpl4j.model.flags;

import com.fasterxml.jackson.annotation.JsonValue;
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
   * A set of static Universal {@link Flags} which could apply to any transaction.
   */
  public static class TransactionFlags extends Flags {

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
    public static final PaymentFlags PARTIAL_PAYMENT = new PaymentFlags(0x00020000L);
    public static final PaymentFlags LIMIT_QUALITY = new PaymentFlags(0x00040000L);

    private PaymentFlags(long value) {
      super(value);
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
          tfPartialPayment ? PARTIAL_PAYMENT : UNSET,
          tfLimitQuality ? LIMIT_QUALITY : UNSET
      ).getValue());
    }

    /**
     * Flag indicated to only use paths included in the {@link Payment#paths()}
     * field. This is intended to force the transaction to take arbitrage opportunities. Most clients do not need this.
     */
    public boolean tfNoDirectRipple() {
      return this.isSet(PaymentFlags.NO_DIRECT_RIPPLE);
    }

    /**
     * If the specified {@link Payment#amount()} cannot be sent without spending
     * more than {@link Payment#sendMax()}, reduce the received amount instead of
     * failing outright.
     *
     * @see "https://xrpl.org/partial-payments.html"
     */
    public boolean tfPartialPayment() {
      return this.isSet(PaymentFlags.PARTIAL_PAYMENT);
    }

    /**
     * Only take paths where all the conversions have an input:output ratio that is equal or better than the ratio of
     * {@link Payment#amount()}:{@link Payment#sendMax()}.
     *
     * @return
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

      public Builder tfFullyCanonicalSig(boolean value) {
        this.tfFullyCanonicalSig = value;
        return this;
      }

      public Builder tfNoDirectRipple(boolean value) {
        this.tfNoDirectRipple = value;
        return this;
      }

      public Builder tfPartialPayment(boolean value) {
        this.tfPartialPayment = value;
        return this;
      }

      public Builder tfLimitQuality(boolean value) {
        this.tfLimitQuality = value;
        return this;
      }

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

    public static Builder builder() {
      return new Builder();
    }

    public static AccountRootFlags of(long value) {
      return new AccountRootFlags(value);
    }

    public static AccountRootFlags of(
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
     * Flag indicated to only use paths included in the {@link Payment#paths()}
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
    public boolean lsfDisallowXrp() {
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

    /**
     * A builder class for {@link PaymentFlags} flags.
     */
    public static class Builder {

      private boolean lsfDefaultRipple = false;
      private boolean lsfDepositAuth = false;
      private boolean lsfDisableMaster = false;
      private boolean lsfDisallowXrp = false;
      private boolean lsfGlobalFreeze = false;
      private boolean lsfNoFreeze = false;
      private boolean lsfPasswordSpent = false;
      private boolean lsfRequireAuth = false;
      private boolean lsfRequireDestTag = false;

      public AccountRootFlags.Builder lsfDefaultRipple(boolean value) {
        this.lsfDefaultRipple = value;
        return this;
      }

      public AccountRootFlags.Builder lsfDepositAuth(boolean value) {
        this.lsfDepositAuth = value;
        return this;
      }

      public AccountRootFlags.Builder lsfDisableMaster(boolean value) {
        this.lsfDisableMaster = value;
        return this;
      }

      public AccountRootFlags.Builder lsfDisallowXrp(boolean value) {
        this.lsfDisallowXrp = value;
        return this;
      }

      public AccountRootFlags.Builder lsfGlobalFreeze(boolean value) {
        this.lsfGlobalFreeze = value;
        return this;
      }

      public AccountRootFlags.Builder lsfNoFreeze(boolean value) {
        this.lsfNoFreeze = value;
        return this;
      }

      public AccountRootFlags.Builder lsfPasswordSpent(boolean value) {
        this.lsfPasswordSpent = value;
        return this;
      }

      public AccountRootFlags.Builder lsfRequireAuth(boolean value) {
        this.lsfRequireAuth = value;
        return this;
      }

      public AccountRootFlags.Builder lsfRequireDestTag(boolean value) {
        this.lsfRequireDestTag = value;
        return this;
      }

      public AccountRootFlags build() {
        return AccountRootFlags.of(
            lsfDefaultRipple, lsfDepositAuth, lsfDisableMaster, lsfDisallowXrp, lsfGlobalFreeze, lsfNoFreeze,
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

    public static SignerListFlags.Builder builder() {
      return new SignerListFlags.Builder();
    }

    private static SignerListFlags of(boolean lsfOneOwnerCount) {
      return new SignerListFlags(Flags.of(lsfOneOwnerCount ? SignerListFlags.ONE_OWNER_COUNT : UNSET).getValue());
    }

    public static SignerListFlags of(long value) {
      return new SignerListFlags(value);
    }

    public boolean lsfOneOwnerCount() {
      return this.isSet(SignerListFlags.ONE_OWNER_COUNT);
    }

    public static class Builder {

      private boolean lsfOneOwnerCount = false;

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

    private TrustSetFlags(long value) {
      super(value);
    }

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

    public static TrustSetFlags of(long value) {
      return new TrustSetFlags(value);
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

  public static class RippleStateFlags extends Flags {

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

    public static RippleStateFlags of(long value) {
      return new RippleStateFlags(value);
    }

    public static RippleStateFlags.Builder builder() {
      return new RippleStateFlags.Builder();
    }

    public boolean lsfLowReserve() {
      return this.isSet(LOW_RESERVE);
    }

    public boolean lsfHighReserve() {
      return this.isSet(HIGH_RESERVE);
    }

    public boolean lsfLowAuth() {
      return this.isSet(LOW_AUTH);
    }

    public boolean lsfHighAuth() {
      return this.isSet(HIGH_AUTH);
    }

    public boolean lsfLowNoRipple() {
      return this.isSet(LOW_NO_RIPPLE);
    }

    public boolean lsfHighNoRipple() {
      return this.isSet(HIGH_NO_RIPPLE);
    }

    public boolean lsfLowFreeze() {
      return this.isSet(LOW_FREEZE);
    }

    public boolean lsfHighFreeze() {
      return this.isSet(HIGH_FREEZE);
    }

    public static class Builder {
      boolean lsfLowReserve = false;
      boolean lsfHighReserve = false;
      boolean lsfLowAuth = false;
      boolean lsfHighAuth = false;
      boolean lsfLowNoRipple = false;
      boolean lsfHighNoRipple = false;
      boolean lsfLowFreeze = false;
      boolean lsfHighFreeze = false;

      public Builder lsfLowReserve(boolean lsfLowReserve) {
        this.lsfLowReserve = lsfLowReserve;
        return this;
      }

      public Builder lsfHighReserve(boolean lsfHighReserve) {
        this.lsfHighReserve = lsfHighReserve;
        return this;
      }

      public Builder lsfLowAuth(boolean lsfLowAuth) {
        this.lsfLowAuth = lsfLowAuth;
        return this;
      }

      public Builder lsfHighAuth(boolean lsfHighAuth) {
        this.lsfHighAuth = lsfHighAuth;
        return this;
      }

      public Builder lsfLowNoRipple(boolean lsfLowNoRipple) {
        this.lsfLowNoRipple = lsfLowNoRipple;
        return this;
      }

      public Builder lsfHighNoRipple(boolean lsfHighNoRipple) {
        this.lsfHighNoRipple = lsfHighNoRipple;
        return this;
      }

      public Builder lsfLowFreeze(boolean lsfLowFreeze) {
        this.lsfLowFreeze = lsfLowFreeze;
        return this;
      }

      public Builder lsfHighFreeze(boolean lsfHighFreeze) {
        this.lsfHighFreeze = lsfHighFreeze;
        return this;
      }

      public RippleStateFlags build() {
        return RippleStateFlags.of(
            lsfLowReserve,
            lsfHighReserve,
            lsfLowAuth,
            lsfHighAuth,
            lsfLowNoRipple,
            lsfHighNoRipple,
            lsfLowFreeze,
            lsfHighFreeze
        );
      }
    }
  }

  /**
   * {@link Flags} for {@link OfferCreate} transactions.
   */
  public static class OfferFlags extends TransactionFlags {

    protected static final OfferFlags PASSIVE = new OfferFlags(0x00010000L);
    protected static final OfferFlags IMMEDIATE_OR_CANCEL = new OfferFlags(0x00020000L);
    protected static final OfferFlags FILL_OR_KILL = new OfferFlags(0x00040000L);
    protected static final OfferFlags SELL = new OfferFlags(0x00080000L);

    private OfferFlags(long value) {
      super(value);
    }

    public static OfferFlags.Builder builder() {
      return new OfferFlags.Builder();
    }

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
     * @return true if enabled
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
     * @return true if enabled
     */
    public boolean tfFillOrKill() {
      return this.isSet(OfferFlags.FILL_OR_KILL);
    }

    /**
     * Exchange the entire TakerGets amount, even if it means obtaining more than the TakerPays amount in exchange.
     *
     * @return true if enabled
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

      public OfferFlags.Builder tfFullyCanonicalSig(boolean value) {
        this.tfFullyCanonicalSig = value;
        return this;
      }

      public OfferFlags.Builder tfPassive(boolean value) {
        this.tfPassive = value;
        return this;
      }

      public OfferFlags.Builder tfImmediateOrCancel(boolean value) {
        this.tfImmediateOrCancel = value;
        return this;
      }

      public OfferFlags.Builder tfFillOrKill(boolean value) {
        this.tfFillOrKill = value;
        return this;
      }

      public OfferFlags.Builder tfSell(boolean value) {
        this.tfSell = value;
        return this;
      }

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

  public static class PaymentChannelClaimFlags extends TransactionFlags {

    protected static final PaymentChannelClaimFlags RENEW = new PaymentChannelClaimFlags(0x00010000);
    protected static final PaymentChannelClaimFlags CLOSE = new PaymentChannelClaimFlags(0x00020000);

    private PaymentChannelClaimFlags(long value) {
      super(value);
    }

    public static Builder builder() {
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

    public static PaymentChannelClaimFlags of(long value) {
      return new PaymentChannelClaimFlags(value);
    }

    public boolean tfFullyCanonicalSig() {
      return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
    }

    public boolean tfRenew() {
      return this.isSet(RENEW);
    }

    public boolean tfClose() {
      return this.isSet(CLOSE);
    }

    public static class Builder {
      boolean tfFullyCanonicalSig = true;
      boolean tfRenew = false;
      boolean tfClose = false;

      public Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
        this.tfFullyCanonicalSig = tfFullyCanonicalSig;
        return this;
      }

      public Builder tfRenew(boolean tfRenew) {
        this.tfRenew = tfRenew;
        return this;
      }

      public Builder tfClose(boolean tfClose) {
        this.tfClose = tfClose;
        return this;
      }

      public PaymentChannelClaimFlags build() {
        return PaymentChannelClaimFlags.of(tfFullyCanonicalSig, tfRenew, tfClose);
      }
    }
  }
}
