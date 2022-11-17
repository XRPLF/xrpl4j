package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an AuctionSlot object in an {@link AmmObject}, containing details of the current owner of the auction
 * slot.
 */
// TODO: Add Optional<TimeInterval> field for amm_info results
@Value.Immutable
@JsonSerialize(as = ImmutableAuctionSlot.class)
@JsonDeserialize(as = ImmutableAuctionSlot.class)
public interface AuctionSlot {

  /**
   * Construct a {@code AuctionSlot} builder.
   *
   * @return An {@link ImmutableAuctionSlot.Builder}.
   */
  static ImmutableAuctionSlot.Builder builder() {
    return ImmutableAuctionSlot.builder();
  }

  /**
   * The current owner of this auction slot.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * A list of at most 4 additional accounts that are authorized to trade at the discounted fee for this AMM instance.
   *
   * @return A {@link List} of {@link AuthAccountWrapper}s.
   */
  @JsonProperty("AuthAccounts")
  List<AuthAccountWrapper> authAccounts();

  /**
   * Extracts all the addresses found in the {@link AuthAccount}s found in {@link #authAccounts()}.
   *
   * @return A {@link List} of {@link Address}.
   */
  @JsonIgnore
  @Value.Derived
  default List<Address> authAccountsAddresses() {
    return authAccounts().stream()
      .map(AuthAccountWrapper::authAccount)
      .map(AuthAccount::account)
      .collect(Collectors.toList());
  }

  /**
   * The trading fee to be charged to the auction owner. By default this is 0, meaning that the auction owner can trade
   * at no fee instead of the standard fee for this AMM.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("DiscountedFee")
  TradingFee discountedFee();

  /**
   * The amount the auction owner paid to win this slot, in LP Tokens.
   *
   * @return An {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("Price")
  IssuedCurrencyAmount price();

  /**
   * The time when this slot expires, in seconds since the Ripple Epoch.
   *
   * @return An {@link UnsignedInteger}
   */
  @JsonProperty("Expiration")
  UnsignedInteger expiration();

}
