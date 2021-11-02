# xrpl4j-keypairs [![javadoc](https://javadoc.io/badge2/org.xrpl/xrpl4j-keypairs/javadoc.svg?color=blue)](https://javadoc.io/doc/org.xrpl/xrpl4j-keypairs)
Library for generating secure random seeds, deriving public/private keypairs, and signing messages and verifying signatures. This library supports both [ECDSA 
using the secp256k1 elliptic curve and EdDSA using the Ed25519 elliptic curve](https://xrpl.org/cryptographic-keys.html#signing-algorithms).

## Installation
Use this module in your project by adding the following to your `pom.xml`:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-keypairs</artifactId>
  <version>2.1.1</version>
</dependency>
```

## Usage
The [`DefaultWalletFactory`](./src/main/java/org/xrpl/xrpl4j/wallet/DefaultWalletFactory.java) class can be used to generate a random wallet, 
or to restore a wallet from a seed or a key pair. `DefaultWalletFactory` uses a 
[`DefaultKeyPairService`](./src/main/java/org/xrpl/xrpl4j/keypairs/DefaultKeyPairService.java), which can also be used on its own, to 
generate seeds and derive keypairs and addresses.

### DefaultWalletFactory
The `DefaultWalletFactory` can generate new wallets and restore existing wallets.

The following code generates a new testnet wallet with an ED25519 public/private keypair:
```java
WalletFactory walletFactory = DefaultWalletFactory.getInstance();
SeedWalletGenerationResult seedWalletGenerationResult = walletFactory.randomWallet(true);
System.out.println("Generation result: " + seedWalletGenerationResult);
```

Which produces the following output:
```
Generation result: SeedWalletGenerationResult{
  seed=sEdSbPbSNZHqH1r1d27Sp3mh8Rq3oyu, 
  wallet=Wallet{
    privateKey=ED9000C29279778F95F807B3F3320E4F8DC9B08067E4F3A4CBE587F88CDB20AEBE, 
    publicKey=ED26F7054744A45706A6D138A32429111DCB090C0CBECE3853A59693657F7AB63A, 
    classicAddress=rKdYoCQbvNz12ELDVmiJcWUADZ1Lg2o9w1, 
    xAddress=TVR8ETfqXp1isjemD7wQCHguktJWAn8HhqpwCVThmUSVPPk, 
    isTest=true
  }
}

```

Alternatively, you can restore a wallet from a seed:
```java
WalletFactory walletFactory = DefaultWalletFactory.getInstance();
Wallet wallet = walletFactory.fromSeed("sEdSbPbSNZHqH1r1d27Sp3mh8Rq3oyu", true);
System.out.println("Wallet: " + wallet);
```

or from a `KeyPair`:
```java
WalletFactory walletFactory = DefaultWalletFactory.getInstance();
Wallet wallet = walletFactory.fromKeyPair(KeyPair.builder()
    .privateKey("ED9000C29279778F95F807B3F3320E4F8DC9B08067E4F3A4CBE587F88CDB20AEBE")
    .publicKey("ED26F7054744A45706A6D138A32429111DCB090C0CBECE3853A59693657F7AB63A")
    .build(),
  true);
System.out.println("Wallet: " + wallet);
```

which both produce the following output:
```
Wallet: Wallet{
  privateKey=ED9000C29279778F95F807B3F3320E4F8DC9B08067E4F3A4CBE587F88CDB20AEBE, 
  publicKey=ED26F7054744A45706A6D138A32429111DCB090C0CBECE3853A59693657F7AB63A, 
  classicAddress=rKdYoCQbvNz12ELDVmiJcWUADZ1Lg2o9w1, 
  xAddress=TVR8ETfqXp1isjemD7wQCHguktJWAn8HhqpwCVThmUSVPPk, 
  isTest=true
}
```

For full API documentation, check out the [`DefaultWalletFactory` Javadoc](https://www.javadoc.io/doc/org.xrpl/xrpl4j-keypairs/latest/org/xrpl/xrpl4j/wallet/DefaultWalletFactory.html).


### DefaultKeyPairService
`DefaultWalletFactory` uses an instance of `DefaultKeyPairService`, which is responsible for generating seeds, deriving `KeyPair`s from seeds, signing messages, and verifying message signatures.

The following code generates a seed, derives a `KeyPair` from the seed, signs a message with that `KeyPair`, and finally verifies the signature:
```java
KeyPairService keyPairService = DefaultKeyPairService.getInstance();
String seed = keyPairService.generateSeed();
System.out.println("Generated seed: " + seed);

KeyPair keyPair = keyPairService.deriveKeyPair(seed);
System.out.println("Derived KeyPair: " + keyPair);

String message = BaseEncoding.base16().encode("test message".getBytes());
String signature = keyPairService.sign(message, keyPair.privateKey());
System.out.println("Message signature: " + signature);

boolean verifies = keyPairService.verify(message, signature, keyPair.publicKey());
System.out.println("Signature verified? : " + verifies);
```

which produces the following output:
```
Generated seed: sEd7Ld6RUVbX9cZEG3jx221tCkq3ZaP
Derived KeyPair: KeyPair{
  privateKey=ED2AAB0754AEB0638D8B87777031F8A524C41A1492AF910BEEF2FBFDF08E2C9168, 
  publicKey=ED0924FE18B96F63BD9C1EF74BC2DA8093840A4F1A272BD13FDBF6E3399AF24448
}
Message signature: 6687EF589B2571938F42043A3617B09AEA92E5C2420C307E670145E9620149D8798224DF1AEB3E2B90CD30D2AD097530F8E32AF7324F2A0B573090C913189C00
Signature verified? : true
```
