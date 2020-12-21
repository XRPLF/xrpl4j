# xrpl4j-keypairs
Library for generating secure random seeds, deriving public/private keypairs, and signing messages and verifying signatures. This library supports both [ECDSA 
using the secp256k1 elliptic curve and EdDSA using the Ed25519 elliptic curve](https://xrpl.org/cryptographic-keys.html#signing-algorithms).

## Installation
Use this module in your project by adding the following to your `pom.xml`:
```
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-keypairs</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage
The [`DefaultWalletFactory`](xrpl4j-keypairs/src/main/java/org/xrpl/xrpl4j/wallet/DefaultWalletFactory.java) class can be used to generate a random wallet, 
or to restore a wallet from a seed or a key pair. `DefaultWalletFactory` uses a 
[`DefaultKeyPairService`](xrpl4j-keypairs/src/main/java/org/xrpl/xrpl4j/keypairs/DefaultKeyPairService.java), which can also be used on its own, to 
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

For full API documentation, check out the [`DefaultWalletFactory` Javadoc](TODO: Link to javadoc).

