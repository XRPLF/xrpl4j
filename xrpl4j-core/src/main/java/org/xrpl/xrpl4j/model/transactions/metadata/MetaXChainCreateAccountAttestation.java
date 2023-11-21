package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;

/**
 * A wrapper around {@link MetaXChainCreateAccountProofSig}s.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 * change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableMetaXChainCreateAccountAttestation.class)
@JsonDeserialize(as = ImmutableMetaXChainCreateAccountAttestation.class)
public interface MetaXChainCreateAccountAttestation {

  /**
   * An {@link MetaXChainCreateAccountProofSig}.
   *
   * @return An {@link MetaXChainCreateAccountProofSig}.
   */
  @JsonProperty("XChainCreateAccountProofSig")
  MetaXChainCreateAccountProofSig xChainCreateAccountProofSig();

}
