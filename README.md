# xrpl4j: XRP Ledger Java SDK
[![codecov][codecov-image]][codecov-url]
[![issues][github-issues-image]][github-issues-url]
[![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-parent/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-parent)

A pure Java implementation of the core functionality necessary to interact with the XRP Ledger.  While this library does not provide a network client, it does support the difficult tasks of XRPL serialization and transaction signing, and provides useful Java bindings for XRP Ledger objects and rippled request/response objects.  

## Project Structure

Xrpl4j is structured as a Maven multi-module project, with the following modules:
- **xrpl4j-binary-codec**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-binary-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-binary-codec)
    - Serializes the JSON representation of XRPL Transactions to the canonical binary format of the XRP Ledger
- **xrpl4j-address-codec**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-address-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-address-codec)
    - Converts seeds, addresses, and public keys from their byte representations to the XRPL Base58Check encoding format, and vice versa
    - Handles X-Address encoding and decoding
- **xrpl4j-keypairs**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-keypairs/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-keypairs)
    - Generates seeds and derives XRPL key pairs, and can be used to sign transactions and verify transaction signatures
    - Supports both secp256k1 and ed25519 key types and signing algorithms
- **xrpl4j-model**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-model/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-model)
    - Provides Java objects which model XRP Ledger objects, as well as request parameters and response results for the rippled websocket and JSON RPC APIs
    - Also provides a Jackson `ObjectMapper` and JSON bindings which can be used to serialize and deserialize to and from JSON
- **xrpl4j-crypto**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-crypto-parent/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-crypto-parent)
    - **xrpl4j-crypto-core**: Provides core primitives like public/private keys definitions, signature interafaces and more.
    - **xrpl4j-crypto-bouncycastle**: An implementation using [BouncyCastle](https://www.bouncycastle.org/) as the underlying library/provider.
- **xrpl4j-client**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-client/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-client)
    - Provides an example rippled JSON RPC client which can be used to communicate with a rippled node
- **xrpl4j-integration-tests**: 
    - Contains all of the project's integration tests, which serve as valuable xrpl4j usage examples for common XRPL flows

## Documentation
- [Get Started Using Java](https://xrpl.org/get-started-using-java.html): a tutorial for building a very simple XRP Ledger-connected app.
- Example usage can be found in the [`xrpl4j-integration-tests` module](xrpl4j-integration-tests/).

## Requirements
- JDK 1.8 or higher
- A Java project manager such as Maven or Gradle

## Installation
You can use one or more xrpl4j modules in your Maven project by using the current [BOM](https://howtodoinjava.com/maven/maven-bom-bill-of-materials-dependency/) like this:

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.xrpl4j</groupId>
            <artifactId>xrpl4j-bom</artifactId>
            <version>2.3.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then you can add any of the xrpl4j modules found in the BOM to your `pom.xml`. For example, if you want to use the xrpl4j address codecs, add the following:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-address-codec</artifactId>
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

[codecov-image]: https://codecov.io/gh/XRPLF/xrpl4j/branch/main/graph/badge.svg
[codecov-url]: https://codecov.io/gh/XRPLF/xrpl4j
[github-issues-image]: https://img.shields.io/github/issues/XRPLF/xrpl4j.svg
[github-issues-url]: https://github.com/XRPLF/xrpl4j/issues
