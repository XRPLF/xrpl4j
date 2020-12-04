# xrpl4j-client
An example library containing a rippled JSON RPC client. The client is meant to be used as a reference, but should not be used in actual applications.  
Users of xrpl4j should implement their own clients for their specific needs and preferences.

# Usage
The main client can be found in the [`XrplClient`](xrpl4j-client/src/main/java/org/xrpl/xrpl4j/client/XrplClient.java) class. 

The integration tests found in [xrpl4j-integration-tests](xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests) make use of this client to communicate with a rippled node over HTTP.

