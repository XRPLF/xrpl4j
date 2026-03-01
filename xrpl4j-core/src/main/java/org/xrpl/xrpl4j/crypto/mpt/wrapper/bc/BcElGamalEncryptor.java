package org.xrpl.xrpl4j.crypto.mpt.wrapper.bc;

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
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.port.bc.BcElGamalEncryptorPort;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalEncryptor;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ElGamalEncryptor}.
 *
 * <p>This implementation delegates to {@link BcElGamalEncryptorPort}
 * and wraps the result in Java-friendly types.</p>
 */
public class BcElGamalEncryptor implements ElGamalEncryptor {

  private final BcElGamalEncryptorPort encryptor;

  /**
   * Constructs a new instance with a new {@link BcElGamalEncryptorPort}.
   */
  public BcElGamalEncryptor() {
    this(new BcElGamalEncryptorPort());
  }

  /**
   * Constructs a new instance with the specified {@link BcElGamalEncryptorPort}.
   *
   * @param encryptor The port encryptor to delegate to.
   */
  public BcElGamalEncryptor(final BcElGamalEncryptorPort encryptor) {
    this.encryptor = Objects.requireNonNull(encryptor);
  }

  @Override
  public ElGamalCiphertext encrypt(
    final UnsignedLong amount,
    final PublicKey publicKey,
    final BlindingFactor blindingFactor
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    Preconditions.checkArgument(
      publicKey.value().length() > 0,
      "publicKey must not be empty"
    );
    Preconditions.checkArgument(
      publicKey.keyType() == KeyType.SECP256K1,
      "publicKey must be a SECP256K1 key, but was %s",
      publicKey.keyType()
    );

    ElGamalCiphertext ciphertext = encryptor.encrypt(
      amount,
      publicKey.value(),
      blindingFactor.value()
    );

    Secp256k1Operations.validateEcPair(
      ciphertext.c1().toByteArray(),
      ciphertext.c2().toByteArray()
    );

    return ciphertext;
  }
}

