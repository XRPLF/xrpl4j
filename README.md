# xrpl4j

A pure Java implementation of the core functionality necessary to interact with the XRP Ledger.  While this library does not provide a network client, it does support the difficult tasks of XRPL serialization and transaction signing, and provides useful Java bindings for XRP Ledger objects and rippled request/response objects.  

## Project Structure

Xrpl4j is structured as a Maven multi-module project, with the following modules:
- **xrpl4j-binary-codec**:
    - Serializes the JSON representation of XRPL Transactions to the canonical binary format of the XRP Ledger
- **xrpl4j-address-codec**:
    - Converts seeds, addresses, and public keys from their byte representations to the XRPL Base58Check encoding format, and vice versa
    - Handles X-Address encoding and decoding
- **xrpl4j-keypairs**:
    - Generates seeds and derives XRPL key pairs, and can be used to sign transactions and verify transaction signatures
    - Supports both secp256k1 and ed25519 key types and signing algorithms
- **xrpl4j-model**:
    - Provides Java objects which model XRP Ledger objects, as well as request parameters and response results for the rippled websocket and JSON RPC APIs
    - Also provides a Jackson `ObjectMapper` and JSON bindings which can be used to serialize and deserialize to and from JSON
- **xrpl4j-client** (**_NOT SUPPORTED_**)
    - Provides an example rippled JSON RPC client which can be used to communicate with a rippled node
- xrpl4j-integration-tests
    - Contains all of the project's integration tests, which serve as valuable xrpl4j usage examples for common XRPL flows

## Documentation
- Example usage can be found in the xrpl4j-integration-tests module
- Javadocs
TODO: Link to Javadocs and ITs

## Requirements
- JDK 1.8 or higher
- A Java project manager such as Maven or Gradle

## Installation
You can use one or more xrpl4j modules in your Maven project by adding the following to your `pom.xml` for each module (remember to replace `{module-name}`!):
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-{module-name}</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Development
You can build and install the project locally using maven from the command line:
```
mvn clean install -DskipTests
```

To run unit tests, use the following command:
```
mvn clean install -DskipITs
```

To run the integration tests, you can either run
```
mvn clean install
```
which will run both the unit tests and integration tests, or to run only the integration tests, you can run the following commands:
```
cd xrpl4j-integration-tests
mvn clean install
```

