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

  @JsonProperty("account")
  Address account();

  @JsonProperty("auth_accounts")
  List<AmmInfoAuthAccount> authAccounts();

  @JsonProperty("discounted_fee")
  TradingFee discountedFee();

  @JsonProperty("expiration")
  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z", locale = "en_US")
  ZonedDateTime expiration();

  @JsonProperty("price")
  IssuedCurrencyAmount price();

  @JsonProperty("time_interval")
  UnsignedInteger timeInterval();

}
