package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Arrays;
import java.util.Objects;

/**
 * A way of encapsulating a secret value set by a server.
 *
 * <p>This class defensively copies the byte array supplied to {@link #of(byte[])} upon construction. As a result,
 * {@link #destroy()} only zeroizes this instance's internal copy of the secret; it never mutates the caller's
 * original array. Callers that source secret material from a persistent, in-memory {@code byte[]} (for example, one
 * loaded once from a keystore or environment variable and reused across calls) can therefore safely construct a new
 * {@link ServerSecret} from that array on every invocation of a {@link ServerSecretSupplier} without fear that a
 * prior {@link #destroy()} call will have zeroed out their source material.</p>
 */
public class ServerSecret implements javax.security.auth.Destroyable {

  private final byte[] value;
  private boolean destroyed;

  /**
   * Instantiates a new builder.
   *
   * @param value This passphrase's binary value. This method makes a defensive copy of {@code value}, so destroying
   *              the returned {@link ServerSecret} will not mutate or zero-out the array passed into this method.
   *
   * @return A {@link ServerSecret}.
   */
  public static ServerSecret of(final byte[] value) {
    return new ServerSecret(value);
  }

  /**
   * Required-args Constructor.
   *
   * @param value This passphrase's binary value. This value is defensively copied so that this class's internal
   *              state is never aliased to memory owned by the caller.
   */
  private ServerSecret(final byte[] value) {
    Objects.requireNonNull(value);
    this.value = Arrays.copyOf(value, value.length);
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
