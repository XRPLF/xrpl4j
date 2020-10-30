package com.ripple.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParam;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSubmissionRequestParams.class)
@JsonDeserialize(as = ImmutableSubmissionRequestParams.class)
public interface SubmissionRequestParams extends JsonRpcRequestParam {

  static SubmissionRequestParams of(String blobHex) {
    return ImmutableSubmissionRequestParams.builder().txBlob(blobHex).build();
  }

  @JsonProperty("tx_blob")
  String txBlob();

}
