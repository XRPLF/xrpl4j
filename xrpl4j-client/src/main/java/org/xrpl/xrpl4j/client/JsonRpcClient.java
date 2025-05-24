package org.xrpl.xrpl4j.client;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: client
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import feign.Feign;
import feign.Headers;
import feign.Request.Options;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.optionals.OptionalDecoder;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * A feign HTTP client for interacting with the rippled JSON RPC API. This client is strictly responsible for making
 * network calls and deserializing responses. All higher order functionality such as signing and serialization should be
 * implemented in a wrapper class.
 *
 * <p>Note: This client is currently marked as {@link Beta}, and should be used as a reference implementation ONLY.
 */
@Beta
public interface JsonRpcClient {

  Logger logger = LoggerFactory.getLogger(JsonRpcClient.class);

  String HEADER_ACCEPT = "Accept";
  String HEADER_CONTENT_TYPE = "Content-Type";
  String APPLICATION_JSON = "application/json";

  ObjectMapper objectMapper = ObjectMapperFactory.create();
  int SERVICE_UNAVAILABLE_STATUS = 503;
  Duration RETRY_INTERVAL = Duration.ofSeconds(1);

  String RESULT = "result";
  String STATUS = "status";
  String ERROR = "error";
  String ERROR_EXCEPTION = "error_exception";
  String ERROR_MESSAGE = "error_message";
  String N_A = "n/a";

  /**
   * Constructs a new client for the given url.
   *
   * @param rippledUrl The {@link HttpUrl} of the node to connect to.
   *
   * @return A {@link JsonRpcClient} that can make request to {@code rippledUrl}
   */
  static JsonRpcClient construct(final HttpUrl rippledUrl) {
    Objects.requireNonNull(rippledUrl);

    return construct(rippledUrl, new Options());
  }

  /**
   * Constructs a new client for the given url with the given client options.
   *
   * @param rippledUrl The {@link HttpUrl} of the node to connect to.
   * @param options    An {@link Options}.
   *
   * @return A {@link JsonRpcClient}.
   */
  static JsonRpcClient construct(HttpUrl rippledUrl, Options options) {
    Objects.requireNonNull(rippledUrl);
    Objects.requireNonNull(options);

    return Feign.builder()
      .encoder(new JacksonEncoder(objectMapper))
      // rate limiting will return a 503 status that can be retried
      .errorDecoder(new RetryStatusDecoder(RETRY_INTERVAL, SERVICE_UNAVAILABLE_STATUS))
      .decode404()
      .options(options)
      .decoder(new OptionalDecoder(new JacksonDecoder(objectMapper)))
      .target(JsonRpcClient.class, rippledUrl.toString());
  }

  /**
   * Send a POST request to the rippled server with {@code rpcRequest} in the request body.
   *
   * @param rpcRequest A rippled JSON RPC API request object.
   *
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
   * @param request    The {@link JsonRpcRequest} to send to the server.
   * @param resultType The type of {@link XrplResult} that should be returned.
   * @param <T>        The extension of {@link XrplResult} corresponding to the request method.
   *
   * @return The {@link T} representing the result of the request.
   *
   * @throws JsonRpcClientErrorException If rippled returns an error message, or if the response could not be
   *                                     deserialized to the provided {@link JsonRpcRequest} type.
   */
  default <T extends XrplResult> T send(
    JsonRpcRequest request,
    Class<T> resultType
  ) throws JsonRpcClientErrorException {
    JavaType javaType = objectMapper.constructType(resultType);
    return send(request, javaType);
  }

  /**
   * Send a given request to rippled. Unlike {@link JsonRpcClient#send(JsonRpcRequest, Class)}, this override requires a
   * {@link JavaType} as the resultType, which can be useful when expecting a {@link XrplResult} with type parameters.
   * In this case, you can use an {@link ObjectMapper}'s {@link com.fasterxml.jackson.databind.type.TypeFactory} to
   * construct parameterized types.
   *
   * @param request    The {@link JsonRpcRequest} to send to the server.
   * @param resultType The type of {@link XrplResult} that should be returned, converted to a {@link JavaType}.
   * @param <T>        The extension of {@link XrplResult} corresponding to the request method.
   *
   * @return The {@link T} representing the result of the request.
   *
   * @throws JsonRpcClientErrorException If rippled returns an error message, or if the response could not be
   *                                     deserialized to the provided {@link JsonRpcRequest} type.
   */
  default <T extends XrplResult> T send(
    JsonRpcRequest request,
    JavaType resultType
  ) throws JsonRpcClientErrorException {
    JsonNode response = postRpcRequest(request);
    JsonNode result = response.get(RESULT);
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
   *
   * @throws JsonRpcClientErrorException If rippled returns an error message.
   */
  default void checkForError(JsonNode response) throws JsonRpcClientErrorException {
    if (response.has(RESULT)) {
      JsonNode result = response.get(RESULT);
      if (result.has(STATUS)) {
        String status = result.get(STATUS).asText();
        if (status.equals(ERROR)) { // <-- Only an error if result.status == "error"
          if (result.has(ERROR)) {
            String errorCode = result.get(ERROR).asText();

            final String errorMessage;
            if (result.hasNonNull(ERROR_EXCEPTION)) {
              errorMessage = result.get(ERROR_EXCEPTION).asText();
            } else if (result.hasNonNull(ERROR_MESSAGE)) {
              errorMessage = result.get(ERROR_MESSAGE).asText();
            } else {
              errorMessage = N_A;
            }
            throw new JsonRpcClientErrorException(String.format("%s (%s)", errorCode, errorMessage));
          }
        }
      }
    }
  }

}
