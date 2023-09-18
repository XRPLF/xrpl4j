package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.util.List;
import java.util.Optional;

/**
 * Information about the requested AMM ledger entry. This response is very closely related to
 * {@link org.xrpl.xrpl4j.model.ledger.AmmObject}, however rippled returns the object in a different format in
 * responses to {@code amm_info} RPC requests.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmInfo.class)
@JsonDeserialize(as = ImmutableAmmInfo.class)
public interface AmmInfo extends XrplResult {

  /**
   * Construct a {@code AmmInfo} builder.
   *
   * @return An {@link ImmutableAmmInfo.Builder}.
   */
  static ImmutableAmmInfo.Builder builder() {
    return ImmutableAmmInfo.builder();
  }

  /**
   * The address of the special account that holds this AMM's assets.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("amm_account")
  Address ammAccount();

  /**
   * The definition for one of the two assets this AMM holds.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("amount")
  CurrencyAmount amount();

  /**
   * The definition for the other asset this AMM holds.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("amount2")
  CurrencyAmount amount2();

  /**
   * Whether the first asset of the AMM is frozen. Always false is the first asset is XRP.
   *
   * @return {@code true} if asset 1 is frozen, otherwise {@code false}.
   */
  @Value.Default
  @JsonProperty("asset_frozen")
  default boolean assetFrozen() {
    return false;
  }

  /**
   * Whether the second asset of the AMM is frozen. Always false is the second asset is XRP.
   *
   * @return {@code true} if asset 2 is frozen, otherwise {@code false}.
   */
  @Value.Default
  @JsonProperty("asset2_frozen")
  default boolean asset2Frozen() {
    return false;
  }

  /**
   * Details of the current owner of the auction slot.
   *
   * @return An {@link AmmInfoAuctionSlot}.
   */
  @JsonProperty("auction_slot")
  Optional<AmmInfoAuctionSlot> auctionSlot();

  /**
   * The total outstanding balance of liquidity provider tokens from this AMM instance. The holders of these tokens
   * can vote on the AMM's trading fee in proportion to their holdings, or redeem the tokens for a share of the AMM's
   * assets which grows with the trading fees collected.
   *
   * @return An {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("lp_token")
  IssuedCurrencyAmount lpToken();

  /**
   * The percentage fee to be charged for trades against this AMM instance, in units of 1/10,000. The maximum value is
   * 1000, for a 1% fee.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("trading_fee")
  TradingFee tradingFee();

  /**
   * A list of vote objects, representing votes on the pool's trading fee.
   *
   * @return A {@link List} of {@link AmmInfoVoteEntry}s.
   */
  @JsonProperty("vote_slots")
  List<AmmInfoVoteEntry> voteSlots();

}
