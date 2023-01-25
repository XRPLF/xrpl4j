# xrpl4j-binary-codec [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-binary-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-binary-codec)

Library to encode XRPL primitives in their JSON format to the [XRPL binary serialization format](https://xrpl.org/serialization.html) and vice versa.

## Installation
Use this module in your project by adding the following to your `pom.xml`:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-binary-codec</artifactId>
  <version>2.1.1</version>
</dependency>
```

## Usage
The primary API can be found in [`XrplBinaryCodec`](../xrpl4j-core/src/main/java/binary/XrplBinaryCodec.java).

For a full API reference, check out the [`XrplBinaryCodec` Javadoc](https://www.javadoc.io/doc/org.xrpl/xrpl4j-binary-codec/latest/org/xrpl/xrpl4j/codec/binary/XrplBinaryCodec.html).

### Encode/Decode JSON and Binary
The following code encodes a Payment transaction in JSON form to the canonical XRPL binary format as a hexadecimal `String`:
```java
String paymentJson = "{\n" +
      "  \"Account\" : \"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\",\n" +
      "  \"Fee\" : \"789\",\n" +
      "  \"Sequence\" : 56565656,\n" +
      "  \"SourceTag\" : 1,\n" +
      "  \"Flags\" : 2147483648,\n" +
      "  \"Amount\" : \"12345\",\n" +
      "  \"Destination\" : \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "  \"DestinationTag\" : 2,\n" +
      "  \"TransactionType\" : \"Payment\"\n" +
      "}";
System.out.println("JSON: \n" + paymentJson);

String binary = binaryCodec.encode(paymentJson);
System.out.println("Binary hex: " + binary);

String decodedJson = binaryCodec.decode(binary);
System.out.println("Decoded JSON: \n" + decodedJson);
```

Producing the following output:
```
JSON: 
{
  "Account" : "r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK",
  "Fee" : "789",
  "Sequence" : 56565656,
  "SourceTag" : 1,
  "Flags" : 2147483648,
  "Amount" : "12345",
  "Destination" : "rrrrrrrrrrrrrrrrrrrrBZbvji",
  "DestinationTag" : 2,
  "TransactionType" : "Payment"
}
Binary hex: 1200002280000000230000000124035F1F982E000000026140000000000030396840000000000003158114EE39E6D05CFD6A90DAB700A1D70149ECEE29DFEC83140000000000000000000000000000000000000001
Decoded JSON: 
{"TransactionType":"Payment","Flags":2147483648,"SourceTag":1,"Sequence":56565656,"DestinationTag":2,"Amount":"12345","Fee":"789","Account":"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK","Destination":"rrrrrrrrrrrrrrrrrrrrBZbvji"}
```
