package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Tracks a preauthorization from one account to another. {@link DepositPreAuth} transactions create these objects.
 *
 *
 * <p>This has no effect on processing of {@link Transaction}s unless the account that provided the preauthorization
 * requires Deposit Authorization. In that case, the account that was preauthorized can send payments and
 * other transactions directly to the account that provided the preauthorization.
 * Preauthorizations are uni-directional, and have no effect on payments going the opposite direction.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDepositPreAuthObject.class)
@JsonDeserialize(as = ImmutableDepositPreAuthObject.class)
public interface DepositPreAuthObject extends LedgerObject {

  /**
   * The type of ledger object, which will always be "DepositPreauth" in this case.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.DEPOSIT_PRE_AUTH;
  }

  /**
   * The account that granted the preauthorization. (The destination of the preauthorized payments.)
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The account that received the preauthorization. (The sender of the preauthorized payments.)
   */
  @JsonProperty("Authorize")
  Address authorize();

  /**
   * A bit-map of boolean flags. No flags are defined for {@link DepositPreAuthObject}s, so this value is always 0.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory
   * consists of multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

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
   * The unique ID of the {@link DepositPreAuthObject}.
   */
  String index();
}
