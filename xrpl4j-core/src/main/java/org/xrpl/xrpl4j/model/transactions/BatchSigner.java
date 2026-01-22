package org.xrpl.xrpl4j.model.transactions;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;

import java.util.List;
import java.util.Optional;

/**
 * Represents a signer in the {@code BatchSigners} array of a {@link Batch} transaction.
 * A BatchSigner can either sign directly with a public key and signature, or use multi-signing
 * with a nested {@code Signers} array.
 *
 * <p>This class will be marked {@link Beta} until the featureBatch amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBatchSigner.class)
@JsonDeserialize(as = ImmutableBatchSigner.class)
@Beta
public interface BatchSigner {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableBatchSigner.Builder}.
   */
  static ImmutableBatchSigner.Builder builder() {
    return ImmutableBatchSigner.builder();
  }

  /**
   * The account address of the signer. This must match either the outer transaction's Account
   * or one of the inner transaction accounts.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The public key used to create the signature. Present for direct signing.
   *
   * @return An optionally-present {@link PublicKey}.
   */
  @JsonProperty("SigningPubKey")
  Optional<PublicKey> signingPublicKey();

  /**
   * The signature for the transaction. Present for direct signing.
   *
   * @return An optionally-present {@link Signature}.
   */
  @JsonProperty("TxnSignature")
  Optional<Signature> transactionSignature();

  /**
   * The array of signers for multi-signing. Present when using multi-sig instead of direct signing.
   *
   * @return A {@link List} of {@link SignerWrapper}s.
   */
  @JsonProperty("Signers")
  List<SignerWrapper> signers();

  /**
   * Validates that the BatchSigner has either direct signing fields or multi-sig signers, but not both.
   */
  @Value.Check
  default void check() {
    boolean hasDirectSigning = signingPublicKey().isPresent() && transactionSignature().isPresent();
    boolean hasMultiSig = !signers().isEmpty();

    Preconditions.checkState(
      hasDirectSigning || hasMultiSig,
      "BatchSigner must have either (SigningPubKey and TxnSignature) or non-empty Signers array"
    );

    Preconditions.checkState(
      !(hasDirectSigning && hasMultiSig),
      "BatchSigner cannot have both direct signing fields and Signers array"
    );

    if (hasDirectSigning) {
      Preconditions.checkState(
        signingPublicKey().isPresent() && transactionSignature().isPresent(),
        "Direct signing requires both SigningPubKey and TxnSignature"
      );
    }
  }

}
