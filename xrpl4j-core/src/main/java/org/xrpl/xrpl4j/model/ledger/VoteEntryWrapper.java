package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;

/**
 * A wrapper around a {@link VoteEntry}.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 *  change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVoteEntryWrapper.class)
@JsonDeserialize(as = ImmutableVoteEntryWrapper.class)
@Beta
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
