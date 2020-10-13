package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * Generic JSON RPC request object.
 */
@Immutable
@JsonSerialize(as = ImmutableJsonRpcRequest.class)
@JsonDeserialize(as = ImmutableJsonRpcRequest.class)
public interface JsonRpcRequest {

  static ImmutableJsonRpcRequest.Builder builder() {
    return ImmutableJsonRpcRequest.builder();
  }

  String method();

  List<JsonRpcRequestParam> params();

}
