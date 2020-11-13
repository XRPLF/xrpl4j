package com.ripple.xrpl4j.client.model.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Payment;
import org.immutables.value.Value;

import java.util.List;

/**
 * The result of a ripple_path_find rippled method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRipplePathFindResult.class)
@JsonDeserialize(as = ImmutableRipplePathFindResult.class)
public interface RipplePathFindResult extends JsonRpcResult {

  /**
   * A {@link List<PathAlternative>} with possible paths to take. If empty, then are are no paths connecting the
   * source and destination accounts.
   */
  List<PathAlternative> alternatives();

  /**
   * Unique {@link Address} of the account that would receive a {@link Payment} transaction.
   */
  @JsonProperty("destination_account")
  Address destinationAccount();

  /**
   * {@link List<String>} representing the currencies that the {@link #destinationAccount()} accepts,
   * as 3-letter codes like "USD" or as 40-character hex like "015841551A748AD2C1F76FF6ECB0CCCD00000000";
   */
  @JsonProperty("destination_currencies")
  List<String> destinationCurrencies();

}
