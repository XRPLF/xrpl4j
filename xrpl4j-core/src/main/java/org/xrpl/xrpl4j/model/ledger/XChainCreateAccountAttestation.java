package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;

/**
 * A wrapper around {@link XChainCreateAccountProofSig}s.
 *
 * <p>This class will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API is
 * subject to
 * change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainCreateAccountAttestation.class)
@JsonDeserialize(as = ImmutableXChainCreateAccountAttestation.class)
public interface XChainCreateAccountAttestation {

  /**
   * Construct an {@link XChainCreateAccountAttestation} containing the specified {@link XChainCreateAccountProofSig}.
   *
   * @param proofSig An {@link XChainCreateAccountProofSig}.
   *
   * @return An {@link XChainCreateAccountAttestation}.
   */
  static XChainCreateAccountAttestation of(XChainCreateAccountProofSig proofSig) {
    return ImmutableXChainCreateAccountAttestation.builder()
      .xChainCreateAccountProofSig(proofSig)
      .build();
  }

  /**
   * An {@link XChainCreateAccountProofSig}.
   *
   * @return An {@link XChainCreateAccountProofSig}.
   */
  @JsonProperty("XChainCreateAccountProofSig")
  @SuppressWarnings("MethodName")
  XChainCreateAccountProofSig xChainCreateAccountProofSig();

}
