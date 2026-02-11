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
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.PermissionWrapper;

import java.util.List;

/**
 * This object represents a set of permissions that an account has delegated to another account.
 * {@link DelegateSet} transactions create these objects.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDelegateObject.class)
@JsonDeserialize(as = ImmutableDelegateObject.class)
public interface DelegateObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableDelegateObject.Builder}.
   */
  static ImmutableDelegateObject.Builder builder() {
    return ImmutableDelegateObject.builder();
  }

  /**
   * The type of ledger object, which will always be "Delegate" in this case.
   *
   * @return Always {@link LedgerEntryType#DELEGATE}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.DELEGATE;
  }

  /**
   * The account that wants to authorize another account.
   *
   * @return The {@link Address} of the account.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The authorized account.
   *
   * @return The {@link Address} of the authorized account.
   */
  @JsonProperty("Authorize")
  Address authorize();

  /**
   * The transaction permissions that the account has access to.
   *
   * @return A {@link List} of {@link PermissionWrapper}s.
   */
  @JsonProperty("Permissions")
  List<PermissionWrapper> permissions();

  /**
   * A hint indicating which page of the sender's owner directory links to this object,
   * in case the directory consists of multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.
   *
   * @return A {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * A bit-map of boolean flags. No flags are defined for the Delegate object
   * type, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

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
   * The unique ID of the {@link DelegateObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();
}
