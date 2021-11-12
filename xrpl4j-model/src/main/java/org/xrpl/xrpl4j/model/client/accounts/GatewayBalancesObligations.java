package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.List;

/**
 * Total amounts issued to addresses not included in the original request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesObligations.class)
@JsonDeserialize(as = ImmutableGatewayBalancesObligations.class)
public interface GatewayBalancesObligations {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesObligations.Builder}
   */
  static ImmutableGatewayBalancesObligations.Builder builder() {
    return ImmutableGatewayBalancesObligations.builder();
  }

  /**
   * The balances of issued currencies from the issuer in the results that are not
   * includes in the hotwallet balances.
   *
   * @return A list of {@link GatewayBalancesIssuedCurrencyAmount}s for issued currencies not
   *   included in the hotwallet balances.
   */
  @Value.Default
  default List<GatewayBalancesIssuedCurrencyAmount> balances() {
    return Collections.emptyList();
  }
}
