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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.util.PedersenCommitmentGenerator;

import java.util.Objects;

/**
 * Implementation of {@link PedersenCommitmentGenerator} that delegates to the native mpt-crypto
 * C library via {@link MptCryptoLibrary}.
 *
 * <p>This class calls {@code mpt_get_pedersen_commitment} from the native library to compute
 * a Pedersen Commitment: C = amount * G + blindingFactor * H.</p>
 */
public class JnaPedersenCommitmentGenerator implements PedersenCommitmentGenerator {

  private static final int COMMITMENT_SIZE = 33;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaPedersenCommitmentGenerator() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaPedersenCommitmentGenerator(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public PedersenCommitment generateCommitment(final UnsignedLong amount, final BlindingFactor blindingFactor) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    byte[] outCommitment = new byte[COMMITMENT_SIZE];
    int result = lib.mpt_get_pedersen_commitment(
      amount.longValue(), blindingFactor.toBytes(), outCommitment
    );
    if (result != 0) {
      throw new IllegalStateException("mpt_get_pedersen_commitment failed with error code: " + result);
    }
    return PedersenCommitment.of(UnsignedByteArray.of(outCommitment));
  }
}
