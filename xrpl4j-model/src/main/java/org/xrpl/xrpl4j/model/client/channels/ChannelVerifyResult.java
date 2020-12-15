package org.xrpl.xrpl4j.model.client.channels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;

/**
 * The result of a channel_verify request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableChannelVerifyResult.class)
@JsonDeserialize(as = ImmutableChannelVerifyResult.class)
public interface ChannelVerifyResult extends XrplResult {

  static ImmutableChannelVerifyResult.Builder builder() {
    return ImmutableChannelVerifyResult.builder();
  }
  /**
   * If true, the signature is valid for the stated amount, channel, and public key.
   */
  @JsonProperty("signature_verified")
  boolean signatureVerified();

}
