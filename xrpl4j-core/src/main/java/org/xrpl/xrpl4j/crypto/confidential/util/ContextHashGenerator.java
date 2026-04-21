package org.xrpl.xrpl4j.crypto.confidential.util;

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
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Generates transaction-specific context hashes for Confidential MPT transactions.
 *
 * <p>Context hashes bind zero-knowledge proofs to specific transactions, preventing replay attacks.
 * Each transaction type has its own context hash format as defined in mpt_utility.h.</p>
 */
public interface ContextHashGenerator {

  /**
   * Generates a context hash for a ConfidentialMPTConvert transaction.
   *
   * @param account    The holder's account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId.
   *
   * @return A {@link ConfidentialMptConvertContext} containing the 32-byte context hash.
   */
  ConfidentialMptConvertContext generateConvertContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId
  );

  /**
   * Generates a context hash for a ConfidentialMPTConvertBack transaction.
   *
   * @param account    The holder's account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId.
   * @param version    The confidential balance version from the MPToken ledger object.
   *
   * @return A {@link ConfidentialMptConvertBackContext} containing the 32-byte context hash.
   */
  ConfidentialMptConvertBackContext generateConvertBackContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    UnsignedInteger version
  );

  /**
   * Generates a context hash for a ConfidentialMPTSend transaction.
   *
   * @param account     The sender's account address.
   * @param sequence    The transaction sequence number.
   * @param issuanceId  The MPTokenIssuanceId.
   * @param destination The destination account address.
   * @param version     The confidential balance version from the MPToken ledger object.
   *
   * @return A {@link ConfidentialMptSendContext} containing the 32-byte context hash.
   */
  ConfidentialMptSendContext generateSendContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    Address destination,
    UnsignedInteger version
  );

  /**
   * Generates a context hash for a ConfidentialMPTClawback transaction.
   *
   * @param account    The issuer's account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId.
   * @param holder     The holder account being clawed back from.
   *
   * @return A {@link ConfidentialMptClawbackContext} containing the 32-byte context hash.
   */
  ConfidentialMptClawbackContext generateClawbackContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    Address holder
  );
}
