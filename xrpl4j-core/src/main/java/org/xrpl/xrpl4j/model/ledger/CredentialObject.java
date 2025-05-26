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

import java.util.Optional;

/**
 * A Credential entry represents a credential,
 * which contains an attestation about a subject account from a credential issuer account.
 * The meaning of the attestation is defined by the issuer.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCredentialObject.class)
@JsonDeserialize(as = ImmutableCredentialObject.class)
public interface CredentialObject extends LedgerObject {

    /**
     * Construct a builder for this class.
     *
     * @return An {@link ImmutableCredentialObject.Builder}.
     */
    static ImmutableCredentialObject.Builder builder() {
        return ImmutableCredentialObject.builder();
    }

    /**
     * Indicates that this object is a {@link CredentialObject} object.
     *
     * @return Always {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#CREDENTIAL}.
     */
    @JsonProperty("LedgerEntryType")
    @Value.Derived
    default LedgerEntryType ledgerEntryType() {
        return LedgerEntryType.CREDENTIAL;
    }

    /**
     * The account that this credential is for.
     *
     * @return The {@link Address} of the credential recipient.
     */
    @JsonProperty("Subject")
    Address subject();

    /**
     * A hint indicating which page of the subject's owner directory links to this entry,
     * in case the directory consists of multiple pages.
     *
     * @return A {@link String} containing the subject node hint.
     */
    @JsonProperty("SubjectNode")
    String subjectNode();

    /**
     * The account that issued this credential.
     *
     * @return The {@link Address} of the credential issuer.
     */
    @JsonProperty("Issuer")
    Address issuer();

    /**
     * A hint indicating which page of the issuer's directory links to this entry,
     * in case the directory consists of multiple pages.
     *
     * @return A {@link String} containing the issuer node hint.
     */
    @JsonProperty("IssuerNode")
    String issuerNode();

    /**
     * A bit-map of boolean flags. No flags are defined for {@link CredentialObject}, so this value is always 0.
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
    Hash256 previousTxnId();

    /**
     * The index of the ledger that contains the transaction that most recently modified this object.
     *
     * @return A {@link UnsignedInteger} representing the previous transaction sequence.
     */
    @JsonProperty("PreviousTxnLgrSeq")
    UnsignedInteger previousTransactionLedgerSequence();

    /**
     * Time after which the credential is expired, in
     * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
     *
     * @return An {@link Optional} of type {@link UnsignedInteger}.
     */
    @JsonProperty("Expiration")
    Optional<UnsignedInteger> expiration();

    /**
     * The unique ID of the {@link CredentialObject}.
     *
     * @return A {@link Hash256}.
     */
    Hash256 index();
}
