package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

/**
 * Parameters that uniquely identify an {@link org.xrpl.xrpl4j.model.ledger.CredentialObject} on ledger that can be used in
 * a {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.CredentialObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableCredentialLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableCredentialLedgerEntryParams.class)
public interface CredentialLedgerEntryParams {

    /**
     * Construct a {@code CredentialLedgerEntryParams} builder.
     *
     * @return An {@link ImmutableCredentialLedgerEntryParams.Builder}.
     */
    static ImmutableCredentialLedgerEntryParams.Builder builder() {
        return ImmutableCredentialLedgerEntryParams.builder();
    }

    /**
     * The subject of the credential.
     *
     * @return The {@link Address} of the subject of this credential.
     */
    Address subject();

    /**
     * The issuer of the credential.
     *
     * @return The {@link Address} of the issuer of this credential.
     */
    Address issuer();

    /**
     * A (hex-encoded) value to identify the type of credential from the issuer.
     *
     * @return The {@link CredentialType} denoting the CredentialType
     */
    @JsonProperty("credential_type")
    CredentialType credentialType();

}
