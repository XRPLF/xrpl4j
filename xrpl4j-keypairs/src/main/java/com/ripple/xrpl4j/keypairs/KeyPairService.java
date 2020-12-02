package com.ripple.xrpl4j.keypairs;

import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.model.transactions.Address;

/**
 * Interface of a service that can perform the crypto operations necessary to create a wallet,
 * sign and verify XRPL transactions, and derive XRPL addresses.
 */
public interface KeyPairService {

  /**
   * Generate a random 16 byte seed to be used to derive a private key.
   *
   * @return A {@link String} containing a randomly generated Base58Check encoded seed value.
   */
  String generateSeed();

  /**
   * Generate a 16 byte seed, which can be used to derive a private key, from a non-encoded value.
   *
   * @param entropy An {@link UnsignedByteArray} containing the bytes of entropy to encode into a seed.
   * @return A {@link String} containing the Base58Check encoded seed value.
   */
  String generateSeed(UnsignedByteArray entropy);

  /**
   * Derive a public/private keypair from a Base58Check encoded 16 byte seed.
   *
   * @param seed A Base58Check encoded {@link String} containing the seed.
   * @return The {@link KeyPair} derived from the seed.
   */
  KeyPair deriveKeyPair(String seed);

  /**
   * Sign a message using the given private key.
   *
   * @param message    An {@link UnsignedByteArray} with an arbitrary message.
   * @param privateKey The hexadecimal encoded private key used to sign the transaction.
   * @return The signed message, in hexadecimal form.
   */
  String sign(UnsignedByteArray message, String privateKey);

  /**
   * Sign a message using the given private key.
   *
   * @param message    A hexadecimal encoded {@link String} containing an arbitrary message.
   * @param privateKey The hexadecimal encoded private key used to sign the transaction.
   * @return The signed message, in hexadecimal form.
   */
  String sign(String message, String privateKey);

  /**
   * Verify that the signature is valid, based on the message that was signed and the public key.
   *
   * @param message   The arbitrary message that was signed with a private key.
   * @param signature The hexadecimal encoded {@link String} containing the signature to verify.
   * @param publicKey The hexadecimal encoded public key derived from the private key that was used to sign the message.
   * @return true if the signature is valid, false if not.
   */
  boolean verify(UnsignedByteArray message, String signature, String publicKey);

  /**
   * Verify that the signature is valid, based on the message that was signed and the public key.
   *
   * @param message   The hexadecimal encoded arbitrary message that was signed with a private key.
   * @param signature The hexadecimal encoded {@link String} containing the signature to verify.
   * @param publicKey The hexadecimal encoded public key derived from the private key that was used to sign the message.
   * @return true if the signature is valid, false if not.
   */
  boolean verify(String message, String signature, String publicKey);

  /**
   * Derive an XRPL address from a public key.
   *
   * @param publicKey The hexadecimal encoded public key of the account.
   * @return A Base58Check encoded XRPL address in Classic Address form.
   */
  Address deriveAddress(String publicKey);

  /**
   * Derive an XRPL address from a public key.
   *
   * @param publicKey The public key of the account.
   * @return A Base58Check encoded XRPL address in Classic Address form.
   */
  Address deriveAddress(UnsignedByteArray publicKey);

}
