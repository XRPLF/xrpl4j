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
import org.xrpl.xrpl4j.model.flags.CredentialFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialUri;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * A Credential object represents a credential,
 * which contains an attestation about a subject account from a credential issuer account.
 * The meaning of the attestation is defined by the issuer.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaCredentialObject.class)
@JsonDeserialize(as = ImmutableMetaCredentialObject.class)
public interface MetaCredentialObject extends MetaLedgerObject {

  /**
   * The account that this credential is for.
   *
   * @return The {@link Address} of the credential recipient.
   */
  @JsonProperty("Subject")
  Optional<Address> subject();

  /**
   * A hint indicating which page of the subject's owner directory links to this entry,
   * in case the directory consists of multiple pages.
   *
   * @return A {@link String} containing the subject node hint.
   */
  @JsonProperty("SubjectNode")
  Optional<String> subjectNode();

  /**
   * The account that issued this credential.
   *
   * @return The {@link Address} of the credential issuer.
   */
  @JsonProperty("Issuer")
  Optional<Address> issuer();

  /**
   * A hint indicating which page of the issuer's directory links to this entry,
   * in case the directory consists of multiple pages.
   *
   * @return A {@link String} containing the issuer node hint.
   */
  @JsonProperty("IssuerNode")
  Optional<String> issuerNode();


  /**
   * A set of boolean {@link CredentialFlags} containing options
   * enabled for this object.
   *
   * @return The {@link CredentialFlags} for this object.
   */
  @JsonProperty("Flags")
  Optional<CredentialFlags> flags();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTxnId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return A {@link UnsignedInteger} representing the previous transaction sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<UnsignedInteger> previousTransactionLedgerSequence();

  /**
   * A (hex-encoded) value to identify the type of credential from the issuer.
   * This field is limited to a maximum length of 64 bytes.
   *
   * @return A {@link CredentialType} defining the type of credential.
   */
  @JsonProperty("CredentialType")
  Optional<CredentialType> credentialType();

  /**
   * Optional additional data about the credential (such as a link to the VC document).
   * This field isn't checked for validity and is limited to a maximum length of 256 bytes.
   *
   * @return An {@link Optional} of type {@link CredentialUri}.
   */
  @JsonProperty("URI")
  Optional<CredentialUri> uri();

  /**
   * Time after which the credential is expired, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger}.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

}
