package org.xrpl.xrpl4j.crypto.mpt.elgamal;

import org.bouncycastle.math.ec.ECPoint;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an ElGamal key pair for secp256k1.
 */
@Deprecated
public final class ElGamalKeyPair {

  private final byte[] privateKey;
  private final ECPoint publicKey;

  /**
   * Constructs a new ElGamalKeyPair.
   *
   * @param privateKey The 32-byte private key.
   * @param publicKey  The public key as an EC point.
   */
  public ElGamalKeyPair(byte[] privateKey, ECPoint publicKey) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    if (privateKey.length != 32) {
      throw new IllegalArgumentException("privateKey must be 32 bytes");
    }
    this.privateKey = Arrays.copyOf(privateKey, privateKey.length);
    this.publicKey = publicKey;
  }

  /**
   * Gets the private key.
   *
   * @return A copy of the 32-byte private key.
   */
  public byte[] privateKey() {
    return Arrays.copyOf(privateKey, privateKey.length);
  }

  /**
   * Gets the public key.
   *
   * @return The public key as an EC point.
   */
  public ECPoint publicKey() {
    return publicKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ElGamalKeyPair that = (ElGamalKeyPair) o;
    return Arrays.equals(privateKey, that.privateKey) &&
      Arrays.equals(publicKey.getEncoded(true), that.publicKey.getEncoded(true));
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(privateKey);
    result = 31 * result + Arrays.hashCode(publicKey.getEncoded(true));
    return result;
  }

  @Override
  public String toString() {
    return "ElGamalKeyPair{publicKey=" + bytesToHex(publicKey.getEncoded(true)) + "}";
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
