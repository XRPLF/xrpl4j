package org.xrpl.xrpl4j.model.client.fees;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * The result of {@link FeeUtils#computeNetworkFee(FeeResult)} use to compute fee for a transaction based on the traffic
 * on the network. Has 3 levels for user to choose from.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNetworkFeeResult.class)
@JsonDeserialize(as = ImmutableNetworkFeeResult.class)
public interface NetworkFeeResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNetworkFeeResult.Builder}.
   */
  static ImmutableNetworkFeeResult.Builder builder() {
    return ImmutableNetworkFeeResult.builder();
  }

  /**
   * Lowest fee a user should provide for submitting a transaction. This applies when the job processing queue for the
   * server is empty.
   *
   * @return An {@link XrpCurrencyAmount} denoting the lowest fee level.
   */
  XrpCurrencyAmount feeLow();

  /**
   * Medium fee a user should provide for submitting a transaction. This applies when the job processing queue is nether
   * full nor empty.
   *
   * @return An {@link XrpCurrencyAmount} denoting the medium fee level.
   */
  XrpCurrencyAmount feeMedium();

  /**
   * Highest fee a user should provide for submitting a transaction. This applies when the job processing queue is full.
   *
   * @return An {@link XrpCurrencyAmount} denoting the highest fee level.
   */
  XrpCurrencyAmount feeHigh();
}
