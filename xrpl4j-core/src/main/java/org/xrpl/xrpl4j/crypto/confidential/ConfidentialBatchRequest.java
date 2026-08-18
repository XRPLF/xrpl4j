package org.xrpl.xrpl4j.crypto.confidential;

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

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialIssuanceInfo;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialTokenState;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.BatchFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The inputs to {@link ConfidentialMptBatchAssembler#assemble(ConfidentialBatchRequest)}: the outer Batch account, the
 * ordered confidential operations, and the pre-fetched ledger state the assembler needs to build each inner's proof.
 *
 * <p>The assembler is client-free — it performs no I/O — so the caller supplies the ledger state it fetched
 * (per-account sequences, per-{@code (account, token)} confidential balances, and per-issuance ElGamal keys).</p>
 */
@Value.Immutable
public interface ConfidentialBatchRequest {

  /**
   * Builder for constructing {@link ConfidentialBatchRequest}.
   *
   * @return An {@link ImmutableConfidentialBatchRequest.Builder}.
   */
  static ImmutableConfidentialBatchRequest.Builder builder() {
    return ImmutableConfidentialBatchRequest.builder();
  }

  /**
   * The composite key identifying one confidential MPToken's state — one {@code (holder, issuance)} pair.
   *
   * @param account The holder's classic address.
   * @param token   The MPTokenIssuanceId.
   *
   * @return The composite state key used in {@link #states()}.
   */
  static String stateKey(final Address account, final MpTokenIssuanceId token) {
    return account.value() + ":" + token.value();
  }

  /**
   * The outer Batch account's signing public key. Its derived address is the outer account and the account that pays
   * the outer fee and signs the assembled Batch.
   *
   * @return A {@link PublicKey}.
   */
  PublicKey accountPublicKey();

  /**
   * The ordered inners to place in the Batch — each either a {@link ConfidentialMptOp} the assembler builds, or a
   * pre-built plain transaction to pass through. rippled requires between 2 and 8 inner transactions.
   *
   * @return An ordered {@link List} of {@link ConfidentialBatchInner}.
   */
  List<ConfidentialBatchInner> inners();

  /**
   * The current on-ledger sequence of each submitting account (the outer account and every inner op's account). The
   * assembler derives each inner's position-dependent sequence from these.
   *
   * @return A {@link Map} from account {@link Address} to its current {@link UnsignedInteger} sequence.
   */
  Map<Address, UnsignedInteger> accountSequences();

  /**
   * The confidential state of each referenced MPToken, keyed by {@link #stateKey(Address, MpTokenIssuanceId)}. Must
   * include every {@code (account, token)} whose balance a proof reads: a send's sender and destination, a
   * convert-back's holder, and a clawback's holder.
   *
   * @return A {@link Map} from state key to {@link ConfidentialTokenState}.
   */
  Map<String, ConfidentialTokenState> states();

  /**
   * The confidential parameters of each referenced issuance (issuer/auditor keys and the outstanding bound), keyed by
   * {@link MpTokenIssuanceId}.
   *
   * @return A {@link Map} from {@link MpTokenIssuanceId} to {@link ConfidentialIssuanceInfo}.
   */
  Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> issuances();

  /**
   * The fee for the outer Batch transaction.
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  XrpCurrencyAmount outerFee();

  /**
   * The outer Batch mode flags. When absent, the assembled {@link org.xrpl.xrpl4j.model.transactions.Batch} uses its
   * default {@code tfAllOrNothing}.
   *
   * @return An optionally-present {@link BatchFlags}.
   */
  Optional<BatchFlags> batchFlags();
}
