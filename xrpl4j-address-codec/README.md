# xrpl4j-address-codec [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-address-codec/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-address-codec)

Library for encoding seeds, addresses, and keys from bytes to the [XRPL Base58Check encoding format](https://xrpl.org/base58-encodings.html) and decoding Base58Check encoded `String`s to bytes.

## Installation
Use this module in your project by adding the following to your `pom.xml`:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-address-codec</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage
The primary API for this module can be found in the [`AddressCodec`](./src/main/java/org/xrpl/xrpl4j/codec/addresses/AddressCodec.java) class.

### Encode/Decode Seeds
The following code decodes a Base58 encoded seed value, then re-encodes the decoded bytes:
```java
AddressCodec addressCodec = AddressCodec.getInstance();

String seed = "sEdTM1uX8pu2do5XvTnutH6HsouMaM2";
System.out.println("Seed: " + seed);

Decoded decoded = addressCodec.decodeSeed(seed);
System.out.println("Decoded: " + decoded.bytes().hexValue());

String encoded = addressCodec.encodeSeed(
  decoded.bytes(),
  decoded.type().orElseThrow(() -> new RuntimeException("Cannot encode seed without version type."))
);

System.out.println("Encoded seed: " + encoded);
```

Which produces the following output:
```
Seed: sEdTM1uX8pu2do5XvTnutH6HsouMaM2
Decoded: 4C3A1D213FBDFB14C7C28D609469B341
Encoded seed: sEdTM1uX8pu2do5XvTnutH6HsouMaM2
```

### Encode/Decode XRPL Addresses
Similarly, XRPL addresses can be decoded from their Base58 format and vice versa:
```java
AddressCodec addressCodec = AddressCodec.getInstance();
Address address = Address.of("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN");
System.out.println("Address: " + address);

UnsignedByteArray decoded = addressCodec.decodeAccountId(address);
System.out.println("Decoded: " + decoded.hexValue());

Address encoded = addressCodec.encodeAccountId(decoded);

System.out.println("Encoded Address: " + encoded);
```

Which produces the following output:
```
Address: rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN
Decoded: BA8E78626EE42C41B46D46C3048DF3A1C3C87072
Encoded Address: rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN
```

An `AddressCodec` is also responsible for converting between Classic Addresses and X-Address format:
```java
AddressCodec addressCodec = AddressCodec.getInstance();

Address address = Address.of("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN");
UnsignedInteger tag = UnsignedInteger.valueOf(2345664);
System.out.println(String.format("Address: %s; Tag: %s", address, tag));

XAddress xAddress = addressCodec.classicAddressToXAddress(address, tag, true);
System.out.println("X-Address: " + xAddress);

ClassicAddress classicAddress = addressCodec.xAddressToClassicAddress(xAddress);
System.out.println("Classic address from X-Address: " + classicAddress);
```

Which produces the following output:
```
Address: rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN; Tag: 2345664
X-Address: TVLVXbLF8Yvds6YKtQYHDTDrZ18Anz2aHjqeKVTzVCy3oLo
Classic address from X-Address: ClassicAddress{classicAddress=rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN, tag=2345664, test=true}
```

For full documentation, please refer to the [`AddressCodec` Javadoc](https://www.javadoc.io/doc/org.xrpl/xrpl4j-address-codec/latest/org/xrpl/xrpl4j/codec/addresses/AddressCodec.html).
