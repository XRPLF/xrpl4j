package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * The result of the gateway_balances rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesResult.class)
@JsonDeserialize(as = ImmutableGatewayBalancesResult.class)
public interface GatewayBalancesResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesResult.Builder}.
   */
  static ImmutableGatewayBalancesResult.Builder builder() {
    return ImmutableGatewayBalancesResult.builder();
  }

  /**
   * The {@link Address} of the account that issued the balances.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * Total amounts held that are issued by others. In the recommended configuration, the issuing address
   * should have none.
   *
   * @return The {@link GatewayBalancesAssets} issued to this account by others.
   */
  GatewayBalancesAssets assets();

  /**
   * Amounts issued to the hotwallet addresses from the request. In the original JSON entity, the keys are addresses
   * and the values are arrays of currency amounts they hold. The wrapping entity packages these in a
   * {@link GatewayBalancesHotWallets} instance instead.
   *
   * @return The balances of the hotwallets field wrapped in a {@link GatewayBalancesHotWallets} instance.
   */
  GatewayBalancesHotWallets balances();

  /**
   * Total amounts issued to addresses not included in hotwallets. In the original JSON entity, this is presented
   * as a map of currencies to the total value issued. The wrapping entity packages these in a
   * {@link GatewayBalancesObligations} instance instead.
   *
   * @return The balances for obligations from this issuer to accounts not included in the hotwallets request
   *   field, wrapped in a {@link GatewayBalancesObligations} instance.
   */
  GatewayBalancesObligations obligations();

  /**
   * The identifying hash the ledger version that was used when retrieving this data.
   *
   * @return An optionally-present {@link org.xrpl.xrpl4j.model.transactions.Hash256}.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Get {@link #ledgerHash()}, or throw an {@link IllegalStateException} if {@link #ledgerHash()} is empty.
   *
   * @return The value of {@link #ledgerHash()}.
   * @throws IllegalStateException If {@link #ledgerHash()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Hash256 ledgerHashSafe() {
    return ledgerHash()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerHash."));
  }

  /**
   * The ledger index of the ledger version that was used when retrieving this data.
   *
   * @return An optionally-present {@link LedgerIndex} representing the ledger index of the response.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Get {@link #ledgerIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerIndex()} is empty.
   *
   * @return The value of {@link #ledgerIndex()}.
   * @throws IllegalStateException If {@link #ledgerIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerIndexSafe() {
    return ledgerIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerIndex."));
  }

  /**
   * The ledger index of the current open ledger, which was used when retrieving this information.
   *
   * @return An optionally-present {@link LedgerIndex} representing the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * Whether or not the information in this response comes from a validated ledger version.
   *
   * @return {@code true} if the information is from a validated ledger, otherwise {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

}
