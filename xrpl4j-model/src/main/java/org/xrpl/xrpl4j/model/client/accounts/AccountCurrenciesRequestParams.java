package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Request parameters for the account_currencies rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountCurrenciesRequestParams.class)
@JsonDeserialize(as = ImmutableAccountCurrenciesRequestParams.class)
public interface AccountCurrenciesRequestParams extends XrplRequestParams {

  static ImmutableAccountCurrenciesRequestParams.Builder builder() {
    return ImmutableAccountCurrenciesRequestParams.builder();
  }

  /**
   * A unique identifier for the account, most commonly the account's {@link Address}.
   *
   * @return The {@link Address} for the account.
   */
  Address account();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @Value.Default
  @JsonUnwrapped
  default LedgerSpecifier ledgerSpecifier() {
    return LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT);
  }

  /**
   * A boolean indicating if the {@link #account()} field only accepts a public key or XRP Ledger {@link Address}.
   * Always true, as {@link #account()} is always an {@link Address}.
   *
   * @return {@code true} if the account field only accepts a public key or XRP Ledger address, otherwise {@code false}.
   *   Defaults to {@code true}.
   */
  @Value.Derived
  default boolean strict() {
    return true;
  }
}
