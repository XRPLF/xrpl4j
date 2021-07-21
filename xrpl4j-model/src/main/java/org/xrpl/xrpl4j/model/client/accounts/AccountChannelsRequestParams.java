package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.jackson.modules.MarkerDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Request parameters for the account_channels rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountChannelsRequestParams.class)
@JsonDeserialize(as = ImmutableAccountChannelsRequestParams.class)
public interface AccountChannelsRequestParams extends XrplRequestParams {

  static ImmutableAccountChannelsRequestParams.Builder builder() {
    return ImmutableAccountChannelsRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account. The request returns channels where this account is the channel's
   * owner/source.
   *
   * @return The {@link Address} for the account.
   */
  Address account();

  /**
   * The unique {@link Address} for the destination account. If provided, the response results are filtered
   * by channels whose destination is this account.
   *
   * @return The optionally present {@link Address} of the destination account.
   */
  @JsonProperty("destination_account")
  Optional<Address> destinationAccount();

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
   * Limit the number of transactions to retrieve. Cannot be less than 10 or more than 400. The default is 200.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String}.
   */
  Optional<Marker> marker();
}
