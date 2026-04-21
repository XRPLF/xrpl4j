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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Objects;

/**
 * Implementation of {@link MptAmountEncryptor} that delegates to the native mpt-crypto C library
 * via {@link MptCryptoLibrary}.
 *
 * <p>This class calls {@code mpt_encrypt_amount} from the native library to perform ElGamal
 * encryption of an amount using a secp256k1 public key.</p>
 */
public class JnaMptAmountEncryptor implements MptAmountEncryptor {

  private static final int PUBKEY_SIZE = 33;
  private static final int BLINDING_FACTOR_SIZE = 32;
  private static final int CIPHERTEXT_SIZE = 66;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaMptAmountEncryptor() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaMptAmountEncryptor(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public EncryptedAmount encrypt(
    final UnsignedLong amount,
    final PublicKey publicKey,
    final BlindingFactor blindingFactor
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    Preconditions.checkArgument(
      publicKey.keyType() == KeyType.SECP256K1,
      "publicKey must be a SECP256K1 key, but was %s",
      publicKey.keyType()
    );

    byte[] pubkeyBytes = publicKey.value().toByteArray();
    Preconditions.checkArgument(
      pubkeyBytes.length == PUBKEY_SIZE,
      "publicKey must be %s bytes, but was %s bytes",
      PUBKEY_SIZE, pubkeyBytes.length
    );

    byte[] blindingBytes = blindingFactor.toBytes();
    Preconditions.checkArgument(
      blindingBytes.length == BLINDING_FACTOR_SIZE,
      "blindingFactor must be %s bytes, but was %s bytes",
      BLINDING_FACTOR_SIZE, blindingBytes.length
    );

    byte[] outCiphertext = new byte[CIPHERTEXT_SIZE];
    int result = lib.mpt_encrypt_amount(amount.longValue(), pubkeyBytes, blindingBytes, outCiphertext);
    if (result != 0) {
      throw new IllegalStateException("mpt_encrypt_amount failed with error code: " + result);
    }

    return EncryptedAmount.fromBytes(outCiphertext);
  }
}
