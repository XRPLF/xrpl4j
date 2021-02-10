package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;

/**
 * Represents the result of an "account_objects" rippled API call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountObjectsResult.class)
@JsonDeserialize(as = ImmutableAccountObjectsResult.class)
public interface AccountObjectsResult extends XrplResult {

  static ImmutableAccountObjectsResult.Builder builder() {
    return ImmutableAccountObjectsResult.builder();
  }

  /**
   * The unique {@link Address} for the account that made the request.
   *
   * @return The unique {@link Address} for the account that made the request.
   */
  Address account();

  /**
   * {@link List} of {@link LedgerObject}s owned by {@link AccountObjectsResult#account()}.
   * Each object is in its raw <a href="https://xrpl.org/ledger-data-formats.html">ledger format</a>.
   *
   * @return A {@link List} of {@link LedgerObject}s.
   */
  @JsonProperty("account_objects")
  List<LedgerObject> accountObjects();

  /**
   * The identifying hash of the ledger that was used to generate this {@link AccountObjectsResult}.
   *
   * @return An optionally-present {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger version that was used to generate this {@link AccountObjectsResult}.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * The ledger index of the current in-progress ledger version, which was used to generate this
   * {@link AccountObjectsResult}.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * The {@link AccountObjectsRequestParams#limit()} that was used in the corresponding request, if any.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Server-defined value indicating the response is paginated. Pass this to the next call to resume where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String}.
   */
  Optional<Marker> marker();

  /**
   * If included and set to true, the information in this response comes from a validated ledger version.
   * Otherwise, the information is subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, otherwise
   *     {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

}
