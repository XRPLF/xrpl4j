package org.xrpl.xrpl4j.model.ledger;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
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

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountRootObject.Builder}.
   */
  static ImmutableAccountRootObject.Builder builder() {
    return ImmutableAccountRootObject.builder();
  }

  /**
   * The type of ledger object, which will always be "AccountRoot" in this case.
   *
   * @return Always returns {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#ACCOUNT_ROOT}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.ACCOUNT_ROOT;
  }

  /**
   * The unique classic {@link Address} of this account.
   *
   * @return The {@link Address} of this account.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The account's current XRP balance in drops, represented as an {@link XrpCurrencyAmount}.
   *
   * @return An {@link XrpCurrencyAmount} representing the account's XRP balance.
   */
  @JsonProperty("Balance")
  XrpCurrencyAmount balance();

  /**
   * A bit-map of boolean {@link Flags.AccountRootFlags} enabled for this account.
   *
   * @return An {@link org.xrpl.xrpl4j.model.flags.Flags.AccountRootFlags}.
   */
  @JsonProperty("Flags")
  Flags.AccountRootFlags flags();

  /**
   * The number of objects this account owns in the ledger, which contributes to its owner reserve.
   *
   * @return An {@link UnsignedInteger} representing the number of objects.
   */
  @JsonProperty("OwnerCount")
  UnsignedInteger ownerCount();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The sequence number of the next valid transaction for this account. (Each account starts with
   * Sequence = 1 and increases each time a transaction is made.)
   *
   * @return An {@link UnsignedInteger} representing the account sequence number.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * The identifying hash of the transaction most recently sent by this account. This field must be enabled to
   * use the {@link org.xrpl.xrpl4j.model.transactions.Transaction#accountTransactionId()} field. To enable it,
   * send an {@link org.xrpl.xrpl4j.model.transactions.AccountSet} transaction with
   * {@link org.xrpl.xrpl4j.model.transactions.AccountSet#setFlag()} equal to
   * {@link org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag#ACCOUNT_TXN_ID}.
   *
   * @return An {@link Optional} of type {@link Hash256}.
   */
  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  /**
   * A domain associated with this account. In JSON, this is the hexadecimal for the ASCII representation of the domain.
   *
   * @return An {@link Optional} of type {@link String} containing the domain.
   */
  @JsonProperty("Domain")
  Optional<String> domain();

  /**
   * The md5 hash of an email address. Clients can use this to look up an avatar through services such as
   * <a href="https://en.gravatar.com/">Gravatar</a>.
   *
   * @return An {@link Optional} of type {@link String}.
   */
  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  /**
   * A public key that may be used to send encrypted messages to this account. No more than 33 bytes.
   *
   * @return An {@link Optional} of type {@link String}.
   */
  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  /**
   * The address of a key pair that can be used to sign {@link org.xrpl.xrpl4j.model.transactions.Transaction}s for
   * this account instead of the master key.
   *
   * @return An {@link Optional} of type {@link Address}.
   */
  @JsonProperty("RegularKey")
  Optional<Address> regularKey();

  /**
   * How many significant digits to use for exchange rates of Offers involving currencies issued by this address.
   * Valid values are 3 to 15, inclusive.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger}.
   */
  @JsonProperty("TickSize")
  Optional<UnsignedInteger> tickSize();

  /**
   * A transfer fee to charge other users for sending currency issued by this account to each other.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger}.
   */
  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

  /**
   * (Omitted unless the request specified signer_lists and at least one SignerList is associated with the account.)
   * Array of {@link SignerListObject} ledger objects associated with this account for Multi-Signing. Since an account
   * can own at most one SignerList, this array must have exactly one member if it is present.
   *
   * @return A {@link List} of {@link SignerListObject}s.
   */
  @JsonProperty("signer_lists")
  List<SignerListObject> signerLists();

  /**
   * The unique ID of this {@link AccountRootObject} ledger object.
   *
   * @return A {@link Hash256}.
   * @see "https://xrpl.org/ledger-object-ids.html"
   */
  Hash256 index();

}
