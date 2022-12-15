package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.ledger.Asset;
import org.xrpl.xrpl4j.model.ledger.AuctionSlot;
import org.xrpl.xrpl4j.model.ledger.VoteEntry;
import org.xrpl.xrpl4j.model.ledger.VoteEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAmmResult.class)
@JsonDeserialize(as = ImmutableAmmResult.class)
public interface AmmResult {

  /**
   * Construct a {@code AmmResult} builder.
   *
   * @return An {@link ImmutableAmmResult.Builder}.
   */
  static ImmutableAmmResult.Builder builder() {
    return ImmutableAmmResult.builder();
  }

  /**
   * The definition for one of the two assets this AMM holds.
   *
   * @return An {@link Asset}.
   */
  @JsonProperty("Amount")
  Asset amount();

  /**
   * The definition for the other asset this AMM holds.
   *
   * @return An {@link Asset}.
   */
  @JsonProperty("Amount2")
  Asset asset2();

  /**
   * The address of the special account that holds this AMM's assets.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("AMMAccount")
  Address ammAccount();

  /**
   * Details of the current owner of the auction slot.
   *
   * @return An {@link AuctionSlot}.
   */
  @JsonProperty("AuctionSlot")
  Optional<AuctionSlot> auctionSlot();

  /**
   * The total outstanding balance of liquidity provider tokens from this AMM instance. The holders of these tokens
   * can vote on the AMM's trading fee in proportion to their holdings, or redeem the tokens for a share of the AMM's
   * assets which grows with the trading fees collected.
   *
   * @return An {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("LPTokenBalance")
  @JsonAlias("LPToken") // FIXME: This is only here because AMM-net hasn't been updated and should be removed later on
  IssuedCurrencyAmount lpTokenBalance();

  /**
   * The percentage fee to be charged for trades against this AMM instance, in units of 1/10,000. The maximum value is
   * 1000, for a 1% fee.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("TradingFee")
  TradingFee tradingFee();

  /**
   * A list of vote objects, representing votes on the pool's trading fee.
   *
   * @return A {@link List} of {@link VoteEntry}s.
   */
  @JsonProperty("VoteSlots")
  List<VoteEntry> voteSlots();

}
