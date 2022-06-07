package org.xrpl.xrpl4j.crypto.bc.signing;

import com.google.common.base.Preconditions;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * A container for EcDSA signature content.
 */
@Value.Immutable
@SuppressWarnings( {"LocalVariableName", "MethodName"})
public interface EcDsaSignature {

  static ImmutableEcDsaSignature.Builder builder() {
    return ImmutableEcDsaSignature.builder();
  }

  /**
   * Create an {@link EcDsaSignature} from a DER encoded byte array signature.
   *
   * @param bytes A DER encoded byte array containing a signature.
   * @return An {@link EcDsaSignature}.
   */
  static EcDsaSignature fromDer(byte[] bytes) {
    try {
      ASN1InputStream decoder = new ASN1InputStream(bytes);
      DLSequence seq = (DLSequence) decoder.readObject();
      ASN1Integer r;
      ASN1Integer s;
      try {
        r = (ASN1Integer) seq.getObjectAt(0);
        s = (ASN1Integer) seq.getObjectAt(1);
      } catch (ClassCastException e) {
        return null;
      } finally {
        decoder.close();
      }
      // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
      // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
      return EcDsaSignature.builder()
        .r(r.getPositiveValue())
        .s(s.getPositiveValue())
        .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The r component of this {@link EcDsaSignature}.
   *
   * @return A {@link BigInteger} denoting the r component of this signature.
   */
  BigInteger r();

  /**
   * The s component of this {@link EcDsaSignature}.
   *
   * @return A {@link BigInteger} denoting the r component of this signature.
   */
  BigInteger s();

  /**
   * Encode this {@link EcDsaSignature} to the ASN.1 DER format.
   *
   * @return An {@link UnsignedByteArray} containing the bytes of the encoded signature.
   */
  @Value.Derived
  default UnsignedByteArray der() {
    // Usually 70-72 bytes.
    ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
    try {
      DERSequenceGenerator seq = new DERSequenceGenerator(bos);
      seq.addObject(new ASN1Integer(r()));
      seq.addObject(new ASN1Integer(s()));
      seq.close();
      return UnsignedByteArray.of(bos.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Make sure signature is canonical to protect against signature morphing attacks.
   *
   * <p>Signature should be:
   * {@code <30> <len> [ <02> <lenR> <R> ] [ <02> <lenS> <S> ]} where {@code 6 <= len <= 70} {@code  1 <= lenR <= 33}
   * {@code 1 <= lenS <= 33}
   * </p>
   */
  @Value.Check
  default void isStrictlyCanonical() {
    int sigLen = der().length();

    Preconditions.checkArgument(sigLen >= 8 && sigLen <= 72);
    Preconditions.checkArgument((der().get(0).asInt() == 0x30) && (der().get(1).asInt() == (sigLen - 2)));

    // Find R and check its length
    int rPos = 4;
    int rLen = der().get(rPos - 1).asInt();

    Preconditions.checkArgument(rLen >= 1 && rLen <= 33 && (rLen + 7) <= sigLen, "r is the wrong length.");

    // Find S and check its length
    int sPos = rLen + 6;
    int sLen = der().get(sPos - 1).asInt();
    Preconditions.checkArgument(sLen >= 1 && sLen <= 33 && (rLen + sLen + 6) == sigLen, "s is the wrong length.");

    Preconditions.checkArgument(
      der().get(rPos - 2).asInt() == 0x02 && der().get(sPos - 2).asInt() == 0x02,
      "r or s have the wrong type."
    );

    Preconditions.checkArgument((der().get(rPos).asInt() & 0x80) == 0, "r cannot be negative.");

    Preconditions.checkArgument(der().get(rPos).asInt() != 0 || rLen != 1, "r cannot be 0.");

    Preconditions.checkArgument(der().get(rPos).asInt() != 0 || (der().get(rPos + 1).asInt() & 0x80) != 0,
      "r cannot be padded.");

    Preconditions.checkArgument((der().get(sPos).asInt() & 0x80) == 0, "s cannot be negative.");

    Preconditions.checkArgument(der().get(sPos).asInt() != 0 || sLen != 1, "s cannot be 0.");

    Preconditions.checkArgument(
      der().get(sPos).asInt() != 0 || (der().get(sPos + 1).asInt() & 0x80) != 0,
      "s cannot be padded");

    byte[] rBytes = new byte[rLen];
    byte[] sBytes = new byte[sLen];

    System.arraycopy(der().toByteArray(), rPos, rBytes, 0, rLen);
    System.arraycopy(der().toByteArray(), sPos, sBytes, 0, sLen);

    BigInteger r = new BigInteger(1, rBytes);
    BigInteger s = new BigInteger(1, sBytes);

    BigInteger order = Secp256k1.EC_DOMAIN_PARAMETERS.getN();

    Preconditions.checkArgument(r.compareTo(order) <= -1 && s.compareTo(order) <= -1, "r or s greater than modulus");
    Preconditions.checkArgument(order.subtract(s).compareTo(s) > -1);

  }
}