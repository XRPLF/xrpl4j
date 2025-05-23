package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

@Value.Immutable
@JsonSerialize(as = ImmutableCredentialAccept.class)
@JsonDeserialize(as = ImmutableCredentialAccept.class)
public interface CredentialAccept extends Transaction {

    /**
     * Construct a {@code CredentialAccept} builder.
     *
     * @return An {@link ImmutableCredentialAccept.Builder}.
     */
    static ImmutableCredentialAccept.Builder builder() {
        return ImmutableCredentialAccept.builder();
    }

    /**
     * The issuer of the credential.
     *
     * @return The {@link Address} of the issuer this credential.
     */
    @JsonProperty("Issuer")
    Address issuer();

    /**
     * A (hex-encoded) value to identify the type of credential from the issuer.
     *
     * @return The {@link CredentialType} denoting the CredentialType
     */
    @JsonProperty("CredentialType")
    CredentialType credentialType();

    /**
     * Set of {@link TransactionFlags}'s for this {@link CredentialAccept}, which only allows the
     * {@code tfFullyCanonicalSig} flag, which is deprecated.
     *
     * @return Always {@link TransactionFlags#EMPTY}.
     */
    @JsonProperty("Flags")
    @Value.Default
    default TransactionFlags flags() {
        return TransactionFlags.EMPTY;
    }
}
