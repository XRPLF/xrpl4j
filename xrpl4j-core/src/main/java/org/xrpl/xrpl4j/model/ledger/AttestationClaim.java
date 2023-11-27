package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;

import java.util.Optional;

/**
 * An attestation that an {@link org.xrpl.xrpl4j.model.transactions.XChainCommit} transaction occurred on another chain.
 *
 * <p>This class will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableAttestationClaim.class)
@JsonDeserialize(as = ImmutableAttestationClaim.class)
public interface AttestationClaim extends Attestation {

  /**
   * Construct a {@code AttestationClaim} builder.
   *
   * @return An {@link ImmutableAttestationClaim.Builder}.
   */
  static ImmutableAttestationClaim.Builder builder() {
    return ImmutableAttestationClaim.builder();
  }

  /**
   * The destination account for the funds on the destination chain.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

  /**
   * The ID of the {@link XChainOwnedClaimIdObject}.
   *
   * @return An {@link XChainClaimId}.
   */
  @JsonProperty("XChainClaimID")
  XChainClaimId xChainClaimId();

}
