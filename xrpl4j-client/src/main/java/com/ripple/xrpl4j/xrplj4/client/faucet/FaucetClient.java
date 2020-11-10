package com.ripple.xrpl4j.xrplj4.client.faucet;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public interface FaucetClient {

  String HEADER_ACCEPT = "Accept";
  String HEADER_CONTENT_TYPE = "Content-Type";
  String APPLICATION_JSON = "application/json";

  /**
   * Constructs a new client for the given url.
   *
   * @param faucetUrl url for the faucet server.
   * @return
   */
  static FaucetClient construct(final HttpUrl faucetUrl) {
    Objects.requireNonNull(faucetUrl);

    final ObjectMapper objectMapper = new ObjectMapper();
    return Feign.builder()
        .encoder(new JacksonEncoder(objectMapper))
        .decode404()
        .decoder(new OptionalDecoder(new JacksonDecoder(objectMapper)))
        .target(FaucetClient.class, faucetUrl.toString());
  }

  /**
   * Request a new account to be created and funded by the test faucet.
   *
   * @return
   */
  @RequestLine("POST /accounts")
  @Headers( {
      HEADER_ACCEPT + ": " + APPLICATION_JSON,
      HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  ImmutableFaucetAccountResponse generateFaucetAccount();

  /**
   * Request a new account to be created and funded by the test faucet.
   *
   * @return
   */
  @RequestLine("POST /accounts")
  @Headers( {
      HEADER_ACCEPT + ": " + APPLICATION_JSON,
      HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  ImmutableFaucetAccountResponse fundAccount(FundAccountRequest request);

}
