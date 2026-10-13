# xrpl4j-client

An example library containing a rippled JSON RPC client. The client is meant to be used as a reference implementation. 
While suitable for production usage, it is currently marked `@Beta` and its API should be considered unstable.

# Usage

The main client can be found in the [`XrplClient`](./src/main/java/org/xrpl/xrpl4j/client/XrplClient.java) class.

In addition, the integration tests found in [xrpl4j-integration-tests](../xrpl4j-integration-tests) make use of this client to
communicate with a rippled node over HTTP.

## Using a pooled HTTP client

By default `XrplClient` sends requests with Feign's built-in `HttpURLConnection` client, which
has no connection-pool configuration. To use a pooled client, pass any Feign `Client` to the
`XrplClient(HttpUrl, Client, Options)` constructor. For example, add `feign-okhttp` at the same
Feign version that `xrpl4j-client` uses, then:

```java
okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
  .connectionPool(new okhttp3.ConnectionPool(32, 30, java.util.concurrent.TimeUnit.SECONDS))
  .build();

XrplClient xrplClient = new XrplClient(
  okhttp3.HttpUrl.get("https://s1.ripple.com:51234/"),
  new feign.okhttp.OkHttpClient(okHttpClient),
  new feign.Request.Options(
    5, java.util.concurrent.TimeUnit.SECONDS,
    30, java.util.concurrent.TimeUnit.SECONDS,
    true
  )
);
```

Classes are fully qualified above because `okhttp3.OkHttpClient` and `feign.okhttp.OkHttpClient` share a simple name;
in real code you would typically import one of the two and fully qualify the other at its point of use.

