# XRPL Java Binary Codec

This package enables encoding XRPL primitives from their JSON format into
the [XRPL binary serialization format](https://xrpl.org/serialization.html) and vice versa.

## Usage

The primary API can be found in [`XrplBinaryCodec`](./XrplBinaryCodec.java).

### Encode/Decode JSON and Binary

The following code encodes a Payment transaction in JSON form to the canonical XRPL binary format as a
hexadecimal `String`:

```java
String paymentJson="{"+
  "\"Account\" : \"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\","+
  "\"Fee\" : \"789\","+
  "\"Sequence\" : 56565656,"+
  "\"SourceTag\" : 1,"+
  "\"Flags\" : 2147483648,"+
  "\"Amount\" : \"12345\","+
  "\"Destination\" : \"rrrrrrrrrrrrrrrrrrrrBZbvji\","+
  "\"DestinationTag\" : 2,"+
  "\"TransactionType\" : \"Payment\""+
"}";

System.out.println("JSON="+paymentJson);

String binary=binaryCodec.encode(paymentJson);
System.out.println("Binary hex="+binary);

String decodedJson=binaryCodec.decode(binary);
System.out.println("Decoded JSON="+decodedJson);
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
