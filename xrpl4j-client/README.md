# xrpl4j-client

An example library containing a rippled JSON RPC client. The client is meant to be used as a reference implementation. 
While suitable for production usage, it is currently marked `@Beta` and its API should be considered unstable.

# Usage

The main client can be found in the [`XrplClient`](./src/main/java/org/xrpl/xrpl4j/client/XrplClient.java) class.

In addition, the integration tests found in [xrpl4j-integration-tests](../xrpl4j-integration-tests) make use of this client to
communicate with a rippled node over HTTP.

