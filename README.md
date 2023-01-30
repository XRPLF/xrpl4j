# xrpl4j: A 100% Java SDK for the XRP Ledger

[![codecov][codecov-image]][codecov-url]
[![issues][github-issues-image]][github-issues-url]
[![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-parent/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-parent)

This project is a pure Java implementation of an SDK that works with the XRP Ledger. This library supports XRPL
transaction serialization and signing, provides useful Java bindings for XRP Ledger objects and rippled request/response
objects, and also provides a JSON-RPC client for interacting with XRPL nodes.

## Documentation

- [Get Started Using Java](https://xrpl.org/get-started-using-java.html): a tutorial for building a very simple XRP
  Ledger-connected app.
- Example usage can be found in the `xrpl4j-integration-tests`
  module [here](https://github.com/XRPLF/xrpl4j/tree/main/xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests).

## Usage

### Requirements

- JDK 1.8 or higher
- A Java project manager such as Maven or Gradle

### Maven Installation

You can use one or more xrpl4j modules in your Maven project by using the
current [BOM](https://howtodoinjava.com/maven/maven-bom-bill-of-materials-dependency/) like this:

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.xrpl4j</groupId>
            <artifactId>xrpl4j-bom</artifactId>
            <version>3.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then you can add one or both of the `xrpl4j-core` and `xrpl4j-client` modules
found in the BOM to your `pom.xml`. For example:

```
<dependencies>  
  ...
  <dependency>
    <groupId>org.xrpl</groupId>
    <artifactId>xrpl4j-core</artifactId>
  </dependency>
  <dependency>
    <groupId>org.xrpl</groupId>
    <artifactId>xrpl4j-client</artifactId>
  </dependency>
  ...
</dependencies>
```

### Examples

#### Core Objects

This library provides Java objects modeling [XRP Ledger Objects](https://xrpl.org/ledger-data-formats.html),
[Transactions](https://xrpl.org/transaction-formats.html),
and [request parameters](https://xrpl.org/request-formatting.html)/[response results](https://xrpl.org/response-formatting.html)
for the [rippled API](https://xrpl.org/public-rippled-methods.html).

The objects in this module are annotated with `@Value.Immutable` from
the [immutables library](https://immutables.github.io/), which generates immutable implementations with builders, copy
constructors, and other useful boilerplate code.

For example, the following code constructs an `EscrowCreate` object, which represents
an [EscrowCreate](https://xrpl.org/escrowcreate.html) Transaction:

```java
EscrowCreate escrowCreate = EscrowCreate.builder()
  .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
  .fee(XrpCurrencyAmount.ofDrops(12))
  .sequence(UnsignedInteger.ONE)
  .amount(XrpCurrencyAmount.ofDrops(10000))
  .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
  .destinationTag(UnsignedInteger.valueOf(23480))
  .cancelAfter(UnsignedLong.valueOf(533257958))
  .finishAfter(UnsignedLong.valueOf(533171558))
  .condition(CryptoConditionReader.readCondition(
  BaseEncoding.base16()
  .decode("A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"))
  )
  .sourceTag(UnsignedInteger.valueOf(11747))
  .build();
```

These objects can be serialized to, and deserialized from, rippled JSON representations using the provided
Jackson `ObjectMapper`, which can be instantiated
using [`ObjectMapperFactory`](./xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/jackson/ObjectMapperFactory.java).

Using the `EscrowCreate` object created above, it is then possible to use the supplied `ObjectMapper` to serialize to
JSON like this:

```java
ObjectMapper objectMapper=ObjectMapperFactory.create();
  String json=objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(escrowCreate);
  System.out.println(json);
```

Which produces the following output:

```
{
  "Account" : "rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn",
  "Fee" : "12",
  "Sequence" : 1,
  "SourceTag" : 11747,
  "Flags" : 2147483648,
  "Amount" : "10000",
  "Destination" : "rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW",
  "DestinationTag" : 23480,
  "CancelAfter" : 533257958,
  "FinishAfter" : 533171558,
  "Condition" : "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100",
  "TransactionType" : "EscrowCreate"
}
```

#### Public/Private Key Material

Most operations using this library require some sort of private key material. Broadly speaking, the library supports
two mechanisms: (1) in-memory private keys, and (2) in-memory _references_ to private keys where the actual private key 
material lives in an external system (e.g., keys in a Hardware Security Module, or HSM). In Java, this is modeled 
using the `PrivateKeyable` interface, which has two subclasses: `PrivateKey` and `PrivateKeyReference`.

##### In-Memory Private Keys (`PrivateKey`)

`PrivateKey` represents a private key held in memory, existing in the same JVM that is executing xrpl4j code. This key 
variant can be useful in the context of an android or native application, but is likely not suitable for server-side 
application because private key material is held in-memory (for these scenarios, consider using a remote service like 
an HSM).

For use-cases that require private keys to exist inside the running JVM, the following examples shows how to
generate a keypair, and also how to derive an XRPL address from there:

```java
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
  
...

Seed seed=Seed.ed255519Seed(); // <-- Generates a random seed.
PrivateKey privateKey=seed.derivePrivateKey(); // <-- Derive a private key from the seed.
PublicKey publicKey=privateKey.derivePublicKey(); // <-- Derive a public key from the private key.
Address address=publicKey.deriveAddress(); // <-- Derive an address from the public key.
```

##### Private Key References (`PrivateKeyReference`)

For applications with higher-security requirements, private-key material can be stored outside the JVM 
using an external system that can simultaneously manage the key material and also perform critical signing operations 
without exposing key material to the outside world (e.g., an HSM or cloud service provider). For these scenarios, 
`PrivateKeyReference` can be used.

This library does not provide an implementation that interacts with any particular external signing service or HSM.
However, developers wishing to support such interactions should extend `PrivateKeyReference` for the particular external service, and implement
[SignatureService](./xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/crypto/signing/SignatureService.java) for their `PrivateKeyReference` type.
interface. In
addition, [FauxGcpKmsSignatureServiceTest](./xrpl4j-core/src/test/java/org/xrpl/xrpl4j/crypto/signing/faux/FauxGcpKmsSignatureServiceTest.java)
and [FauxAwsKmsSignatureServiceTest](./xrpl4j-core/src/test/java/org/xrpl/xrpl4j/crypto/signing/faux/FauxAwsKmsSignatureServiceTest.java)
illustrate faux variants of a simulated external key provider that can also be used for further guidance.

### Signing and Verifying Transactions

The main interface used to sign and verify transactions
is [SignatureService](./xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/crypto/signing/SignatureService.java),
which has two concrete implementations: `BcSignatureService` and `BcDerivedKeySignatureService`. The first uses
in-memory private key material to perform signing and validation operations, while the latter can be used to derive
multiple private keys using a single entropy source combined with differing unique key identifiers (e.g., User Ids).

#### Construct and Sign an XRP Payment:

The following example illustrates how to construct a payment transaction, sign it using an in-memory private key, and
then submit that transaction to the XRP Ledger for processing and validation:

```java
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;

// Construct a SignatureService that uses in-memory Keys (see SignatureService.java for alternatives).
SignatureService signatureService = new BcSignatureService();

// Sender (using ed25519 key)
Seed senderSeed = Seed.ed255519Seed();
PrivateKey senderPrivateKey=senderSeed.derivePrivateKey();
PublicKey senderPublicKey=senderPrivateKey.derivePublicKey();
Address senderAddress=senderPublicKey.deriveAddress();

// Receiver (using secp256k1 key)
Address receiverAddress=Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");

// Construct a Payment
Payment payment=...; // See V3 ITs for examples.

SingleSignedTransaction<Payment> signedTransaction=signatureService.sign(sourcePrivateKey,payment);
SubmitResult<Payment> result=xrplClient.submit(signedTransaction);
assertThat(result.result()).isEqualTo("tesSUCCESS");
```

## Development

### Project Structure

Xrpl4j is structured as a Maven multi-module project, with the following modules:

- **xrpl4j-core**: [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-binary-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-binary-codec)
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