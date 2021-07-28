package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;

/**
 * The result of an "account_lines" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountLinesResult.class)
@JsonDeserialize(as = ImmutableAccountLinesResult.class)
public interface AccountLinesResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountLinesResult.Builder}.
   */
  static ImmutableAccountLinesResult.Builder builder() {
    return ImmutableAccountLinesResult.builder();
  }

  /**
   * The unique {@link Address} for the account that made the request.
   *
   * @return The unique {@link Address} for the account that made the request.
   */
  Address account();

  /**
   * A {@link List} of {@link TrustLine}s.  If the number of {@link TrustLine}s for this account is large,
   * this will contain up to {@link AccountLinesRequestParams#limit()} entries.
   *
   * @return A {@link List} of {@link TrustLine}s for the requested account.
   */
  List<TrustLine> lines();

  /**
   * The ledger index of the current open ledger, which was used when retrieving this information.
   *
   * @return An optionally-present {@link LedgerIndex} representing the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * The ledger index of the ledger version that was used when retrieving this data.
   *
   * @return An optionally-present {@link LedgerIndex} representing the ledger index of the response.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * The identifying hash the ledger version that was used when retrieving this data.
   *
   * @return An optionally-present {@link org.xrpl.xrpl4j.model.transactions.Hash256}.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Server-defined value indicating the response is paginated. Pass this to the next call to resume where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String} containing a marker.
   */
  Optional<Marker> marker();

}
