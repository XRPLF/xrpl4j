package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Request parameters for the account_lines rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountLinesRequestParams.class)
@JsonDeserialize(as = ImmutableAccountLinesRequestParams.class)
public interface AccountLinesRequestParams extends XrplRequestParams {

  static ImmutableAccountLinesRequestParams.Builder builder() {
    return ImmutableAccountLinesRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account.
   */
  Address account();

  /**
   * A 20-byte hex string for the ledger version to use.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   *
   * <p>Defaults to "current".
   */
  @JsonProperty("ledger_index")
  @Value.Default
  default LedgerIndex ledgerIndex() {
    return LedgerIndex.CURRENT;
  }

  /**
   * The {@link Address} of a second account. If provided, show only lines of trust connecting the two accounts.
   */
  Optional<Address> peer();

  /**
   * Limit the number of trust lines to retrieve. The server is not required to honor this value. Must be
   * within the inclusive range 10 to 400.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   */
  Optional<String> marker();

}
