package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;

/**
 * Represents a signer for a multi-signature XRPL Transaction for purposes of deserializing response from an xrpld
 * server.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSigner.class)
@JsonDeserialize(as = ImmutableSigner.class)
public interface Signer {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSigner.Builder}.
   */
  static ImmutableSigner.Builder builder() {
    return ImmutableSigner.builder();
  }

  /**
   * The {@link Address} associated with this signature, as it appears in the signer list.
   *
   * @return The {@link Address} of the signer account.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * A signature for a transaction, verifiable using the {@link Signer#signingPublicKey()}.
   *
   * @return A {@link String} containing the transaction signature.
   */
  @JsonProperty("TxnSignature")
  Signature transactionSignature();

  /**
   * The public key used to create this signature.
   *
   * @return A {@link String} containing the public key used to sign the transaction.
   */
  @JsonProperty("SigningPubKey")
  PublicKey signingPublicKey();

}
