package org.xrpl.xrpl4j.crypto.core.keys;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A way of choosing a {@link Seed} or {@link PrivateKey}.
 */
public class Passphrase implements javax.security.auth.Destroyable {

  private final byte[] value;
  private boolean destroyed;

  /**
   * Instantiates a new builder.
   *
   * @param value This passphrase's binary value.
   *
   * @return A {@link Passphrase}.
   */
  public static Passphrase of(final byte[] value) {
    return new Passphrase(value);
  }

  /**
   * Instantiates a new builder.
   *
   * @param valueAsString An {@link UnsignedByteArray} containing this key's binary value.
   *
   * @return A {@link Passphrase}.
   */
  public static Passphrase of(final String valueAsString) {
    return new Passphrase(valueAsString);
  }

  /**
   * Required-args Constructor.
   *
   * @param valueAsString An {@link UnsignedByteArray} containing this key's binary value.
   */
  private Passphrase(final String valueAsString) {
    this(valueAsString.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Required-args Constructor.
   *
   * @param value This passphrase's binary value.
   */
  private Passphrase(byte[] value) {
    Objects.requireNonNull(value);
    this.value = value;
  }

  /**
   * Accessor for a copy of the value of this passphrase.
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public byte[] value() {
    final byte[] copiedValue = new byte[this.value.length];
    System.arraycopy(this.value, 0, copiedValue, 0, value.length);
    return copiedValue;
  }

  @Override
  public final void destroy() {
    Arrays.fill(value, (byte) 0);
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
    if (!(obj instanceof Passphrase)) {
      return false;
    }

    Passphrase that = (Passphrase) obj;

    return Arrays.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

  @Override
  public String toString() {
    return "Passphrase{" +
      "value=[redacted]" +
      ", destroyed=" + destroyed +
      '}';
  }
}
