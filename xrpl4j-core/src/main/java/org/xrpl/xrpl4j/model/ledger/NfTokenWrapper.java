package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A wrapper for {@link NfToken} to conform to the rippled API JSON structure.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenWrapper.class)
@JsonDeserialize(as = ImmutableNfTokenWrapper.class)
public interface NfTokenWrapper {

  /**
   * Construct a new wrapper for the given {@link NfToken}.
   *
   * @param nfToken A {@link NfToken}.
   *
   * @return A {@link NfTokenWrapper} wrapping the given {@link NfToken}.
   */
  static NfTokenWrapper of(NfToken nfToken) {
    return ImmutableNfTokenWrapper.builder().nfToken(nfToken).build();
  }

  /**
   * The {@link NfToken} that this wrapper wraps.
   *
   * @return A {@link NfToken}.
   */
  @JsonProperty("NFToken")
  NfToken nfToken();

}
