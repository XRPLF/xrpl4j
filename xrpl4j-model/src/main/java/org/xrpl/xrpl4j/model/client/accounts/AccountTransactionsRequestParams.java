package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.LegacyLedgerSpecifierUtils;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.jackson.modules.AccountTransactionsRequestParamsDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Request parameters for the account_tx rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsRequestParams.class)
@JsonDeserialize(
  as = ImmutableAccountTransactionsRequestParams.class,
  using = AccountTransactionsRequestParamsDeserializer.class
)
public interface AccountTransactionsRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountTransactionsRequestParams.Builder}.
   */
  static ImmutableAccountTransactionsRequestParams.Builder builder() {
    return ImmutableAccountTransactionsRequestParams.builder();
  }

  /**
   * A unique {@link Address} for the account.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * The earliest ledger to include transactions from. A value of {@code -1} instructs the server to use the
   * earliest validated ledger version available.
   *
   * @return A {@link LedgerIndex} with a default of empty.
   * @deprecated ledger_index_min field should be specified by {@link #ledgerIndexMinimum()}
   */
  @JsonIgnore
  @Deprecated
  @Value.Auxiliary
  Optional<LedgerIndex> ledgerIndexMin();

  /**
   * The most recent ledger to include transactions from. A value of {@code -1} instructs the server to use the most
   * recent validated ledger version available.
   *
   * @return A {@link LedgerIndex} with a default of empty.
   * @deprecated ledger_index_max field should be specified by {@link #ledgerIndexMaximum()}.
   */
  @JsonIgnore
  @Deprecated
  @Value.Auxiliary
  Optional<LedgerIndex> ledgerIndexMax();

  /**
   * The earliest ledger to include transactions from. A value of {@code -1} instructs the server to use the
   * earliest validated ledger version available.
   *
   * @return A {@link LedgerIndexBound} with a default of empty.
   */
  @JsonProperty("ledger_index_min")
  @Value.Default
  @Nullable // Value.Default on Optional attributes takes away the non-optional builder method
  default LedgerIndexBound ledgerIndexMinimum() {
    // Gives deprecated field precedence
    return ledgerIndexMin()
      .map(LedgerIndex::unsignedIntegerValue)
      .map(UnsignedInteger::intValue)
      .map(LedgerIndexBound::of)
      .orElse(LedgerIndexBound.of(-1));
  }

  /**
   * The most recent ledger to include transactions from. A value of {@code -1} instructs the server to use the most
   * recent validated ledger version available.
   *
   * @return A {@link LedgerIndexBound} with a default of empty.
   */
  @JsonProperty("ledger_index_max")
  @Value.Default
  @Nullable // Value.Default on Optional attributes takes away the non-optional builder method
  default LedgerIndexBound ledgerIndexMaximum() {
    // Gives deprecated field precedence
    return ledgerIndexMax()
      .map(LedgerIndex::unsignedIntegerValue)
      .map(UnsignedInteger::intValue)
      .map(LedgerIndexBound::of)
      .orElse(LedgerIndexBound.of(-1));
  }

  /**
   * Return transactions from the ledger with this hash only.
   *
   * @return An optionally-present {@link Hash256} containing the ledger hash.
   * @deprecated Ledger hash should be specified in {@link #ledgerSpecifier()}.
   */
  @JsonIgnore
  @Deprecated
  @Value.Auxiliary
  Optional<Hash256> ledgerHash();

  /**
   * Return transactions from the ledger with this index only.
   *
   * @return A {@link LedgerIndex} containing the ledger index, defaults to "current".
   * @deprecated Ledger index and any shortcut values should be specified in {@link #ledgerSpecifier()}.
   */
  @JsonIgnore
  @Deprecated
  @Value.Auxiliary
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * <p>The only valid ledger index shortcut for this request object is
   * {@link org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut#VALIDATED}.</p>
   *
   * <p>Setting this value will nullify and take precedence over {@link #ledgerIndexMinimum()}
   * and {@link #ledgerIndexMaximum()}</p>
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  @Value.Default // TODO: Make non-default once ledgerIndex and ledgerHash are gone
  default Optional<LedgerSpecifier> ledgerSpecifier() {
    // If either ledgerHash or ledgerIndex are specified, return a LedgerSpecifier with the present field,
    // otherwise return empty
    return ledgerHash()
      .map(LedgerSpecifier::ledgerHash)
      .map(Optional::of)
      .orElseGet(() -> ledgerIndex().map(LegacyLedgerSpecifierUtils::computeLedgerSpecifierFromLedgerIndex));
  }

  /**
   * Whether or not to return transactions as JSON or binary-encoded hex strings. Always {@code false}.
   *
   * @return Always {@code false}.
   */
  @Value.Derived
  default boolean binary() {
    return false;
  }

  /**
   * If set to {@code true}, returns values indexed with the oldest ledger first. Otherwise, the results are indexed
   * with the newest ledger first. (Each page of results may not be internally ordered, but the pages are overall
   * ordered.)
   *
   * @return {@code true} if values should be indexed with the oldest ledger first, otherwise {@code false}. Defaults
   *   to {@code false}.
   */
  @Value.Default
  default boolean forward() {
    return false;
  }

  /**
   * Limit the number of transactions to retrieve. The server is not required to honor this value.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the number of transactions to return.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   * This value is stable even if there is a change in the server's range of available ledgers.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();

  /**
   * Validates that if {@link LedgerSpecifier#ledgerIndexShortcut()} is present, its value is
   * {@link LedgerIndexShortcut#VALIDATED}.
   */
  @Value.Check
  default void validateSpecifierNotCurrentOrClosed() {
    ledgerSpecifier().ifPresent(
      ledgerSpecifier -> ledgerSpecifier.handle(
        ledgerHash -> {
        },
        ledgerIndex -> {
        },
        ledgerIndexShortcut -> Preconditions.checkArgument(
          ledgerIndexShortcut.equals(LedgerIndexShortcut.VALIDATED),
          "Invalid LedgerIndexShortcut. The account_tx API method only accepts 'validated' when specifying a shortcut."
        )
      )
    );
  }

  /**
   * Nullifies {@link #ledgerIndexMinimum()} and {@link #ledgerIndexMaximum()} if {@link #ledgerSpecifier()} is present.
   *
   * @return An {@link AccountTransactionsRequestParams}.
   */
  @Value.Check
  default AccountTransactionsRequestParams emptyBoundedParametersIfSpecifierPresent() {
    // If user included a ledgerSpecifier, this will blank out ledgerIndexMin and ledgerIndexMax
    // so that they do not override the ledgerSpecifier.
    if (ledgerSpecifier().isPresent() && (ledgerIndexMinimum() != null || ledgerIndexMaximum() != null)) {
      return AccountTransactionsRequestParams.builder()
        .from(this)
        .ledgerIndexMinimum(null)
        .ledgerIndexMaximum(null)
        .build();
    } else {
      return this;
    }
  }
}
