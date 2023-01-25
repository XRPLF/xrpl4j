package org.xrpl.xrpl4j.crypto.core;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Arrays;
import java.util.Objects;

/**
 * A way of encapsulating a secret value set by a server.
 */
public class ServerSecret implements javax.security.auth.Destroyable {

  private final byte[] value;
  private boolean destroyed;

  /**
   * Instantiates a new builder.
   *
   * @param value This passphrase's binary value.
   *
   * @return A {@link ServerSecret}.
   */
  public static ServerSecret of(final byte[] value) {
    return new ServerSecret(value);
  }

  /**
   * Required-args Constructor.
   *
   * @param value This passphrase's binary value.
   */
  private ServerSecret(byte[] value) {
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
    if (!(obj instanceof ServerSecret)) {
      return false;
    }

    ServerSecret that = (ServerSecret) obj;

    return Arrays.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

  @Override
  public String toString() {
    return "ServerSecret{" +
      "value=[redacted]" +
      ", destroyed=" + destroyed +
      '}';
  }
}
