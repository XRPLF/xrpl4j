package org.xrpl.xrpl4j.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Headers;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.optionals.OptionalDecoder;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.model.client.rippled.XrplResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * A feign HTTP client for interacting with the rippled JSON RPC API. This client is strictly responsible for
 * making network calls and deserializing responses. All higher order functionality such as signing and serialization
 * should be implemented in a wrapper class.
 */
public interface JsonRpcClient {

  Logger logger = LoggerFactory.getLogger(JsonRpcClient.class);

  String HEADER_ACCEPT = "Accept";
  String HEADER_CONTENT_TYPE = "Content-Type";
  String APPLICATION_JSON = "application/json";

  ObjectMapper objectMapper = ObjectMapperFactory.create();
  int SERVICE_UNAVAILABLE_STATUS = 503;
  Duration RETRY_INTERVAL = Duration.ofSeconds(1);

  /**
   * Constructs a new client for the given url.
   *
   * @param rippledUrl url for the faucet server.
   * @return A {@link JsonRpcClient} that can make request to {@code rippledUrl}
   */
  static JsonRpcClient construct(final HttpUrl rippledUrl) {
    Objects.requireNonNull(rippledUrl);

    return Feign.builder()
        .encoder(new JacksonEncoder(objectMapper))
        // rate limiting will return a 503 status that can be retried
        .errorDecoder(new RetryStatusDecoder(RETRY_INTERVAL, SERVICE_UNAVAILABLE_STATUS))
        .decode404()
        .decoder(new OptionalDecoder(new JacksonDecoder(objectMapper)))
        .target(JsonRpcClient.class, rippledUrl.toString());
  }

  /**
   * Send a POST request to the rippled server with {@code rpcRequest} in the request body.
   *
   * @param rpcRequest A rippled JSON RPC API request object.
   * @return A {@link JsonNode} which can be manually parsed containing the response.
   */
  @RequestLine("POST /")
  @Headers( {
      HEADER_ACCEPT + ": " + APPLICATION_JSON,
      HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  JsonNode postRpcRequest(JsonRpcRequest rpcRequest);

  /**
   * Send a given request to rippled.
   *
   * @param request      The {@link JsonRpcRequest} to send to the server.
   * @param resultType   The type of {@link XrplResult} that should be returned.
   * @param <ResultType> The extension of {@link XrplResult} corresponding to the request method.
   * @return The {@link ResultType} representing the result of the request.
   * @throws JsonRpcClientErrorException If rippled returns an error message, or if the response could not be
   *                                     deserialized to the provided {@link JsonRpcRequest} type.
   */
  default <ResultType extends XrplResult> ResultType send(
      JsonRpcRequest request,
      Class<ResultType> resultType
  ) throws JsonRpcClientErrorException {
    JavaType javaType = objectMapper.constructType(resultType);
    return send(request, javaType);
  }

  /**
   * Send a given request to rippled. Unlike {@link JsonRpcClient#send(JsonRpcRequest, Class)}, this
   * override requires a {@link JavaType} as the resultType, which can be useful when expecting a {@link XrplResult}
   * with type parameters. In this case, you can use an {@link ObjectMapper}'s {@link com.fasterxml.jackson.databind.type.TypeFactory}
   * to construct parameterized types.
   *
   * @param request      The {@link JsonRpcRequest} to send to the server.
   * @param resultType   The type of {@link XrplResult} that should be returned, converted to a {@link JavaType}.
   * @param <ResultType> The extension of {@link XrplResult} corresponding to the request method.
   * @return The {@link ResultType} representing the result of the request.
   * @throws JsonRpcClientErrorException If rippled returns an error message, or if the response could not be
   *                                     deserialized to the provided {@link JsonRpcRequest} type.
   */
  default <ResultType extends XrplResult> ResultType send(
      JsonRpcRequest request,
      JavaType resultType
  ) throws JsonRpcClientErrorException {
    JsonNode response = postRpcRequest(request);
    JsonNode result = response.get("result");
    checkForError(response);
    try {
      return objectMapper.readValue(result.toString(), resultType);
    } catch (JsonProcessingException e) {
      throw new JsonRpcClientErrorException(e);
    }
  }

  /**
   * Parse the response JSON to detect a possible error response message.
   *
   * @param response The {@link JsonNode} containing the JSON response from rippled.
   * @throws JsonRpcClientErrorException If rippled returns an error message.
   */
  default void checkForError(JsonNode response) throws JsonRpcClientErrorException {
    if (response.has("result")) {
      JsonNode result = response.get("result");
      if (result.has("error")) {
        String errorMessage = Optional.ofNullable(result.get("error_exception"))
            .map(JsonNode::asText)
            .orElseGet(() -> result.get("error_message").asText());
        throw new JsonRpcClientErrorException(errorMessage);
      }
    }
  }

}
