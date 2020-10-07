package com.ripple.xrpl4j.keypairs;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.UnsignedByte;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.util.Objects;

public class Ed25519KeyPairService implements KeyPairService {

  private AddressCodec addressCodec;

  public Ed25519KeyPairService() {
    this.addressCodec = new AddressCodec();
  }

  public Ed25519KeyPairService(final AddressCodec addressCodec) {
    Objects.requireNonNull(addressCodec);

    this.addressCodec = addressCodec;
  }

  /**
   * Derive an ED25519 private/public key pair from a seed.
   *
   * @param seed An {@link UnsignedByteArray} containing the seed.
   * @return The {@link KeyPair} containing an ED25519 public and private key that was derived from the seed.
   */
  @Override
  public KeyPair deriveKeyPair(UnsignedByteArray seed) {
    UnsignedByteArray rawPrivateKey = HashUtils.sha512Half(seed);
    Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(rawPrivateKey.toByteArray(), 0);

    Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

    // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of sekp256k1 keys.
    // Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
    UnsignedByte prefix = UnsignedByte.of(0xED);
    UnsignedByteArray prefixedPrivateKey = UnsignedByteArray.of(Lists.newArrayList(prefix))
      .concat(UnsignedByteArray.of(privateKey.getEncoded()));
    UnsignedByteArray prefixedPublicKey = UnsignedByteArray.of(Lists.newArrayList(prefix))
      .concat(UnsignedByteArray.of(publicKey.getEncoded()));

    return KeyPair.builder()
      .privateKey(prefixedPrivateKey.hexValue())
      .publicKey(prefixedPublicKey.hexValue())
      .build();
  }

  /**
   * Derive an ED25519 private/public key pair from a Base58Check encoded seed.
   *
   * @param seed A Base58Check encoded {@link String} containing the seed.
   * @return The {@link KeyPair} containing an ED25519 public and private key that was derived from the seed.
   */
  @Override
  public KeyPair deriveKeyPair(String seed) {
    return deriveKeyPair(addressCodec.decodeSeed(seed).bytes());
  }

  /**
   * Sign a message using the Ed25519 algorithm.
   *
   * @param message A {@link String} containing the hex encoded message to sign.
   * @param privateKey A {@link String} containing the hex encoded private key to sign with.
   * @return The hex encoded ED25519 signature of the message, using the privateKey.
   */
  @Override
  public String sign(String message, String privateKey) {
    Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(privateKey.substring(2)), // Remove ED prefix byte
      0
    );

    Signer signer = new Ed25519Signer();
    signer.init(true, privateKeyParameters);
    byte[] messageBytes = BaseEncoding.base16().decode(message);
    signer.update(messageBytes, 0, messageBytes.length);

    try {
      byte[] signature = signer.generateSignature();
      return BaseEncoding.base16().encode(signature);
    } catch (CryptoException e) {
      throw new RuntimeException(e); // TODO: custom exception
    }
  }

  /**
   * Verify an ED25519 signature of a message with the given public key.
   *
   * @param message A {@link String} containing the hex encoded message that was signed.
   * @param signature A {@link String} containing the hex encoded signature.
   * @param publicKey A {@link String} containing the hex encoded public key corresponding to the private key
   *                  that was used to generate the signature.
   * @return true if the signature was valid, false if not.
   */
  @Override
  public boolean verify(String message, String signature, String publicKey) {
    Ed25519PublicKeyParameters publicKeyParameters = new Ed25519PublicKeyParameters(
      BaseEncoding.base16().decode(publicKey.substring(2)), // Remove ED prefix byte
      0
    );

    Signer verifier = new Ed25519Signer();
    verifier.init(false, publicKeyParameters);
    byte[] messageBytes = BaseEncoding.base16().decode(message);
    verifier.update(messageBytes, 0, messageBytes.length);
    return verifier.verifySignature(BaseEncoding.base16().decode(signature));
  }
}
