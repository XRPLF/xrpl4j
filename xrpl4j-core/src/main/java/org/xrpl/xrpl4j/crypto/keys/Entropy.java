package org.xrpl.xrpl4j.crypto.keys;

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

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.SecureRandomUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * A compact value that is used to derive the actual private and public keys for an account.
 */
public class Entropy implements javax.security.auth.Destroyable {

  private final byte[] value;
  private boolean destroyed;

  /**
   * Construct a new instance of an {@link Entropy}.
   *
   * @return An {@link Entropy}.
   */
  public static Entropy newInstance() {
    final byte[] entropyBytes = SecureRandomUtils.secureRandom().generateSeed(16);
    return new Entropy(entropyBytes);
  }

  /**
   * Construct a new instance of an {@link Entropy} from the supplied byte array.
   *
   * @param entropy An array of bytes.
   *
   * @return An {@link Entropy}.
   */
  public static Entropy of(final byte[] entropy) {
    Objects.requireNonNull(entropy);
    return new Entropy(entropy);
  }

  /**
   * Required-args constructor.
   *
   * @param entropy An byte array containing random values.
   */
  private Entropy(final byte[] entropy) {
    Objects.requireNonNull(entropy);
    Preconditions.checkArgument(entropy.length == 16, "Entropy must be 16 bytes");
    final byte[] copiedEntropy = new byte[entropy.length];
    System.arraycopy(entropy, 0, copiedEntropy, 0, entropy.length);
    this.value = copiedEntropy;
  }

  /**
   * Accessor for a copy of the value of this seed.
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public UnsignedByteArray value() {
    final byte[] copiedValue = new byte[this.value.length];
    System.arraycopy(this.value, 0, copiedValue, 0, value.length);
    return UnsignedByteArray.of(copiedValue);
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
    if (!(obj instanceof Entropy)) {
      return false;
    }

    Entropy that = (Entropy) obj;
    return that.value().equals(this.value());
  }

  @Override
  public int hashCode() {
    return value().hashCode();
  }

  @Override
  public String toString() {
    return "Entropy{" +
      "value=[redacted]" +
      ", destroyed=" + destroyed +
      '}';
  }
}
