package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * The result of an account_currencies rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountCurrenciesResult.class)
@JsonDeserialize(as = ImmutableAccountCurrenciesResult.class)
public interface AccountCurrenciesResult extends XrplResult {

  /**
   * Construct a builder.
   *
   * @return {@link ImmutableAccountCurrenciesResult.Builder}
   */
  static ImmutableAccountCurrenciesResult.Builder builder() {
    return ImmutableAccountCurrenciesResult.builder();
  }

  /**
   * The identifying Hash of the ledger version used to generate this response.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Get {@link #ledgerHash()}, or throw an {@link IllegalStateException} if {@link #ledgerHash()} is empty.
   *
   * @return The value of {@link #ledgerHash()}.
   *
   * @throws IllegalStateException If {@link #ledgerHash()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Hash256 ledgerHashSafe() {
    return ledgerHash()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerHash."));
  }

  /**
   * The Ledger Index of the ledger version used to generate this response.
   *
   * @return A {@link LedgerIndex}.
   *
   * @deprecated When requesting Account Channels from a non-validated ledger, the result will not contain this field.
   *   To prevent this class from throwing an error when requesting Account Currencies from a non-validated ledger, this
   *   field is currently marked as {@link Nullable}. However, this field will be {@link Optional} in a future release.
   */
  @Deprecated
  @Nullable
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  /**
   * Get {@link #ledgerIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerIndex()} is null.
   *
   * @return The value of {@link #ledgerIndex()}.
   *
   * @throws IllegalStateException If {@link #ledgerIndex()} is null.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerIndexSafe() {
    return Optional.ofNullable(ledgerIndex())
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerIndex."));
  }

  /**
   * The ledger index of the current open ledger, which was used when retrieving this information. Only present in
   * responses to requests with ledger_index = "current".
   *
   * @return An optionally-present {@link LedgerIndex} representing the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * Get {@link #ledgerCurrentIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerCurrentIndex()} is
   * empty.
   *
   * @return The value of {@link #ledgerCurrentIndex()}.
   *
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }

  /**
   * If true, the information in this response comes from a validated ledger version. Otherwise, the information is
   * subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, {@code false} if
   *   not.
   */
  boolean validated();

  /**
   * Array of currency codes for currencies that this account can receive.
   *
   * @return Array of currencies that this account can receive.
   */
  @JsonProperty("receive_currencies")
  List<String> receiveCurrencies();

  /**
   * Array of currency codes for currencies that this account can send.
   *
   * @return Array of currencies that this account can send.
   */
  @JsonProperty("send_currencies")
  List<String> sendCurrencies();
}
