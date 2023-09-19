package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an AMM ledger object, which describes a single Automated Market Maker instance.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 *  change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmObject.class)
@JsonDeserialize(as = ImmutableAmmObject.class)
@Beta
public interface AmmObject extends LedgerObject {

  /**
   * Construct a {@code AmmObject} builder.
   *
   * @return An {@link ImmutableAmmObject.Builder}.
   */
  static ImmutableAmmObject.Builder builder() {
    return ImmutableAmmObject.builder();
  }

  /**
   * The type of ledger object, which will always be "AMM" in this case.
   *
   * @return Always returns {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#AMM}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.AMM;
  }

  /**
   * A bit-map of boolean flags. No flags are defined for {@link AmmObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The definition for one of the two assets this AMM holds.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset")
  Issue asset();

  /**
   * The definition for the other asset this AMM holds.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset2")
  Issue asset2();

  /**
   * The address of the special account that holds this AMM's assets.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

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
   * @return A {@link List} of {@link VoteEntryWrapper}s.
   */
  @JsonProperty("VoteSlots")
  List<VoteEntryWrapper> voteSlots();

  /**
   * Unwraps the {@link VoteEntryWrapper}s in {@link #voteSlots()} for easier access to {@link VoteEntry}s.
   *
   * @return A {@link List} of {@link VoteEntry}.
   */
  @JsonIgnore
  @Value.Derived
  default List<VoteEntry> voteSlotsUnwrapped() {
    return voteSlots().stream()
      .map(VoteEntryWrapper::voteEntry)
      .collect(Collectors.toList());
  }
}
