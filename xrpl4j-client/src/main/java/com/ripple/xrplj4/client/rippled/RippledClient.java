package com.ripple.xrplj4.client.rippled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.jackson.ObjectMapperFactory;
import feign.Feign;
import feign.Headers;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.optionals.OptionalDecoder;
import okhttp3.HttpUrl;

import java.util.Objects;

/**
 * A feign HTTP client for interacting with the Testnet Faucet REST API.
 */
public interface RippledClient {

  String HEADER_ACCEPT = "Accept";
  String HEADER_CONTENT_TYPE = "Content-Type";
  String APPLICATION_JSON = "application/json";

  static final ObjectMapper objectMapper = ObjectMapperFactory.create();

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
  @Headers( {
      HEADER_ACCEPT + ": " + APPLICATION_JSON,
      HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  JsonRpcResponse postRpcRequest(JsonRpcRequest rpcRequest);

  default <T> T sendRequest(JsonRpcRequest request, Class<T> responseClass) throws RippledClientErrorException {
    JsonRpcResponse response = postRpcRequest(request);
    if (response.result().has("error")) {
      throw new RippledClientErrorException(response.result().get("error_exception").asText());
    }
    return objectMapper.convertValue(response.result(), responseClass);
  }

}
