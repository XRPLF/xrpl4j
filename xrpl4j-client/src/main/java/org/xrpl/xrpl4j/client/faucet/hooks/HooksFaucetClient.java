package org.xrpl.xrpl4j.client.faucet.hooks;


import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.optionals.OptionalDecoder;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.xrpl.xrpl4j.client.RetryStatusDecoder;
import org.xrpl.xrpl4j.client.faucet.FaucetAccountResponse;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.time.Duration;
import java.util.Objects;

public interface HooksFaucetClient {

  String HEADER_ACCEPT = "Accept";
  String HEADER_CONTENT_TYPE = "Content-Type";
  String APPLICATION_JSON = "application/json";
  int SERVICE_UNAVAILABLE_STATUS = 503;
  Duration RETRY_INTERVAL = Duration.ofSeconds(1);

  /**
   * Constructs a new client for the given url.
   *
   * @param faucetUrl url for the faucet server.
   *
   * @return A {@link FaucetClient}.
   */
  static HooksFaucetClient construct(final HttpUrl faucetUrl) {
    Objects.requireNonNull(faucetUrl);

    okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
      .addNetworkInterceptor(chain -> {
        final Request request = chain.request();
        final Request fooUserAgent = request.newBuilder()
          .header("User-Agent", request.header("User-Agent").replaceAll("\\.", ""))
          .build();
        return chain.proceed(fooUserAgent);
      })
      .build();

    final ObjectMapper objectMapper = ObjectMapperFactory.create();
    return Feign.builder()
      .encoder(new JacksonEncoder(objectMapper))
      .decode404()
      // rate limiting will return a 503 status that can be retried
      .errorDecoder(new RetryStatusDecoder(RETRY_INTERVAL, SERVICE_UNAVAILABLE_STATUS))
      .decoder(new OptionalDecoder(new JacksonDecoder(objectMapper)))
      .client(new OkHttpClient(okHttpClient))
      .target(HooksFaucetClient.class, faucetUrl.toString());
  }

  /**
   * Request a new account to be created and funded by the test faucet.
   *
   * @return A {@link FaucetAccountResponse} containing the faucet response fields.
   */
  @RequestLine("POST /newcreds")
  @Headers( {
    HEADER_ACCEPT + ": " + APPLICATION_JSON,
    HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  HooksFaucetAccountResponse generateAndFundAccount();

  @RequestLine("POST /newcreds?account={address}")
  @Headers( {
    HEADER_ACCEPT + ": */*",
    HEADER_CONTENT_TYPE + ": " + APPLICATION_JSON,
  })
  HooksFaucetAccountResponse fundAccount(@Param("address") Address address);

}
