package org.xrpl.xrpl4j.crypto.mpt.elgamal;

import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an ElGamal ciphertext consisting of two EC points (C1, C2).
 *
 * <p>For an encryption of amount m with blinding factor k and public key Q:</p>
 * <ul>
 *   <li>C1 = k * G</li>
 *   <li>C2 = m * G + k * Q</li>
 * </ul>
 */
public final class ElGamalCiphertext {

  private final ECPoint c1;
  private final ECPoint c2;

  /**
   * Constructs a new ElGamalCiphertext.
   *
   * @param c1 The first component of the ciphertext.
   * @param c2 The second component of the ciphertext.
   */
  public ElGamalCiphertext(ECPoint c1, ECPoint c2) {
    this.c1 = Objects.requireNonNull(c1, "c1 must not be null");
    this.c2 = Objects.requireNonNull(c2, "c2 must not be null");
  }

  /**
   * Gets the first component C1.
   *
   * @return The C1 point.
   */
  public ECPoint c1() {
    return c1;
  }

  /**
   * Gets the second component C2.
   *
   * @return The C2 point.
   */
  public ECPoint c2() {
    return c2;
  }

  /**
   * Serializes this ciphertext to bytes (66 bytes total: 33 for C1 + 33 for C2).
   *
   * @return The serialized ciphertext.
   */
  public byte[] toBytes() {
    byte[] c1Bytes = c1.getEncoded(true);
    byte[] c2Bytes = c2.getEncoded(true);
    byte[] result = new byte[c1Bytes.length + c2Bytes.length];
    System.arraycopy(c1Bytes, 0, result, 0, c1Bytes.length);
    System.arraycopy(c2Bytes, 0, result, c1Bytes.length, c2Bytes.length);
    return result;
  }

  /**
   * Deserializes a ciphertext from bytes.
   *
   * @param bytes     The serialized ciphertext (66 bytes).
   * @param secp256k1 The secp256k1 operations instance for point decoding.
   *
   * @return The deserialized ciphertext.
   */
  public static ElGamalCiphertext fromBytes(byte[] bytes, Secp256k1Operations secp256k1) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Objects.requireNonNull(secp256k1, "secp256k1 must not be null");
    if (bytes.length != 66) {
      throw new IllegalArgumentException("bytes must be 66 bytes (33 + 33)");
    }
    byte[] c1Bytes = Arrays.copyOfRange(bytes, 0, 33);
    byte[] c2Bytes = Arrays.copyOfRange(bytes, 33, 66);
    ECPoint c1 = secp256k1.deserialize(c1Bytes);
    ECPoint c2 = secp256k1.deserialize(c2Bytes);
    return new ElGamalCiphertext(c1, c2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ElGamalCiphertext that = (ElGamalCiphertext) o;
    return Arrays.equals(c1.getEncoded(true), that.c1.getEncoded(true)) &&
      Arrays.equals(c2.getEncoded(true), that.c2.getEncoded(true));
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(c1.getEncoded(true));
    result = 31 * result + Arrays.hashCode(c2.getEncoded(true));
    return result;
  }

  @Override
  public String toString() {
    return "ElGamalCiphertext{" +
      "c1=" + bytesToHex(c1.getEncoded(true)) +
      ", c2=" + bytesToHex(c2.getEncoded(true)) +
      '}';
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
