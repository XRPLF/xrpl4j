package org.xrpl.xrpl4j.crypto.mpt.context;

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

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Utility class for generating context hashes for Confidential MPT transactions.
 *
 * <p>Context hashes bind proofs to specific transactions, preventing replay attacks.
 * Each transaction type has its own context generation method.</p>
 */
public final class ConfidentialMPTContextUtil {

  /**
   * Transaction type for ConfidentialMPTConvert (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CONVERT = 85;

  /**
   * Transaction type for ConfidentialMPTSend (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_SEND = 88;

  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private ConfidentialMPTContextUtil() {
    // Utility class
  }

  /**
   * Generates a context hash for a ConfidentialMPTConvert transaction.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CONVERT (85)</li>
   *   <li>account (20 bytes) - sender account</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>amount (8 bytes) - amount being converted</li>
   * </ul>
   *
   * @param account    The account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   * @param amount     The amount being converted.
   *
   * @return A {@link ConfidentialMPTConvertContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMPTConvertContext generateConvertContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedLong amount
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");

    // Total: 2 (txType) + 20 (account) + 4 (sequence) + 24 (issuanceId) + 8 (amount) = 58 bytes
    ByteBuffer buffer = ByteBuffer.allocate(58);
    buffer.order(ByteOrder.BIG_ENDIAN);

    // 1. add16(txType)
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CONVERT);

    // 2. addBitString(account)
    UnsignedByteArray accountBytes = ADDRESS_CODEC.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());

    // 3. add32(sequence)
    buffer.putInt(sequence.intValue());

    // 4. addBitString(issuanceID)
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);

    // 5. add64(amount)
    buffer.putLong(amount.longValue());

    // Compute SHA512Half
    UnsignedByteArray hash = HashingUtils.sha512Half(buffer.array());

    return ConfidentialMPTConvertContext.of(hash);
  }

  /**
   * Generates a context hash for a ConfidentialMPTSend transaction.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_SEND (88)</li>
   *   <li>account (20 bytes) - sender account</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>destination (20 bytes) - destination account</li>
   *   <li>version (4 bytes) - confidential balance version</li>
   * </ul>
   *
   * @param account     The sender account address.
   * @param sequence    The transaction sequence number.
   * @param issuanceId  The MPTokenIssuanceId (24 bytes as hex string).
   * @param destination The destination account address.
   * @param version     The confidential balance version from the MPToken ledger object.
   *
   * @return A {@link ConfidentialMPTSendContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMPTSendContext generateSendContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final Address destination,
    final UnsignedInteger version
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(destination, "destination must not be null");
    Objects.requireNonNull(version, "version must not be null");

    // Total: 2 + 20 + 4 + 24 + 20 + 4 = 74 bytes
    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.putShort((short) TT_CONFIDENTIAL_MPT_SEND);
    buffer.put(ADDRESS_CODEC.decodeAccountId(account).toByteArray());
    buffer.putInt(sequence.intValue());
    buffer.put(BaseEncoding.base16().decode(issuanceId.value().toUpperCase()));
    buffer.put(ADDRESS_CODEC.decodeAccountId(destination).toByteArray());
    buffer.putInt(version.intValue());

    UnsignedByteArray hash = HashingUtils.sha512Half(buffer.array());
    return ConfidentialMPTSendContext.of(hash);
  }
}

