package com.ripple.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

/**
 * Represents a TrustLine between two accounts on the XRPL. This representation is only present in responses
 * to account_lines rippled method calls.
 *
 * The values in this object are from the perspective of the requesting account.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTrustLine.class)
@JsonDeserialize(as = ImmutableTrustLine.class)
public interface TrustLine {

  /**
   * The unique {@link Address} of the counterparty to this trust line.
   */
  Address account();

  /**
   * Representation of the numeric balance currently held against this line. A positive balance means that
   * the perspective account holds value; a negative balance means that the perspective account owes value.
   */
  String balance();

  /**
   * A Currency Code identifying what currency this trust line can hold.
   */
  String currency();

  /**
   * The maximum amount of the given currency that this account is willing to owe the peer account.
   */
  String limit();

  /**
   * The maximum amount of currency that the counterparty account is willing to owe the perspective account.
   */
  @JsonProperty("limit_peer")
  String limitPeer();

  /**
   * Rate at which the account values incoming balances on this trust line, as a ratio of this value per 1
   * billion units. (For example, a value of 500 million represents a 0.5:1 ratio.)
   *
   * As a special case, 0 is treated as a 1:1 ratio.
   */
  @JsonProperty("quality_in")
  UnsignedInteger qualityIn();

  /**
   * Rate at which the account values outgoing balances on this trust line, as a ratio of this value per 1
   * billion units. (For example, a value of 500 million represents a 0.5:1 ratio.)
   *
   * As a special case, 0 is treated as a 1:1 ratio.
   */
  @JsonProperty("quality_out")
  UnsignedInteger qualityOut();

  /**
   * Whether or not this account has enabled the lsfNoRipple flag for this line.
   */
  @JsonProperty("no_ripple")
  @Value.Default
  default boolean noRipple() {
    return false;
  }

  /**
   * Whether or not the peer account has enabled the lsfNoRipple flag for this line.
   */
  @JsonProperty("no_ripple_peer")
  @Value.Default
  default boolean noRipplePeer() {
    return false;
  }

  /**
   * Whether or not this account has authorized this trust line.
   */
  @Value.Default
  default boolean authorized() {
    return false;
  }

  /**
   * Whether or not the peer account has authorized this trust line.
   */
  @JsonProperty("peer_authorized")
  @Value.Default
  default boolean peerAuthorized() {
    return false;
  }

  /**
   * Whether or not this account has frozen this trust line.
   */
  @Value.Default
  default boolean freeze() {
    return false;
  }

  /**
   * Whether or not the peer account has frozen this trust line.
   */
  @JsonProperty("freeze_peer")
  @Value.Default
  default boolean freezePeer() {
    return false;
  }

}
