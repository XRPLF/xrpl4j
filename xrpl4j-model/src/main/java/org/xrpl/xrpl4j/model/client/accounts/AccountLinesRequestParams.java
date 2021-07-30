package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.Optional;

/**
 * Request parameters for the "account_lines" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountLinesRequestParams.class)
@JsonDeserialize(as = ImmutableAccountLinesRequestParams.class)
public interface AccountLinesRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountLinesRequestParams.Builder}.
   */
  static ImmutableAccountLinesRequestParams.Builder builder() {
    return ImmutableAccountLinesRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account.
   *
   * @return The unique {@link Address} for the account.
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
   * The {@link Address} of a second account. If provided, show only lines of trust connecting the two accounts.
   *
   * @return An optionally-present {@link Address} denoting the peer's account.
   */
  Optional<Address> peer();

  /**
   * Limit the number of trust lines to retrieve. The server is not required to honor this value. Must be
   * within the inclusive range 10 to 400.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the response limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();

}
