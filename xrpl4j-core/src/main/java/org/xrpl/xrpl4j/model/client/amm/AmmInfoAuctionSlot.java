package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Object mapping for an AMM auction slot returned in response to an {@code amm_info} RPC call. The structure
 * of the response object is similar but has a slightly different format from
 * {@link org.xrpl.xrpl4j.model.ledger.AuctionSlot}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmInfoAuctionSlot.class)
@JsonDeserialize(as = ImmutableAmmInfoAuctionSlot.class)
public interface AmmInfoAuctionSlot {

  /**
   * Construct a {@code AmmInfoAuctionSlot} builder.
   *
   * @return An {@link ImmutableAmmInfoAuctionSlot.Builder}.
   */
  static ImmutableAmmInfoAuctionSlot.Builder builder() {
    return ImmutableAmmInfoAuctionSlot.builder();
  }

  /**
   * The current owner of this auction slot.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("account")
  Address account();

  /**
   * A list of at most 4 additional accounts that are authorized to trade at the discounted fee for this AMM instance.
   *
   * @return A {@link List} of {@link AmmInfoAuthAccount}s.
   */
  @JsonProperty("auth_accounts")
  List<AmmInfoAuthAccount> authAccounts();

  /**
   * The trading fee to be charged to the auction owner. By default this is 0, meaning that the auction owner can trade
   * at no fee instead of the standard fee for this AMM.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("discounted_fee")
  TradingFee discountedFee();

  /**
   * The time when this slot expires, as a {@link ZonedDateTime}.
   *
   * @return An {@link ZonedDateTime}
   */
  // rippled reports the expiration date/time in ISO 8601 format, which is natively supported by ZonedDateTime.
  // Therefore, this field does not require a @JsonFormat(pattern = ...) annotation similar to how other ZonedDateTime
  // fields are annotated in this library.
  @JsonProperty("expiration")
  @JsonFormat(locale = "en_US")
  ZonedDateTime expiration();

  /**
   * The amount the auction owner paid to win this slot, in LP Tokens.
   *
   * @return An {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("price")
  IssuedCurrencyAmount price();

  /**
   * An {@link UnsignedInteger} between 1 and 20 denoting the time slot used for the continuous auction slot pricing
   * mechanism of the AMM.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("time_interval")
  UnsignedInteger timeInterval();

}
