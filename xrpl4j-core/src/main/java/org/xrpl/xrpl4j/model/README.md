# xrpl4j-model [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-model/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-model)

Library providing Java objects modeling [XRP Ledger Objects](https://xrpl.org/ledger-data-formats.html), [Transactions](https://xrpl.org/transaction-formats.html), and [request parameters](https://xrpl.org/request-formatting.html)/[response results](https://xrpl.org/response-formatting.html) for the 
[rippled API](https://xrpl.org/public-rippled-methods.html).

## Installation
Use this module in your project by adding the following to your `pom.xml`:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-model</artifactId>
  <version>2.1.1</version>
</dependency>
```

## Usage
The objects in this module are annotated with `@Value.Immutable` from the [immutables library](https://immutables.github.io/), which generates immutable implementations with builders, copy constructors, and other useful boilerplate code.

For example, the following code constructs an `EscrowCreate` object, which represents an [EscrowCreate](https://xrpl.org/escrowcreate.html) Transaction:
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

These objects can be serialized to and deserialized from the rippled JSON representations using the provided Jackson `ObjectMapper`, which can be instantiated using [`ObjectMapperFactory`](../xrpl4j-core/src/main/java/model/jackson/ObjectMapperFactory.java).

Using the `EscrowCreate` object we just created, we can use the supplied `ObjectMapper` to serialize to JSON:
```java
ObjectMapper objectMapper = ObjectMapperFactory.create();
String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(escrowCreate);
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
