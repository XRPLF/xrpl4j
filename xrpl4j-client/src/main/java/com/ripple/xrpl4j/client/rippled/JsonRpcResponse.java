package com.ripple.xrpl4j.client.rippled;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import org.immutables.value.Value.Immutable;

/**
 * Generic JSON RPC response object.
 * @param <ResultType> The type of {@link JsonRpcResponse#result()}, which varies based on API method.
 */
@Immutable
@JsonSerialize(as = ImmutableJsonRpcResponse.class)
@JsonDeserialize(as = ImmutableJsonRpcResponse.class)
public interface JsonRpcResponse<ResultType extends JsonRpcResult> {

  /**
   * The result of a request to the rippled JSON RPC API. Contents vary depending on the API method.
   */
  ResultType result();

}
