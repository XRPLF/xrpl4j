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

import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Optional;

/**
 * The confidential parameters of an MPTokenIssuance needed to build proofs against it: the issuer's and (optional)
 * auditor's ElGamal encryption keys, plus an upper bound on any holder's confidential balance for the bounded
 * decryption a range proof requires.
 */
@Value.Immutable
public interface ConfidentialIssuanceInfo {

  /**
   * Builder for constructing {@link ConfidentialIssuanceInfo}.
   *
   * @return An {@link ImmutableConfidentialIssuanceInfo.Builder}.
   */
  static ImmutableConfidentialIssuanceInfo.Builder builder() {
    return ImmutableConfidentialIssuanceInfo.builder();
  }

  /**
   * The issuer's ElGamal encryption key registered on the issuance.
   *
   * @return A {@link PublicKey}.
   */
  PublicKey issuerEncryptionKey();

  /**
   * The auditor's ElGamal encryption key, if the issuance registered one.
   *
   * @return An optionally-present {@link PublicKey}.
   */
  Optional<PublicKey> auditorEncryptionKey();

  /**
   * The issuance's pre-batch confidential outstanding amount — an upper bound on any single holder's confidential
   * balance — used as the maximum for the bounded decryption performed while building Send/ConvertBack range proofs.
   * The assembler widens this by any amount converted for the same token earlier in the same Batch, so a balance
   * topped up by an in-batch Convert stays decryptable.
   *
   * @return An {@link UnsignedLong}.
   */
  UnsignedLong outstandingAmount();
}
