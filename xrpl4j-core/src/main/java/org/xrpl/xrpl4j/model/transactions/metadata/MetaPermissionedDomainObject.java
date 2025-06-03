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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;

/**
 * This object represents a permissioned domain controlled by the
 * {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject#owner}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaPermissionedDomainObject.class)
@JsonDeserialize(as = ImmutableMetaPermissionedDomainObject.class)
public interface MetaPermissionedDomainObject extends MetaLedgerObject {

  /**
   * A bit-map of boolean flags. No flags are defined
   * for {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The account that controls the settings of the domain.
   *
   * @return The {@link Address} of the domain owner.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * A hint indicating which page of the sender's owner directory links to this object,
   * in case the directory consists of multiple pages.
   *
   * @return A {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The Sequence value of the {@link org.xrpl.xrpl4j.model.transactions.PermissionedDomainSet} transaction that created
   * this domain. Used in combination with the {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject#owner}
   * to identify this domain.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * The credentials that are accepted by the domain.
   * Ownership of one of these credentials automatically makes you a member of the domain.
   *
   * @return A list of {@link CredentialWrapper}.
   */
  @JsonProperty("AcceptedCredentials")
  List<CredentialWrapper> acceptedCredentials();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTxnId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return A {@link UnsignedInteger} representing the previous transaction sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();
}
