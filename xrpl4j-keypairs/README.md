# xrpl4j-keypairs
Library for generating secure random seeds, deriving public/private keypairs, and signing messages and verifying signatures. This library supports both [ECDSA 
using the secp256k1 elliptic curve and EdDSA using the Ed25519 elliptic curve](https://xrpl.org/cryptographic-keys.html#signing-algorithms).

# Usage
The [`DefaultWalletFactory`](xrpl4j-keypairs/src/main/java/org/xrpl/xrpl4j/wallet/DefaultWalletFactory.java) class can be used to generate a random wallet, 
or to restore a wallet from a seed or a key pair. `DefaultWalletFactory` uses a 
[`DefaultKeyPairService`](xrpl4j-keypairs/src/main/java/org/xrpl/xrpl4j/keypairs/DefaultKeyPairService.java), which can also be used on its own, to 
generate seeds and derive keypairs and addresses.
