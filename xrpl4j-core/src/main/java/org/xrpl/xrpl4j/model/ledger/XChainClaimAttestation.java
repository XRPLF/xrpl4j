package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;

/**
 * A wrapper around {@link XChainClaimProofSig}s.
 *
 * <p>This class will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainClaimAttestation.class)
@JsonDeserialize(as = ImmutableXChainClaimAttestation.class)
public interface XChainClaimAttestation {

  /**
   * Construct an {@link XChainClaimAttestation} containing the specified {@link XChainClaimProofSig}.
   *
   * @param proofSig An {@link XChainClaimProofSig}.
   *
   * @return An {@link XChainClaimAttestation}.
   */
  static XChainClaimAttestation of(XChainClaimProofSig proofSig) {
    return ImmutableXChainClaimAttestation.builder()
      .xChainClaimProofSig(proofSig)
      .build();
  }

  /**
   * An {@link XChainClaimProofSig}.
   *
   * @return An {@link XChainClaimProofSig}.
   */
  @JsonProperty("XChainClaimProofSig")
  @SuppressWarnings("MethodName")
  XChainClaimProofSig xChainClaimProofSig();

}
