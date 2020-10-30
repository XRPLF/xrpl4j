package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrplj4.client.model.JsonRpcResult;
import feign.Feign;
import feign.Headers;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.optionals.OptionalDecoder;
import java.util.Optional;
import okhttp3.HttpUrl;

import java.util.Objects;

/**
 * A feign HTTP client for interacting with the rippled JSON RPC API.
 */
public interface RippledClient {

  String HEADER_ACCEPT = "Accept";
  String HEADER_CONTENT_TYPE = "Content-Type";
  String APPLICATION_JSON = "application/json";

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  /**
   * Constructs a new client for the given url.
   *
   * @param rippledUrl url for the faucet server.
   * @return
   */
  static RippledClient construct(final HttpUrl rippledUrl) {
    Objects.requireNonNull(rippledUrl);

    return Feign.builder()
        .encoder(new JacksonEncoder(objectMapper))
        .decode404()
        .decoder(new OptionalDecoder(new JacksonDecoder(objectMapper)))
        .target(RippledClient.class, rippledUrl.toString());
  }

  /**
   * Request a new account to be created and funded by the test faucet.
   *
   * @return
   */
  @RequestLine("POST /")
  @Headers({
      HEADER_ACCEPT + ": " + APPLICATION_JSON,
      HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  JsonNode postRpcRequest(JsonRpcRequest rpcRequest);

  default <ResultType extends JsonRpcResult> ResultType send(
    JsonRpcRequest request,
    Class<ResultType> resultType
  ) throws RippledClientErrorException, JsonProcessingException {
    JavaType javaType = objectMapper.constructType(resultType);
    return send(request, javaType);
  }

  default <ResultType extends JsonRpcResult> ResultType send(
    JsonRpcRequest request,
    JavaType resultType
  ) throws RippledClientErrorException, JsonProcessingException {
    JsonNode response = postRpcRequest(request);
    JsonNode result = response.get("result");
    checkForError(response);
    return objectMapper.readValue(result.asText(), resultType);
  }

  default void checkForError(JsonNode response) throws RippledClientErrorException {
    if (response.has("result")) {
      JsonNode result = response.get("result");
      if (result.has("error")) {
        String errorMessage = Optional.ofNullable(result.get("error_exception"))
          .map(JsonNode::asText)
          .orElseGet(() -> result.get("error_message").asText());
        throw new RippledClientErrorException(errorMessage);
      }
    }
  }

}
