package org.xrpl.xrpl4j.crypto.core.keys;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.wallet.WalletFactory;

import java.util.Objects;

/**
 * A typed instance of an XRPL private-key.
 */
public class PrivateKey implements PrivateKeyable, javax.security.auth.Destroyable {

  /**
   * Keys generated from the secp256k1 curve have 33 bytes in XRP Ledger. However, keys derived from the ed25519 curve
   * have only 32 bytes, and so get prefixed with this HEX value so that all keys in the ledger are 33 bytes.
   */
  public static final UnsignedByte PREFIX = UnsignedByte.of(0xED);

  private final UnsignedByteArray value;
  private final HashCode sha256HashCode;
  private boolean destroyed;

  /**
   * Instantiates a new instance of a private key using the supplied bytes. Note that in order to derive a private key,
   * considering using instances of either {@link WalletFactory} or {@link KeyPairService}.
   *
   * @param value An {@link UnsignedByteArray} containing this key's binary value.
   *
   * @return A {@link PrivateKey}.
   */
  public static PrivateKey of(final UnsignedByteArray value) {
    return new PrivateKey(value);
  }

  /**
   * Required-args Constructor.
   *
   * @param value An {@link UnsignedByteArray} for this key's value.
   */
  private PrivateKey(final UnsignedByteArray value) {
    this.value = Objects.requireNonNull(value);
    this.sha256HashCode = Hashing.sha256().hashBytes(this.value().toByteArray());
  }

  /**
   * Accessor for the key value, in binary (Note: will be 33 bytes).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public UnsignedByteArray value() {
    return UnsignedByteArray.of(value.toByteArray());
  }

  /**
   * The type of this key (either {@link VersionType#ED25519} or {@link VersionType#SECP256K1}).
   *
   * @return A {@link VersionType}.
   */
  public final VersionType versionType() {
    final UnsignedByte prefixByte = this.value().get(0);
    return prefixByte.equals(PREFIX) ? VersionType.ED25519 : VersionType.SECP256K1;
  }

  @Override
  public final void destroy() {
    this.value.destroy();
    this.destroyed = true;
  }

  @Override
  public final boolean isDestroyed() {
    return this.destroyed;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PrivateKey)) {
      return false;
    }

    PrivateKey that = (PrivateKey) obj;

    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  /**
   * A SHA-256 hash of the private key material contained in this object.
   *
   * @return A hex-encoded {@link String} representing the hash of this private key.
   */
  public String sha256() {
    return sha256HashCode.toString();
  }

  @Override
  public String toString() {
    return "PrivateKey{" +
      "sha256=" + sha256() +
      ", value=[redacted]" +
      ", destroyed=" + destroyed +
      '}';
  }
}
