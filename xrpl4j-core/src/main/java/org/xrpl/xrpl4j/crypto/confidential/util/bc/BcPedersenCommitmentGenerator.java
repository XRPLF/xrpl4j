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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.commitment.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.commitment.bc.BcPedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.util.PedersenCommitmentGenerator;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link PedersenCommitmentGenerator}.
 *
 * <p>This implementation delegates to {@link BcPedersenCommitment} for the actual
 * cryptographic operations.</p>
 */
public class BcPedersenCommitmentGenerator implements PedersenCommitmentGenerator {

  private final PedersenCommitment port;

  /**
   * Creates a new instance with the default port implementation.
   */
  public BcPedersenCommitmentGenerator() {
    this(new BcPedersenCommitment());
  }

  /**
   * Creates a new instance with a custom port implementation.
   *
   * @param port The port implementation to use.
   */
  public BcPedersenCommitmentGenerator(final PedersenCommitment port) {
    this.port = Objects.requireNonNull(port, "port must not be null");
  }

  @Override
  public org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment generateCommitment(
    final UnsignedLong amount,
    final BlindingFactor blindingFactor
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    UnsignedByteArray rho = UnsignedByteArray.of(blindingFactor.toBytes());
    UnsignedByteArray commitment = port.generateCommitment(amount, rho);

    return org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment.of(commitment);
  }
}

