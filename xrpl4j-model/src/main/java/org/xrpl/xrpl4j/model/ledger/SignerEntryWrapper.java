package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A wrapper for {@link SignerEntry} to conform to the rippled API JSON structure.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerEntryWrapper.class)
@JsonDeserialize(as = ImmutableSignerEntryWrapper.class)
public interface SignerEntryWrapper {

  /**
   * Construct a new wrapper for the given {@link SignerEntry}.
   *
   * @param entry A {@link SignerEntry}.
   *
   * @return A {@link SignerEntryWrapper} wrapping the given {@link SignerEntry}.
   */
  static SignerEntryWrapper of(SignerEntry entry) {
    return ImmutableSignerEntryWrapper.builder()
      .signerEntry(entry)
      .build();
  }

  /**
   * The {@link SignerEntry} that this wrapper wraps.
   *
   * @return A {@link SignerEntry}.
   */
  @JsonProperty("SignerEntry")
  SignerEntry signerEntry();

}
