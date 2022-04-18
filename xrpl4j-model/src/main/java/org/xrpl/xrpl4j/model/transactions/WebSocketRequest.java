package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;

import java.util.List;

/**
 * Generic rippled WebSocket request object.
 */
@Value.Immutable
@JsonSerialize(
  as = ImmutableWebSocketRequest.class
)
@JsonDeserialize(as = ImmutableWebSocketRequest.class)
public interface WebSocketRequest {
  static ImmutableWebSocketRequest.Builder builder() {
    return ImmutableWebSocketRequest.builder();
  }

  /**
   * The name of the <a href="https://xrpl.org/public-rippled-methods.html">API method</a>.
   *
   * @return A {@link String} containing the method name.
   */
  String command();

  /**
   * A one-item {@link List} containing a {@link XrplRequestParams} with the parameters to this method.
   * You may omit this field if the method does not require any parameters.
   *
   * @return A {@link List} of {@link XrplRequestParams}.
   */
  @JsonUnwrapped
  XrplRequestParams params();

}
