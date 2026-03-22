package org.xrpl.xrpl4j.crypto.confidential.util.bc;

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
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.elgamal.bc.BcElGamalDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link MptAmountDecryptor}.
 *
 * <p>This implementation delegates to {@link BcElGamalDecryptor}
 * and validates inputs using Java-friendly types.</p>
 *
 * <p>Mirrors the C utility function {@code mpt_decrypt_amount} from mpt_utility.cpp.</p>
 */
public class BcMptAmountDecryptor implements MptAmountDecryptor {

  private final BcElGamalDecryptor decryptor;

  /**
   * Constructs a new instance with a new {@link BcElGamalDecryptor}.
   */
  public BcMptAmountDecryptor() {
    this(new BcElGamalDecryptor());
  }

  /**
   * Constructs a new instance with the specified {@link BcElGamalDecryptor}.
   *
   * @param decryptor The port decryptor to delegate to.
   */
  public BcMptAmountDecryptor(final BcElGamalDecryptor decryptor) {
    this.decryptor = Objects.requireNonNull(decryptor);
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

