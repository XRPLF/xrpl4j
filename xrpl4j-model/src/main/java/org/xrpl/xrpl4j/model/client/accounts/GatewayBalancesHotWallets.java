package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.List;

/**
 * Amounts issued to the hotwallet addresses from the request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesHotWallets.class)
@JsonDeserialize(as = ImmutableGatewayBalancesHotWallets.class)
public interface GatewayBalancesHotWallets {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesHotWallets.Builder}.
   */
  static ImmutableGatewayBalancesHotWallets.Builder builder() {
    return ImmutableGatewayBalancesHotWallets.builder();
  }

  /**
   * Address of the holder of the balances.
   *
   * @return An {@link Address} of the holder of the balances.
   */
  Address holder();

  /**
   * The balances of currencies issued to the account specified in {@link #holder()} from the issuer
   * specified in the results.
   *
   * @return A list of {@link GatewayBalancesIssuedCurrencyAmount}s specifying balances of issued currencies
   *   from the issuer.
   */
  List<GatewayBalancesIssuedCurrencyAmount> balances();
}
