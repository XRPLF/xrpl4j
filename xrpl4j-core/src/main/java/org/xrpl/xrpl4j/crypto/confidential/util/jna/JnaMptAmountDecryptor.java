package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;

import java.util.Objects;

/**
 * Implementation of {@link MptAmountDecryptor} that delegates to the native mpt-crypto C library
 * via the {@link NativeMptCrypto} bridge.
 *
 * <p>This class lives in {@code xrpl4j-core} and has no compile-time dependency on JNA.
 * The {@link NativeMptCrypto} implementation is loaded from {@code xrpl4j-mpt-crypto} at runtime.</p>
 */
public class JnaMptAmountDecryptor implements MptAmountDecryptor {

  private static final int CIPHERTEXT_SIZE = 66;
  private static final int PRIVKEY_SIZE = 32;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge from {@code xrpl4j-mpt-crypto}.
   *
   * @throws IllegalStateException if {@code xrpl4j-mpt-crypto} is not on the classpath.
   */
  public JnaMptAmountDecryptor() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaMptAmountDecryptor(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public UnsignedLong decrypt(
    final EncryptedAmount ciphertext,
    final PrivateKey privateKey,
    final UnsignedLong minAmount,
    final UnsignedLong maxAmount
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(minAmount, "minAmount must not be null");
    Objects.requireNonNull(maxAmount, "maxAmount must not be null");

    Preconditions.checkArgument(
      privateKey.keyType() == KeyType.SECP256K1,
      "privateKey must be a SECP256K1 key, but was %s",
      privateKey.keyType()
    );

    byte[] ciphertextBytes = ciphertext.toBytes().toByteArray();
    Preconditions.checkArgument(
      ciphertextBytes.length == CIPHERTEXT_SIZE,
      "ciphertext must be %s bytes, but was %s bytes",
      CIPHERTEXT_SIZE, ciphertextBytes.length
    );

    UnsignedByteArray naturalBytes = privateKey.naturalBytes();
    byte[] privkeyBytes = naturalBytes.toByteArray();
    Preconditions.checkArgument(
      privkeyBytes.length == PRIVKEY_SIZE,
      "privateKey must be %s bytes, but was %s bytes",
      PRIVKEY_SIZE, privkeyBytes.length
    );

    long[] outAmount = new long[1];
    int result = nativeCrypto.decryptAmount(ciphertextBytes, privkeyBytes, outAmount);

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_decrypt_amount failed with error code: " + result);
    }

    return UnsignedLong.fromLongBits(outAmount[0]);
  }
}
