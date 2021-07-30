package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.modules.MarkerDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;

/**
 * The result of an account_channels rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountChannelsResult.class)
@JsonDeserialize(as = ImmutableAccountChannelsResult.class)
public interface AccountChannelsResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountChannelsResult.Builder}.
   */
  static ImmutableAccountChannelsResult.Builder builder() {
    return ImmutableAccountChannelsResult.builder();
  }

  /**
   * The {@link Address} of the source/owner of the {@link #channels()}. This corresponds to the
   * {@link AccountChannelsRequestParams#account()} field of the request.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * Payment channels owned by this {@link #account()}.
   *
   * @return A {@link List} of {@link PaymentChannelResultObject}s.
   */
  List<PaymentChannelResultObject> channels();

  /**
   * The identifying Hash of the ledger version used to generate this response.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Hash256 ledgerHash();

  /**
   * The Ledger Index of the ledger version used to generate this response.
   *
   * @return A {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  /**
   * If true, the information in this response comes from a validated ledger version.
   * Otherwise, the information is subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, {@code false}
   *   if not.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * The limit to how many {@link #channels()} were actually returned by this request.
   *
   * @return An optionally-present {@link UnsignedInteger} containing the request limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Server-defined value for pagination. Pass this to the next call to resume getting results where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String} containing the response marker.
   */
  Optional<Marker> marker();

}
