package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.AmmObject;
import org.xrpl.xrpl4j.model.ledger.ImmutableAuctionSlot;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an AuctionSlot object in an {@link AmmObject}, containing details of the current owner of the auction
 * slot.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 * change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableMetaAuctionSlot.class)
@JsonDeserialize(as = ImmutableMetaAuctionSlot.class)
@Beta
public interface MetaAuctionSlot {

  /**
   * The current owner of this auction slot.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * A list of at most 4 additional accounts that are authorized to trade at the discounted fee for this AMM instance.
   *
   * @return A {@link List} of {@link MetaAuthAccountWrapper}s.
   */
  @JsonProperty("AuthAccounts")
  List<MetaAuthAccountWrapper> authAccounts();

  /**
   * Extracts all the addresses found in the {@link MetaAuthAccount}s found in {@link #authAccounts()}.
   *
   * @return A {@link List} of {@link Address}.
   */
  @JsonIgnore
  @Value.Derived
  default List<Address> authAccountsAddresses() {
    return authAccounts().stream()
      .map(MetaAuthAccountWrapper::authAccount)
      .map(MetaAuthAccount::account)
      .collect(Collectors.toList());
  }

  /**
   * The trading fee to be charged to the auction owner. By default this is 0, meaning that the auction owner can trade
   * at no fee instead of the standard fee for this AMM.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("DiscountedFee")
  Optional<TradingFee> discountedFee();

  /**
   * The amount the auction owner paid to win this slot, in LP Tokens.
   *
   * @return An {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("Price")
  Optional<IssuedCurrencyAmount> price();

  /**
   * The time when this slot expires, in seconds since the Ripple Epoch.
   *
   * @return An {@link UnsignedInteger}
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

}
