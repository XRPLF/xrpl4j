package com.ripple.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParams;
import org.immutables.value.Value;

/**
 * Request parameters for the submit API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmissionRequestParams.class)
@JsonDeserialize(as = ImmutableSubmissionRequestParams.class)
public interface SubmissionRequestParams extends JsonRpcRequestParams {

  static SubmissionRequestParams of(String blobHex) {
    return ImmutableSubmissionRequestParams.builder().txBlob(blobHex).build();
  }

  /**
   * The hex encoded {@link String} containing a signed, binary encoded transaction.
   */
  @JsonProperty("tx_blob")
  String txBlob();

}
