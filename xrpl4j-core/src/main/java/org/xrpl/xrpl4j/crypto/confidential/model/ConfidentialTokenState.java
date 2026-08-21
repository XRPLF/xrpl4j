package org.xrpl.xrpl4j.crypto.confidential.model;

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

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Optional;

/**
 * The confidential state of one {@code (account, token)} MPToken — the encrypted balances, version, and holder key that
 * a Confidential MPT proof binds to. Used by the Batch assembler as both the initial (fetched) state of a referenced
 * MPToken and the predicted state threaded through chained operations.
 *
 * <p>An absent balance ({@link Optional#empty()}) means the value is unavailable — either never present on-ledger, or
 * reset to a canonical encrypted zero by an earlier MergeInbox/Clawback in the same Batch (a value the client cannot
 * reproduce). Reading such a balance for a subsequent proof must fail rather than emit a proof the ledger would
 * reject.</p>
 */
@Value.Immutable
public interface ConfidentialTokenState {

  /**
   * Builder for constructing {@link ConfidentialTokenState}.
   *
   * @return An {@link ImmutableConfidentialTokenState.Builder}.
   */
  static ImmutableConfidentialTokenState.Builder builder() {
    return ImmutableConfidentialTokenState.builder();
  }

  /**
   * The holder's directly-spendable confidential balance ciphertext.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> spending();

  /**
   * The holder's pending (received/converted) inbox balance ciphertext.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> inbox();

  /**
   * The issuer's encrypted mirror balance for this holder's tokens.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> issuerEncrypted();

  /**
   * The auditor's encrypted balance for this holder's tokens.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> auditorEncrypted();

  /**
   * The confidential balance version. Bound by Send/ConvertBack proofs and incremented by each balance-mutating
   * operation.
   *
   * @return An {@link UnsignedInteger}; defaults to {@link UnsignedInteger#ZERO}.
   */
  @Value.Default
  default UnsignedInteger version() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The holder's registered ElGamal encryption key, if any. Required as a Send destination's key.
   *
   * @return An optionally-present {@link PublicKey}.
   */
  Optional<PublicKey> holderKey();
}
