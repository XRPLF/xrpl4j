package org.xrpl.xrpl4j.crypto.confidential;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * A {@link ConfidentialMptOp} in which the issuer claws back a confidential amount from a holder. The submitting
 * {@link #account()} is the issuer; the proof binds the holder's issuer-encrypted balance.
 */
@Value.Immutable
public interface ConfidentialClawbackOp extends ConfidentialMptOp {

  /**
   * Builder for constructing {@link ConfidentialClawbackOp}.
   *
   * @return An {@link ImmutableConfidentialClawbackOp.Builder}.
   */
  static ImmutableConfidentialClawbackOp.Builder builder() {
    return ImmutableConfidentialClawbackOp.builder();
  }

  /**
   * The holder whose confidential balance is clawed back.
   *
   * @return The holder {@link Address}.
   */
  Address holder();

  /**
   * The amount to claw back.
   *
   * @return An {@link UnsignedLong}.
   */
  UnsignedLong amount();

  /**
   * The issuer's ElGamal keypair, used to decrypt the mirror balance and prove the clawback.
   *
   * @return A {@link KeyPair}.
   */
  KeyPair issuerKeyPair();
}
