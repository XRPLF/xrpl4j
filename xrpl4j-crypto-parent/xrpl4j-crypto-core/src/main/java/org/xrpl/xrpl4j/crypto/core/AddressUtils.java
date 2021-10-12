package org.xrpl.xrpl4j.crypto.core;

import com.google.common.hash.Hashing;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Objects;

/**
 * A service to help with interactions involving XRPL addresses.
 */
public class AddressUtils {

  private static final AddressUtils INSTANCE = new AddressUtils();

  /**
   * Obtain the singleton instance of {@link AddressUtils}.
   *
   * @return An {@link AddressUtils}.
   */
  public static AddressUtils getInstance() {
    return INSTANCE;
  }

  /**
   * No-args constructor.
   */
  private AddressUtils() {

  }

  /**
   * Derive an XRPL address from a public key.
   *
   * @param publicKey The hexadecimal encoded public key of the account.
   *
   * @return A Base58Check encoded XRPL address in Classic Address form.
   */
  public Address deriveAddress(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);
    return AddressCodec.getInstance().encodeAccountId(computePublicKeyHash(publicKey.value()));
  }

  /**
   * Compute the RIPEMD160 of the SHA256 of the given public key, which can be encoded to an XRPL address.
   *
   * @param publicKey The public key that should be hashed.
   *
   * @return An {@link UnsignedByteArray} containing the non-encoded XRPL address derived from the public key.
   */
  @SuppressWarnings("UnstableApiUsage")
  private UnsignedByteArray computePublicKeyHash(final UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);

    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(sha256, 0, sha256.length);
    byte[] ripemdSha256 = new byte[digest.getDigestSize()];
    digest.doFinal(ripemdSha256, 0);
    return UnsignedByteArray.of(ripemdSha256);
  }
}
