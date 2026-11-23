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

/**
 * A {@link ConfidentialMptOp} that converts a confidential amount back to a public MPT balance, debiting the holder's
 * spendable balance.
 */
@Value.Immutable
public interface ConfidentialConvertBackOp extends ConfidentialMptOp {

  /**
   * Builder for constructing {@link ConfidentialConvertBackOp}.
   *
   * @return An {@link ImmutableConfidentialConvertBackOp.Builder}.
   */
  static ImmutableConfidentialConvertBackOp.Builder builder() {
    return ImmutableConfidentialConvertBackOp.builder();
  }

  /**
   * The confidential amount to reveal back to the public MPT balance.
   *
   * @return An {@link UnsignedLong}.
   */
  UnsignedLong amount();

  /**
   * The holder's ElGamal keypair, used to decrypt the current balance and prove the convert-back.
   *
   * @return A {@link KeyPair}.
   */
  KeyPair holderKeyPair();
}
