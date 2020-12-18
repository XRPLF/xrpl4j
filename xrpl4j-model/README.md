# xrpl4j-model
Library providing Java objects modeling [XRP Ledger Objects](https://xrpl.org/ledger-data-formats.html), [Transactions](https://xrpl.org/transaction-formats.html), and [request parameters](https://xrpl.org/request-formatting.html)/[response results](https://xrpl.org/response-formatting.html) for the 
[rippled API](https://xrpl.org/public-rippled-methods.html).

## Installation
Use this module in your project by adding the following to your `pom.xml`:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-model</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage
The objects in this module are annotated with `@Value.Immutable` from the [immutables library](https://immutables.github.io/), which generates immutable implementations with builders, copy constructors, and other useful boilerplate code.

These objects can be serialized to and deserialized from the rippled JSON representations using the provided Jackson `ObjectMapper`, which can be instantiated using [`ObjectMapperFactory`](src/main/java/org/xrpl/xrpl4j/model/jackson/ObjectMapperFactory.java).
