package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrplj4.client.model.JsonRpcResult;
import org.immutables.value.Value.Immutable;

/**
 * Generic JSON RPC response object.
 */
@Immutable
@JsonSerialize(as = ImmutableJsonRpcResponse.class)
@JsonDeserialize(as = ImmutableJsonRpcResponse.class)
public interface JsonRpcResponse<ResultType extends JsonRpcResult> {

  ResultType result();

}
