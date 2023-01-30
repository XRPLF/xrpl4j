package org.xrpl.xrpl4j.crypto.signing;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * A container object for a {@link Signature} and a corresponding {@link PublicKey}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignatureWithPublicKey.class)
@JsonDeserialize(as = ImmutableSignatureWithPublicKey.class)
public interface SignatureWithPublicKey {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignatureWithPublicKey.Builder}.
   */
  static ImmutableSignatureWithPublicKey.Builder builder() {
    return ImmutableSignatureWithPublicKey.builder();
  }

  /**
   * A signature for a transaction, verifiable using the {@link SignatureWithPublicKey#signingPublicKey()}.
   *
   * @return A {@link Signature} containing the transaction signature.
   */
  Signature transactionSignature();

  /**
   * The public key used to create this signature.
   *
   * @return A {@link PublicKey} containing the public key used to sign the transaction.
   */
  PublicKey signingPublicKey();

}
