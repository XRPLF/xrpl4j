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
 * MPToken and the predicted state threaded through chained operations. Each accessor mirrors a field on the on-ledger
 * {@code MPToken} entry (see {@link org.xrpl.xrpl4j.model.ledger.MpTokenObject}).
 *
 * <p>Every balance and the holder key is optional in the protocol (each is a {@code SoeOptional} field on the MPToken
 * entry), so any of them being {@link Optional#empty()} is legal on-ledger. A field is <em>absent</em> when it has
 * never been set: a freshly created MPToken carries none of them, so an all-absent state (with {@link #version()} at
 * its default zero) is the legal initial state of a token that has not yet taken part in any confidential operation.
 * Absence also arises within a Batch when an earlier MergeInbox/Clawback resets a balance to a canonical encrypted zero
 * the client cannot reproduce, so the predicted state records it as absent rather than as a wrong ciphertext.</p>
 *
 * <p>Whether an absent field is usable depends on the operation, mirroring rippled: an operation that must read a
 * balance fails when it is absent (the assembler throws rather than emit a proof the ledger would reject), while an
 * operation crediting a not-yet-initialized balance treats absent as zero. See each accessor for its specific rule.</p>
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
   * The holder's directly-spendable confidential balance ciphertext. Absent until the holder's first
   * ConfidentialMptConvert initializes it, and after a MergeInbox/Clawback reset.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> spending();

  /**
   * The holder's pending (received/converted) inbox balance ciphertext. Absent when there are no pending receipts, and
   * after a MergeInbox/Clawback reset.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> inbox();

  /**
   * The issuer's encrypted mirror balance for this holder's tokens. Absent until the holder's first
   * ConfidentialMptConvert initializes it, and after a Clawback reset.
   *
   * @return An optionally-present {@link EncryptedAmount}.
   */
  Optional<EncryptedAmount> issuerEncrypted();

  /**
   * The auditor's encrypted balance for this holder's tokens. Present only when the token's issuance defines an auditor
   * key; otherwise always absent.
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
   * The holder's registered ElGamal encryption key, if any. Absent until the holder registers one via their first
   * ConfidentialMptConvert; required as a Send destination's key (a Send to a holder that has not registered fails).
   *
   * @return An optionally-present {@link PublicKey}.
   */
  Optional<PublicKey> holderKey();
}
