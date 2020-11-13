package com.ripple.xrpl4j.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTrustLine.class)
@JsonDeserialize(as = ImmutableTrustLine.class)
public interface TrustLine {

  Address account();

  String balance();

  String currency();

  String limit();

  @JsonProperty("limit_peer")
  String limitPeer();

  @JsonProperty("quality_in")
  UnsignedInteger qualityIn();

  @JsonProperty("quality_out")
  UnsignedInteger qualityOut();

  @JsonProperty("no_ripple")
  @Value.Default
  default boolean noRipple() {
    return false;
  }

  @JsonProperty("no_ripple_peer")
  @Value.Default
  default boolean noRipplePeer() {
    return false;
  }

  @Value.Default
  default boolean authorized() {
    return false;
  }

  @JsonProperty("peer_authorized")
  @Value.Default
  default boolean peerAuthorized() {
    return false;
  }

  @Value.Default
  default boolean freeze() {
    return false;
  }

  @JsonProperty("freeze_peer")
  @Value.Default
  default boolean freezePeer() {
    return false;
  }

}
