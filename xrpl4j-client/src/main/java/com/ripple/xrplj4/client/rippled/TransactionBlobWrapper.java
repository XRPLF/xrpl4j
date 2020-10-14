package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableTransactionBlobWrapper.class)
@JsonDeserialize(as = ImmutableTransactionBlobWrapper.class)
public interface TransactionBlobWrapper extends JsonRpcRequestParam<TransactionBlobWrapper> {

  static TransactionBlobWrapper of(String blobHex) {
    return ImmutableTransactionBlobWrapper.builder().txBlob(blobHex).build();
  }

  @JsonProperty("tx_blob")
  String txBlob();

}
