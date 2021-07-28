package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Provides a wrapper for {@link Signer}s, in order to conform to the XRPL transaction JSON structure.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerWrapper.class)
@JsonDeserialize(as = ImmutableSignerWrapper.class)
public interface SignerWrapper {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignerWrapper.Builder}.
   */
  static ImmutableSignerWrapper.Builder builder() {
    return ImmutableSignerWrapper.builder();
  }

  /**
   * Construct a {@link SignerWrapper} wrapping the given {@link Signer}.
   *
   * @param signer A {@link Signer}.
   *
   * @return A {@link SignerWrapper}.
   */
  static SignerWrapper of(Signer signer) {
    return builder().signer(signer).build();
  }

  /**
   * The {@link Signer} that this wrapper wraps.
   *
   * @return The {@link Signer} that this wrapper wraps.
   */
  @JsonProperty("Signer")
  Signer signer();
}
