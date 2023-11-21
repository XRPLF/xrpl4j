package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.ImmutableXChainClaimAttestation;

/**
 * A wrapper around {@link MetaXChainClaimProofSig}s.
 *
 * <p>This class will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableMetaXChainClaimAttestation.class)
@JsonDeserialize(as = ImmutableMetaXChainClaimAttestation.class)
public interface MetaXChainClaimAttestation {

  /**
   * An {@link MetaXChainClaimProofSig}.
   *
   * @return An {@link MetaXChainClaimProofSig}.
   */
  @JsonProperty("XChainClaimProofSig")
  MetaXChainClaimProofSig xChainClaimProofSig();

}
