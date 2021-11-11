package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.List;

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
   * Address of the issuer account.
   *
   * @return The {@link Address} of the issuer account.
   */
  Address issuer();

  /**
   * The balances of issued currencies issued by others to the account specified in {{@link #issuer()}}.
   *
   * @return A list of {@link GatewayBalancesIssuedCurrencyAmount}s of currencies issued by others.
   */
  List<GatewayBalancesIssuedCurrencyAmount> balances();

}
