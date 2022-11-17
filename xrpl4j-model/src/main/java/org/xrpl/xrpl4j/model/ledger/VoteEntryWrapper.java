package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A wrapper around a {@link VoteEntry}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVoteEntryWrapper.class)
@JsonDeserialize(as = ImmutableVoteEntryWrapper.class)
public interface VoteEntryWrapper {

  /**
   * Construct a {@link VoteEntryWrapper} containing the specified
   * {@link VoteEntry}.
   *
   * @param voteEntry A {@link VoteEntry}.
   *
   * @return A {@link VoteEntryWrapper}.
   */
  static VoteEntryWrapper of(VoteEntry voteEntry) {
    return ImmutableVoteEntryWrapper.builder()
      .voteEntry(voteEntry)
      .build();
  }

  /**
   * A {@link VoteEntry}.
   *
   * @return A {@link VoteEntry}.
   */
  @JsonProperty("VoteEntry")
  VoteEntry voteEntry();

}
