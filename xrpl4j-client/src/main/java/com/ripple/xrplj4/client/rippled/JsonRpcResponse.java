package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableJsonRpcResponse.class)
@JsonDeserialize(as = ImmutableJsonRpcResponse.class)
public interface JsonRpcResponse {

  JsonNode result();

}
