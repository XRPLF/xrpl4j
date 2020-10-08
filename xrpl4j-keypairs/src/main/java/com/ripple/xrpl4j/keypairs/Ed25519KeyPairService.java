package com.ripple.xrpl4j.keypairs;

import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.Decoded;
import com.ripple.xrpl4j.codec.addresses.UnsignedByte;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.addresses.Version;
import com.ripple.xrpl4j.codec.addresses.VersionType;
import com.ripple.xrpl4j.codec.addresses.exceptions.DecodeException;
import com.ripple.xrpl4j.keypairs.exceptions.SigningException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * Implementation of {@link KeyPairService} which uses the ED25519 algorithm to derive keys and sign/verify
 * signatures.
 */
public class Ed25519KeyPairService extends AbstractKeyPairService {

  private static final Ed25519KeyPairService INSTANCE = new Ed25519KeyPairService(AddressCodec.getInstance());

  public static Ed25519KeyPairService getInstance() {
    return INSTANCE;
  }

  Ed25519KeyPairService(final AddressCodec addressCodec) {
    Objects.requireNonNull(addressCodec);
    this.addressCodec = addressCodec;
    this.signer = new Ed25519Signer();
  }

  @Override
  public String generateSeed() {
    return generateSeed(UnsignedByteArray.of(SecureRandom.getSeed(16)));
  }

  @Override
  public String generateSeed(UnsignedByteArray entropy) {
    return addressCodec.encodeSeed(entropy, VersionType.ED25519);
  }

  @Override
  public KeyPair deriveKeyPair(UnsignedByteArray seed) {
    UnsignedByteArray rawPrivateKey = HashUtils.sha512Half(seed);
    Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(rawPrivateKey.toByteArray(), 0);

    Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

    // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of sekp256k1 keys.
    // Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
    UnsignedByte prefix = UnsignedByte.of(0xED);
    UnsignedByteArray prefixedPrivateKey = UnsignedByteArray.of(prefix)
      .append(UnsignedByteArray.of(privateKey.getEncoded()));
    UnsignedByteArray prefixedPublicKey = UnsignedByteArray.of(prefix)
      .append(UnsignedByteArray.of(publicKey.getEncoded()));

    return KeyPair.builder()
      .privateKey(prefixedPrivateKey.hexValue())
      .publicKey(prefixedPublicKey.hexValue())
      .build();
  }

  @Override
  public KeyPair deriveKeyPair(String seed) {
    Decoded decoded = addressCodec.decodeSeed(seed);

    if (!decoded.version().equals(Version.ED25519_SEED)) {
      throw new DecodeException("Seed must use ED25519 algorithm. Algorithm was " + decoded.version());
    }

    return deriveKeyPair(decoded.bytes());
  }

  @Override
  public String sign(UnsignedByteArray message, String privateKey) {
    Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(privateKey.substring(2)), // Remove ED prefix byte
      0
    );

    signer.reset();
    signer.init(true, privateKeyParameters);
    signer.update(message.toByteArray(), 0, message.getUnsignedBytes().size());

    try {
      byte[] signature = signer.generateSignature();
      return BaseEncoding.base16().encode(signature);
    } catch (CryptoException e) {
      throw new SigningException(e);
    }
  }

  @Override
  public boolean verify(UnsignedByteArray message, String signature, String publicKey) {
    Ed25519PublicKeyParameters publicKeyParameters = new Ed25519PublicKeyParameters(
      BaseEncoding.base16().decode(publicKey.substring(2)), // Remove ED prefix byte
      0
    );

    signer.reset();
    signer.init(false, publicKeyParameters);
    signer.update(message.toByteArray(), 0, message.getUnsignedBytes().size());
    return signer.verifySignature(BaseEncoding.base16().decode(signature));
  }
}
