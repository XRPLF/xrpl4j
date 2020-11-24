package com.ripple.xrpl4j.client.model.channels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import org.immutables.value.Value;

/**
 * The result of a channel_verify request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableChannelVerifyResult.class)
@JsonDeserialize(as = ImmutableChannelVerifyResult.class)
public interface ChannelVerifyResult extends JsonRpcResult {

  /**
   * If true, the signature is valid for the stated amount, channel, and public key.
   */
  @JsonProperty("signature_verified")
  boolean signatureVerified();

}
