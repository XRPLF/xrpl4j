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

import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

/**
 * A {@link ConfidentialMptOp} that converts a public MPT amount into a confidential balance (crediting the holder's
 * inbox) and registers the holder's ElGamal encryption key.
 */
@Value.Immutable
public interface ConfidentialConvertOp extends ConfidentialMptOp {

  /**
   * Builder for constructing {@link ConfidentialConvertOp}.
   *
   * @return An {@link ImmutableConfidentialConvertOp.Builder}.
   */
  static ImmutableConfidentialConvertOp.Builder builder() {
    return ImmutableConfidentialConvertOp.builder();
  }

  /**
   * The public MPT amount to convert into confidential form.
   *
   * @return An {@link UnsignedLong}.
   */
  UnsignedLong amount();

  /**
   * The holder's ElGamal keypair (the account's own confidential key), whose public key is registered on the MPToken.
   *
   * @return A {@link KeyPair}.
   */
  KeyPair holderKeyPair();

  /**
   * Whether to register the holder's ElGamal encryption key (attaching {@code HolderEncryptionKey} and its Schnorr
   * ownership proof) on this Convert.
   *
   * <p>{@code rippled} requires the key on a holder's <em>first</em> Convert (otherwise {@code tecNO_PERMISSION}) and
   * rejects it as {@code tecDUPLICATE} on every later one. Because this assembler is client-free and cannot read the
   * ledger to tell which case applies, the caller declares intent here: {@code true} (the default) for a holder's
   * first Convert, {@code false} for a top-up Convert on an already-registered holder.</p>
   *
   * @return {@code true} to register the holder key (and emit its proof) on this Convert; defaults to {@code true}.
   */
  @Value.Default
  default boolean registerKey() {
    return true;
  }
}
