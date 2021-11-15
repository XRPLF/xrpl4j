package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Total amounts held that are issued by others. In the recommended configuration, the issuing address
 * should have none.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesAssets.class)
@JsonDeserialize(as = ImmutableGatewayBalancesAssets.class)
public interface GatewayBalancesAssets {

  /**
   * Constructs a builder for this class.
   * @return An {@link ImmutableGatewayBalancesAssets.Builder}.
   */
  static ImmutableGatewayBalancesAssets.Builder builder() {
    return ImmutableGatewayBalancesAssets.builder();
  }

  /**
   * The balances of issued currencies issued by the address which is the key of this map.
   *
   * @return A map of issued currencies, keyed by the issuer {@link Address} to a list of
   *   {@link GatewayBalancesIssuedCurrencyAmount}s of currencies issued
   */
  @Value.Default
  default Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balancesByIssuer() {
    return Collections.emptyMap();
  }

}
