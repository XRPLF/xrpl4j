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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.port.bc.BcElGamalDecryptorPort;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.MPTAmountDecryptor;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link MPTAmountDecryptor}.
 *
 * <p>This implementation delegates to {@link BcElGamalDecryptorPort}
 * and validates inputs using Java-friendly types.</p>
 *
 * <p>Mirrors the C utility function {@code mpt_decrypt_amount} from mpt_utility.cpp.</p>
 */
public class BcMPTAmountDecryptor implements MPTAmountDecryptor {

  private final BcElGamalDecryptorPort decryptor;

  /**
   * Constructs a new instance with a new {@link BcElGamalDecryptorPort}.
   */
  public BcMPTAmountDecryptor() {
    this(new BcElGamalDecryptorPort());
  }

  /**
   * Constructs a new instance with the specified {@link BcElGamalDecryptorPort}.
   *
   * @param decryptor The port decryptor to delegate to.
   */
  public BcMPTAmountDecryptor(final BcElGamalDecryptorPort decryptor) {
    this.decryptor = Objects.requireNonNull(decryptor);
  }

  @Override
  public UnsignedLong decrypt(
    final ElGamalCiphertext ciphertext,
    final PrivateKey privateKey,
    final UnsignedLong minAmount,
    final UnsignedLong maxAmount
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(minAmount, "minAmount must not be null");
    Objects.requireNonNull(maxAmount, "maxAmount must not be null");

    Preconditions.checkArgument(
      privateKey.naturalBytes().length() > 0,
      "privateKey must not be empty"
    );
    Preconditions.checkArgument(
      privateKey.keyType() == KeyType.SECP256K1,
      "privateKey must be a SECP256K1 key, but was %s",
      privateKey.keyType()
    );

    // Validate that c1 and c2 are valid curve points
    Secp256k1Operations.validateEcPair(
      ciphertext.c1().toByteArray(),
      ciphertext.c2().toByteArray()
    );

    UnsignedByteArray naturalBytes = privateKey.naturalBytes();
    UnsignedLong amount = decryptor.decrypt(
      ciphertext,
      privateKey.naturalBytes(),
      minAmount,
      maxAmount
    );

    naturalBytes.destroy();
    return amount;
  }
}

