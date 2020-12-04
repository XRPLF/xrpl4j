package org.xrpl.xrpl4j.client;

import com.google.common.collect.ImmutableList;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Feign error decoder for retrying requests based on HTTP status.
 */
public class RetryStatusDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();

  private final Duration retryInterval;
  private final List<Integer> retryableStatuses;

  /**
   * @param retryInterval       retry frequency.
   * @param retryHttpStatusCode http status code to retry
   * @param otherStatuses       additional http status codes to retry.
   */
  public RetryStatusDecoder(Duration retryInterval, Integer retryHttpStatusCode, Integer... otherStatuses) {
    this.retryableStatuses = ImmutableList.<Integer>builder().add(retryHttpStatusCode)
        .addAll(Arrays.asList(otherStatuses))
        .build();
    this.retryInterval = retryInterval;
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    Exception exception = defaultErrorDecoder.decode(methodKey, response);
    if (retryableStatuses.contains(response.status())) {
      JsonRpcClient.logger.error(String.format("##### Got %s response from %s #######", response.status(),
          methodKey));
      return new RetryableException(
          response.status(),
          exception.getMessage(),
          response.request().httpMethod(),
          Date.from(new Date().toInstant().plus(retryInterval)),
          response.request()
      );
    }
    return exception;
  }
}
