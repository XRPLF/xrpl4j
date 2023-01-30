package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.ledger.ImmutableVoteEntry;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;

/**
 * Describes a vote for the trading fee on an AMM by an LP.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmInfoVoteEntry.class)
@JsonDeserialize(as = ImmutableAmmInfoVoteEntry.class)
public interface AmmInfoVoteEntry {

  /**
   * Construct a {@code AmmInfoVoteEntry} builder.
   *
   * @return An {@link ImmutableAmmInfoVoteEntry.Builder}.
   */
  static ImmutableAmmInfoVoteEntry.Builder builder() {
    return ImmutableAmmInfoVoteEntry.builder();
  }

  /**
   * The address of the LP who voted.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("account")
  Address account();

  /**
   * The trading fee that the LP voted for.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("trading_fee")
  TradingFee tradingFee();

  /**
   * The weight of the LP's vote.
   *
   * @return The {@link VoteWeight}.
   */
  @JsonProperty("vote_weight")
  VoteWeight voteWeight();

}
