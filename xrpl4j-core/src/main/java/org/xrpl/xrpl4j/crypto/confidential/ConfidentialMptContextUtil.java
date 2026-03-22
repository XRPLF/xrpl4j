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

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
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
public final class ConfidentialMptContextUtil {

  /**
   * Transaction type for ConfidentialMptConvert (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CONVERT = 85;

  /**
   * Transaction type for ConfidentialMptConvertBack (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CONVERT_BACK = 87;

  /**
   * Transaction type for ConfidentialMptSend (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_SEND = 88;

  /**
   * Transaction type for ConfidentialMptClawback (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CLAWBACK = 89;

  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private ConfidentialMptContextUtil() {
    // Utility class
  }

  /**
   * Generates a context hash for a ConfidentialMptConvert transaction.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CONVERT (85)</li>
   *   <li>account (20 bytes) - sender account</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>account (20 bytes) - identity (self)</li>
   *   <li>freshness (4 bytes) - always 0</li>
   * </ul>
   *
   * @param account    The account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   *
   * @return A {@link ConfidentialMptConvertContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMptConvertContext generateConvertContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");

    UnsignedByteArray accountBytes = ADDRESS_CODEC.decodeAccountId(account);
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());

    // Total: 2 (txType) + 20 (account) + 24 (issuanceId) + 4 (sequence) + 20 (account) + 4 (freshness) = 74 bytes
    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CONVERT);
    buffer.put(accountBytes.toByteArray());
    buffer.put(issuanceIdBytes);
    buffer.putInt(sequence.intValue());
    buffer.put(accountBytes.toByteArray()); // identity (self)
    buffer.putInt(0); // freshness (always 0)

    UnsignedByteArray hash = HashingUtils.sha512Half(buffer.array());

    return ConfidentialMptConvertContext.of(hash);
  }

  /**
   * Generates a context hash for a ConfidentialMptSend transaction.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_SEND (88)</li>
   *   <li>account (20 bytes) - sender account</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>destination (20 bytes) - identity</li>
   *   <li>version (4 bytes) - freshness (balance version counter)</li>
   * </ul>
   *
   * @param account     The sender account address.
   * @param sequence    The transaction sequence number.
   * @param issuanceId  The MPTokenIssuanceId (24 bytes as hex string).
   * @param destination The destination account address.
   * @param version     The confidential balance version from the MPToken ledger object.
   *
   * @return A {@link ConfidentialMptSendContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMptSendContext generateSendContext(
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

    // Total: 2 + 20 + 24 + 4 + 20 + 4 = 74 bytes
    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.putShort((short) TT_CONFIDENTIAL_MPT_SEND);
    buffer.put(ADDRESS_CODEC.decodeAccountId(account).toByteArray());
    buffer.put(BaseEncoding.base16().decode(issuanceId.value().toUpperCase()));
    buffer.putInt(sequence.intValue());
    buffer.put(ADDRESS_CODEC.decodeAccountId(destination).toByteArray());
    buffer.putInt(version.intValue());

    UnsignedByteArray hash = HashingUtils.sha512Half(buffer.array());
    return ConfidentialMptSendContext.of(hash);
  }

  /**
   * Generates a context hash for a ConfidentialMptConvertBack transaction.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CONVERT_BACK (87)</li>
   *   <li>account (20 bytes) - sender account</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>account (20 bytes) - identity (self)</li>
   *   <li>version (4 bytes) - freshness (balance version counter)</li>
   * </ul>
   *
   * @param account    The account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   * @param version    The confidential balance version from the MPToken ledger object.
   *
   * @return A {@link ConfidentialMptConvertBackContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMptConvertBackContext generateConvertBackContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedInteger version
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(version, "version must not be null");

    UnsignedByteArray accountBytes = ADDRESS_CODEC.decodeAccountId(account);
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());

    // Total: 2 (txType) + 20 (account) + 24 (issuanceId) + 4 (sequence) + 20 (account) + 4 (version) = 74 bytes
    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CONVERT_BACK);
    buffer.put(accountBytes.toByteArray());
    buffer.put(issuanceIdBytes);
    buffer.putInt(sequence.intValue());
    buffer.put(accountBytes.toByteArray()); // identity (self)
    buffer.putInt(version.intValue());

    UnsignedByteArray hash = HashingUtils.sha512Half(buffer.array());

    return ConfidentialMptConvertBackContext.of(hash);
  }

  /**
   * Generates a context hash for a ConfidentialMptClawback transaction.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CLAWBACK (89)</li>
   *   <li>account (20 bytes) - issuer account</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>holder (20 bytes) - identity (holder account)</li>
   *   <li>freshness (4 bytes) - always 0</li>
   * </ul>
   *
   * @param account    The issuer's account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   * @param holder     The holder account from which tokens are being clawed back.
   *
   * @return A {@link ConfidentialMptClawbackContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMptClawbackContext generateClawbackContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final Address holder
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(holder, "holder must not be null");

    // Total: 2 (txType) + 20 (account) + 24 (issuanceId) + 4 (sequence) + 20 (holder) + 4 (freshness) = 74 bytes
    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CLAWBACK);
    buffer.put(ADDRESS_CODEC.decodeAccountId(account).toByteArray());
    buffer.put(BaseEncoding.base16().decode(issuanceId.value().toUpperCase()));
    buffer.putInt(sequence.intValue());
    buffer.put(ADDRESS_CODEC.decodeAccountId(holder).toByteArray()); // identity
    buffer.putInt(0); // freshness (always 0)

    UnsignedByteArray hash = HashingUtils.sha512Half(buffer.array());

    return ConfidentialMptClawbackContext.of(hash);
  }
}

