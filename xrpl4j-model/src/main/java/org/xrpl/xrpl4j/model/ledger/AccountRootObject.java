package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.Optional;

/**
 * Represents the AccountRoot ledger object, which describes a single account, its settings, and XRP balance.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountRootObject.class)
@JsonDeserialize(as = ImmutableAccountRootObject.class)
public interface AccountRootObject extends LedgerObject {

  static ImmutableAccountRootObject.Builder builder() {
    return ImmutableAccountRootObject.builder();
  }

  /**
   * The type of ledger object, which will always be "AccountRoot" in this case.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.ACCOUNT_ROOT;
  }

  /**
   * The unique classic {@link Address} of this account.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The account's current XRP balance in drops, represented as an {@link XrpCurrencyAmount}.
   */
  @JsonProperty("Balance")
  XrpCurrencyAmount balance();

  /**
   * A bit-map of boolean {@link Flags.AccountRootFlags} enabled for this account.
   */
  @JsonProperty("Flags")
  Flags.AccountRootFlags flags();

  /**
   * The number of objects this account owns in the ledger, which contributes to its owner reserve.
   */
  @JsonProperty("OwnerCount")
  UnsignedInteger ownerCount();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The sequence number of the next valid transaction for this account. (Each account starts with
   * Sequence = 1 and increases each time a transaction is made.)
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * The identifying hash of the transaction most recently sent by this account. This field must be enabled to
   * use the {@link org.xrpl.xrpl4j.model.transactions.Transaction#accountTransactionId()} field. To enable it,
   * send an {@link org.xrpl.xrpl4j.model.transactions.AccountSet} transaction with
   * {@link org.xrpl.xrpl4j.model.transactions.AccountSet#setFlag()} equal to
   * {@link org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag#ACCOUNT_TXN_ID}.
   */
  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  /**
   * A domain associated with this account. In JSON, this is the hexadecimal for the ASCII representation of the domain.
   */
  @JsonProperty("Domain")
  Optional<String> domain();

  /**
   * The md5 hash of an email address. Clients can use this to look up an avatar through services such as
   * <a href="https://en.gravatar.com/">Gravatar</a>.
   */
  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  /**
   * A public key that may be used to send encrypted messages to this account. No more than 33 bytes.
   */
  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  /**
   * The address of a key pair that can be used to sign {@link org.xrpl.xrpl4j.model.transactions.Transaction}s for
   * this account instead of the master key.
   */
  @JsonProperty("RegularKey")
  Optional<Address> regularKey();

  /**
   * How many significant digits to use for exchange rates of Offers involving currencies issued by this address.
   * Valid values are 3 to 15, inclusive.
   */
  @JsonProperty("TickSize")
  Optional<UnsignedInteger> tickSize();

  /**
   * A transfer fee to charge other users for sending currency issued by this account to each other.
   */
  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

  /**
   * (Omitted unless the request specified signer_lists and at least one SignerList is associated with the account.)
   * Array of {@link SignerList} ledger objects associated with this account for Multi-Signing. Since an account can own
   * at most one SignerList, this array must have exactly one member if it is present.
   */
  @JsonProperty("signer_lists")
  List<SignerList> signerLists();

  /**
   * The unique ID of this {@link AccountRootObject} ledger object.
   *
   * @see "https://xrpl.org/ledger-object-ids.html"
   */
  Hash256 index();

}
