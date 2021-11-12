package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
   * Map of addresses of currencies holders to the balances of the currencies held as issued by the
   * issuer in the full response.
   *
   * @return A map of the {@link Address}es of holders of issued currencies to a list of
   *   {@link GatewayBalancesIssuedCurrencyAmount}s specifying balances of issued currencies from the issuer.
   */
  @Value.Default
  default Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balancesByHolder() {
    return Collections.emptyMap();
  }
}
