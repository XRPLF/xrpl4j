package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;
import org.xrpl.xrpl4j.model.ledger.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Object mapping for the AMMBid transaction.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 * change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmBid.class)
@JsonDeserialize(as = ImmutableAmmBid.class)
@Beta
public interface AmmBid extends Transaction {

  /**
   * Construct a {@code AmmBid} builder.
   *
   * @return An {@link ImmutableAmmBid.Builder}.
   */
  static ImmutableAmmBid.Builder builder() {
    return ImmutableAmmBid.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link AmmBid}, which only allows the {@code tfFullyCanonicalSig} flag,
   * which is deprecated.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The definition for one of the assets in the AMM's pool.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset")
  Issue asset();

  /**
   * The definition for the other asset in the AMM's pool.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset2")
  Issue asset2();

  /**
   * Pay at least this amount for the slot. Setting this value higher makes it harder for others to outbid you. If
   * omitted, pay the minimum necessary to win the bid.
   *
   * @return An optionally present {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("BidMin")
  Optional<IssuedCurrencyAmount> bidMin();

  /**
   * Pay at most this amount for the slot. If the cost to win the bid is higher than this amount, the transaction fails.
   * If omitted, pay as much as necessary to win the bid.
   *
   * @return An optionally present {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("BidMax")
  Optional<IssuedCurrencyAmount> bidMax();

  /**
   * A list of up to 4 additional accounts that you allow to trade at the discounted fee. This cannot include the
   * address of the transaction sender
   *
   * @return A {@link List}
   */
  @JsonProperty("AuthAccounts")
  List<AuthAccountWrapper> authAccounts();

}
