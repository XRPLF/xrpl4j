package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.ImmutableVoteEntry;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;

import java.util.Optional;

/**
 * Describes a vote for the trading fee on an AMM by an LP.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 * change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableMetaVoteEntry.class)
@JsonDeserialize(as = ImmutableMetaVoteEntry.class)
@Beta
public interface MetaVoteEntry {

  /**
   * The address of the LP who voted.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * The trading fee that the LP voted for.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("TradingFee")
  Optional<TradingFee> tradingFee();

  /**
   * The weight of the LP's vote.
   *
   * @return The {@link VoteWeight}.
   */
  @JsonProperty("VoteWeight")
  Optional<VoteWeight> voteWeight();

}
