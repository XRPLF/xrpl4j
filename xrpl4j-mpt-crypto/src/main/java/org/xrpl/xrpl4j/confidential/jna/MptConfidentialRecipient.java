package org.xrpl.xrpl4j.confidential.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: mpt-crypto
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

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA mapping for the C struct {@code mpt_confidential_participant}.
 *
 * <pre>
 * struct mpt_confidential_participant {
 *     uint8_t pubkey[33];
 *     uint8_t ciphertext[66];
 * };
 * </pre>
 */
@Structure.FieldOrder({"pubkey", "ciphertext"})
public class MptConfidentialRecipient extends Structure {

  /** The 33-byte compressed secp256k1 public key. */
  public byte[] pubkey = new byte[33];

  /** The 66-byte ElGamal ciphertext (C1 || C2). */
  public byte[] ciphertext = new byte[66];

  /** Default constructor required by JNA. */
  public MptConfidentialRecipient() {
  }
}
