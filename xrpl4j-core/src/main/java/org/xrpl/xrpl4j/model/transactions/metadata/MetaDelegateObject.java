package org.xrpl.xrpl4j.model.transactions.metadata;

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
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.PermissionWrapper;

import java.util.List;
import java.util.Optional;

/**
 * Represents a Delegate ledger object in transaction metadata, which describes a set of permissions
 * that an account has delegated to another account.
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaDelegateObject.class)
@JsonDeserialize(as = ImmutableMetaDelegateObject.class)
@Beta
public interface MetaDelegateObject extends MetaLedgerObject {

  /**
   * The account that wants to authorize another account.
   *
   * @return An {@link Optional} {@link Address} of the account.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * The authorized account.
   *
   * @return An {@link Optional} {@link Address} of the authorized account.
   */
  @JsonProperty("Authorize")
  Optional<Address> authorize();

  /**
   * The transaction permissions that the account has access to.
   *
   * @return An {@link Optional} {@link List} of {@link PermissionWrapper}s.
   */
  @JsonProperty("Permissions")
  Optional<List<PermissionWrapper>> permissions();

  /**
   * A hint indicating which page of the sender's owner directory links to this object,
   * in case the directory consists of multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.
   *
   * @return An {@link Optional} {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * A bit-map of boolean flags. No flags are defined for the Delegate object
   * type, so this value is always 0.
   *
   * @return An {@link Optional} {@link Flags}.
   */
  @JsonProperty("Flags")
  Optional<Flags> flags();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return An {@link Optional} {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link Optional} {@link LedgerIndex} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

}

