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
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * A {@link ConfidentialMptOp} that sends a confidential amount from the sender's spendable balance to a destination's
 * inbox.
 */
@Value.Immutable
public interface ConfidentialSendOp extends ConfidentialMptOp {

  /**
   * Builder for constructing {@link ConfidentialSendOp}.
   *
   * @return An {@link ImmutableConfidentialSendOp.Builder}.
   */
  static ImmutableConfidentialSendOp.Builder builder() {
    return ImmutableConfidentialSendOp.builder();
  }

  /**
   * The destination that receives the confidential amount into its inbox.
   *
   * @return The destination {@link Address}.
   */
  Address destination();

  /**
   * The amount to send.
   *
   * @return An {@link UnsignedLong}.
   */
  UnsignedLong amount();

  /**
   * The sender's ElGamal keypair, used to encrypt to the sender, decrypt the current balance, and prove the send.
   *
   * @return A {@link KeyPair}.
   */
  KeyPair senderKeyPair();
}
