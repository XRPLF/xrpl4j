# xrpl4j: A 100% Java SDK for the XRP Ledger

[![codecov][codecov-image]][codecov-url]
[![issues][github-issues-image]][github-issues-url]
[![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-parent/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-parent)

This project is a pure Java implementation of an SDK that works with the XRP Ledger. This library supports the XRPL
transaction serialization and signing, provides useful Java bindings for XRP Ledger objects and rippled request/response
objects,
and also provides a JSON-RPC client for interacting with XRPL nodes.

## Documentation

- [Get Started Using Java](https://xrpl.org/get-started-using-java.html): a tutorial for building a very simple XRP
  Ledger-connected app.
- Example usage can be found in the [`xrpl4j-integration-tests` module](xrpl4j-integration-tests/).

## Usage

### Requirements

- JDK 1.8 or higher
- A Java project manager such as Maven or Gradle

### Installation

You can use one or more xrpl4j modules in your Maven project by using the
current [BOM](https://howtodoinjava.com/maven/maven-bom-bill-of-materials-dependency/) like this:

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

Then you can add any of the xrpl4j modules found in the BOM to your `pom.xml`. For example, if you want to use the
xrpl4j address codecs, add the following:

```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-address-codec</artifactId>
</dependency>
```

### Examples

Generate a new XRPL address and public/private keypair:

```java
  import org.xrpl.xrpl4j.crypto.core.keys.Seed;

...

  Seed seed=Seed.ed255519Seed(); // <-- Generates a random seed.
  PrivateKey privateKey=seed.derivePrivateKey(); // <-- Derive a private key from the seed.
  PublicKey publicKey=privateKey.derivePublicKey(); // <-- Derive a public key from the private key.
  Address address=publicKey.deriveAddress(); // <-- Derive an address from the public key.
```

Construct and Sign an XRP Payment:

```java
  import org.xrpl.xrpl4j.crypto.core.keys.Seed;
  import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
  import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
  import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
  import org.xrpl.xrpl4j.model.transactions.Address;
  import org.xrpl.xrpl4j.crypto.core.signing.SignatureService;
  import org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService;
  
  //...
  
  // Construct a SignatureService that uses in-memory Keys (see SignatureService.java for alternatives).
  SignatureService signatureService = new BcSignatureService();

  // Sender (using ed25519 key)
  Seed senderSeed = Seed.ed255519Seed();
  PrivateKey senderPrivateKey = senderSeed.derivePrivateKey();
  PublicKey senderPublicKey = senderPrivateKey.derivePublicKey();
  Address senderAddress = senderPublicKey.deriveAddress();

  // Receiver (using secp256k1 key)
  Address receiverAddress=Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");

  // Construct a Payment
  Payment payment=...; // See V3 ITs for examples.

  SingleSignedTransaction<Payment> signedTransaction = signatureService.sign(sourcePrivateKey,payment);
  SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
  assertThat(result.result()).isEqualTo("tesSUCCESS");
```

## Development

### Project Structure

Xrpl4j is structured as a Maven multi-module project, with the following modules:

- **xrpl4j-core
  **: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-binary-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-binary-codec)
    - Provides Java objects which model XRP Ledger objects, as well as request parameters and response results for the
      rippled websocket and JSON RPC APIs
    - Also provides a Jackson `ObjectMapper` and JSON bindings which can be used to serialize and deserialize to and
      from JSON
    - Serializes the JSON representation of XRPL Transactions to the canonical binary format of the XRP Ledger
- **xrpl4j-crypto
  **: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-address-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-address-codec)
    - **xrpl4j-crypto-core**: Provides core primitives like seeds, public/private keys definitions (supports secp256k1
      and ed25519 key types and signing algorithms), signature interfaces, etc.
    - **xrpl4j-crypto-bouncycastle**: An implementation using [BouncyCastle](https://www.bouncycastle.org/) as the
      underlying library/provider.
- **xrpl4j-client
  **: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-client/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-client)
    - Provides an example rippled JSON RPC client which can be used to communicate with a rippled node
- **xrpl4j-integration-tests**:
    - Contains the project's integration tests, which also serve as valuable xrpl4j usage examples for common XRPL
      flows.

You can build and test the entire project locally using maven from the command line:

```
mvn clean install
```

To build the project while skipping Integration tests, use the following command:

```
mvn clean install -DskipITs
```

To build the project while skipping Unit and Integration tests, use the following command:

```
mvn clean install -DskipITs -DskipTests
```

[codecov-image]: https://codecov.io/gh/XRPLF/xrpl4j/branch/main/graph/badge.svg

[codecov-url]: https://codecov.io/gh/XRPLF/xrpl4j

[github-issues-image]: https://img.shields.io/github/issues/XRPLF/xrpl4j.svg

[github-issues-url]: https://github.com/XRPLF/xrpl4j/issues
