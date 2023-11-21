package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;

/**
 * A wrapper around {@link XChainCreateAccountProofSig}s.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 * change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainCreateAccountProofSigWrapper.class)
@JsonDeserialize(as = ImmutableXChainCreateAccountProofSigWrapper.class)
public interface XChainCreateAccountProofSigWrapper {

  /**
   * Construct an {@link XChainCreateAccountProofSigWrapper} containing the specified
   * {@link XChainCreateAccountProofSig}.
   *
   * @param proofSig An {@link XChainCreateAccountProofSig}.
   *
   * @return An {@link XChainCreateAccountProofSigWrapper}.
   */
  static XChainCreateAccountProofSigWrapper of(XChainCreateAccountProofSig proofSig) {
    return ImmutableXChainCreateAccountProofSigWrapper.builder()
      .xChainCreateAccountProofSig(proofSig)
      .build();
  }

  /**
   * An {@link XChainCreateAccountProofSig}.
   *
   * @return An {@link XChainCreateAccountProofSig}.
   */
  @JsonProperty("XChainCreateAccountProofSig")
  XChainCreateAccountProofSig xChainCreateAccountProofSig();

}
