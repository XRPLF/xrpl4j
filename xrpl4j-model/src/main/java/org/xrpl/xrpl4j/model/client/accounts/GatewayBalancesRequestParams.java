package org.xrpl.xrpl4j.model.client.accounts;

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
   *
   * @return An optionally specified set of operational address balances to exclude.
   */
  @Value.Default
  default Set<Address> hotwallet() {
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
   * If true, only accept an address or public key for the account parameter. Defaults to false.
   *
   * @return true if only accepting an address or public key for the account parameter, false otherwise.
   */
  @Value.Default
  default boolean strict() {
    return false;
  }

}
