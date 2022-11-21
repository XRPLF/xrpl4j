package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.Asset;

import java.util.Optional;

/**
 * Object mapping for the AMMWithdraw transaction.
 */
public interface AmmWithdraw extends Transaction {

  /**
   * Construct a {@code AmmWithdraw} builder.
   *
   * @return An {@link ImmutableAmmWithdraw.Builder}.
   */
  static ImmutableAmmWithdraw.Builder builder() {
    return ImmutableAmmWithdraw.builder();
  }

  /**
   * A {@link Flags.AmmWithdrawFlags} for this transaction.
   *
   * @return A {@link Flags.AmmWithdrawFlags} for this transaction.
   */
  @JsonProperty("Flags")
  Flags.AmmWithdrawFlags flags();

  /**
   * The definition for one of the assets in the AMM's pool.
   *
   * @return An {@link Asset}.
   */
  @JsonProperty("Asset")
  Asset asset();

  /**
   * 	The definition for the other asset in the AMM's pool.
   *
   * @return An {@link Asset}.
   */
  @JsonProperty("Asset2")
  Asset asset2();

  /**
   * The amount of one asset to deposit to the AMM. If present, this must match the type of one of the assets
   * (tokens or XRP) in the AMM's pool.
   *
   * @return An optionally present {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  /**
   * The amount of another asset to add to the AMM. If present, this must match the type of the other asset in the
   * AMM's pool and cannot be the same asset as Amount.
   *
   * @return An optionally present {@link CurrencyAmount}.
   */
  @JsonProperty("Amount2")
  Optional<CurrencyAmount> amount2();

  /**
   * The maximum effective price, in the deposit asset, to pay for each LP Token received.
   *
   * @return An optionally present {@link CurrencyAmount}.
   */
  @JsonProperty("EPrice")
  Optional<CurrencyAmount> effectivePrice();

  /**
   * How many of the AMM's LP Tokens to buy.
   *
   * @return @return An optionally present {@link IssuedCurrencyAmount}.
   */
  @JsonProperty("LPTokensIn")
  Optional<IssuedCurrencyAmount> lpTokensIn();

  @Value.Immutable
  @JsonSerialize(as = ImmutableAmmWithdraw.class)
  @JsonDeserialize(as = ImmutableAmmWithdraw.class)
  abstract class AbstractAmmWithdraw implements AmmWithdraw {

    @Value.Check
    void checkFieldPresenceBasedOnFlags() {
      boolean lpTokenPresent = lpTokensIn().isPresent();
      boolean amountPresent = amount().isPresent();
      boolean amount2Present = amount2().isPresent();
      boolean effectivePricePresent = effectivePrice().isPresent();

      if (flags().tfLPToken()) {
        Preconditions.checkState(
          lpTokenPresent && !amountPresent && !amount2Present && !effectivePricePresent,
          "If the tfLPToken flag is set, amount, amount2, and effectivePrice cannot be present."
        );
      } else if (flags().tfWithdrawAll()) {
        Preconditions.checkState(
          !lpTokenPresent && !amountPresent && !amount2Present && !effectivePricePresent,
          "If the tfLPToken flag is set, lpTokensIn, amount, amount2, and effectivePrice cannot be present."
        );
      } else if (flags().tfTwoAsset()) {
        Preconditions.checkState(
          !lpTokenPresent && amountPresent && amount2Present && !effectivePricePresent,
          "If the tfTwoAsset flag is set, lpTokensIn and effectivePrice cannot be present."
        );
      } else if (flags().tfSingleAsset() || flags().tfOneAssetWithdrawAll()) {
        Preconditions.checkState(
          !lpTokenPresent && amountPresent && !amount2Present && !effectivePricePresent,
          "If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice cannot " +
            "be present."
        );
      } else if (flags().tfOneAssetLPToken()) {
        Preconditions.checkState(
          lpTokenPresent && amountPresent && !amount2Present && !effectivePricePresent,
          "If the tfOneAssetLPToken flag is set, amount2 and effectivePrice cannot be present."
        );
      } else if (flags().tfLimitLPToken()) {
        Preconditions.checkState(
          !lpTokenPresent && amountPresent && !amount2Present && effectivePricePresent,
          "If the tfLimitLPToken flag is set, lpTokensIn and amount2 cannot be present."
        );
      }
    }

  }
}
