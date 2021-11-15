package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Request parameters for the gateway_balances rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesRequestParams.class)
@JsonDeserialize(as = ImmutableGatewayBalancesRequestParams.class)
public interface GatewayBalancesRequestParams extends XrplRequestParams {
  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesRequestParams.Builder}.
   */
  static ImmutableGatewayBalancesRequestParams.Builder builder() {
    return ImmutableGatewayBalancesRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account, which should be an issuer. The request assets and
   * balances associated with the issuer account.
   *
   * @return The {@link Address} for the account.
   */
  Address account();

  /**
   * An optional set of addresses of operational accounts that should not be included in the
   * balances field of the response. Defaults to an empty set.
   * balances field of the response. Defaults to an empty set.
   *
   * @return An optionally specified set of operational address balances to exclude.
   */
  @Value.Default
  @JsonProperty("hotwallet")
  default Set<Address> hotWallets() {
    return Collections.emptySet();
  }

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  @Nullable
  LedgerSpecifier ledgerSpecifier();

  /**
   * Forcibly set to true as true implies either a public key or address is being specified as the
   * account. Setting this field to false allows for secrets to be passed in which this API explictly
   * discourages.
   *
   * @return true to force usage of either a public key or address and not a secret.
   */
  @Value.Derived
  default boolean strict() {
    return true;
  }

}
