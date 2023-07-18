package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.ImmutableVoteEntryWrapper;

/**
 * A wrapper around a {@link MetaVoteEntry}.
 */
@Immutable
@JsonSerialize(as = ImmutableMetaVoteEntryWrapper.class)
@JsonDeserialize(as = ImmutableMetaVoteEntryWrapper.class)
public interface MetaVoteEntryWrapper {

  /**
   * A {@link MetaVoteEntry}.
   *
   * @return A {@link MetaVoteEntry}.
   */
  @JsonProperty("VoteEntry")
  MetaVoteEntry voteEntry();

}
