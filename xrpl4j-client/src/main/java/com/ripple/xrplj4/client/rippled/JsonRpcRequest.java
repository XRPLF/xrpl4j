package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * Generic rippled JSON RPC request object.
 */
@Immutable
@JsonSerialize(as = ImmutableJsonRpcRequest.class)
@JsonDeserialize(as = ImmutableJsonRpcRequest.class)
public interface JsonRpcRequest {

  static ImmutableJsonRpcRequest.Builder builder() {
    return ImmutableJsonRpcRequest.builder();
  }

  /**
   * The name of the <a href="https://xrpl.org/public-rippled-methods.html">API method</a>.
   */
  String method();

  /**
   * A one-item {@link List} containing a {@link JsonRpcRequestParams} with the parameters to this method.
   * You may omit this field if the method does not require any parameters.
   */
  List<JsonRpcRequestParams> params();

}
