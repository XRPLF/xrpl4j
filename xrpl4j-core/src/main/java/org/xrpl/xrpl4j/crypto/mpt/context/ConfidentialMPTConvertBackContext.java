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

import com.google.common.base.Preconditions;
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
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the context hash for a ConfidentialMPTConvertBack transaction.
 *
 * <p>The context hash is computed as SHA512Half of:
 * <ul>
 *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CONVERT_BACK (87)</li>
 *   <li>account (20 bytes) - sender account</li>
 *   <li>sequence (4 bytes) - transaction sequence</li>
 *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
 *   <li>amount (8 bytes) - amount being converted back</li>
 *   <li>version (4 bytes) - confidential balance version</li>
 * </ul>
 *
 * <p>This context is used in {@link org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ElGamalPedersenLinkProofGenerator}
 * to bind the proof to a specific transaction.</p>
 */
public final class ConfidentialMPTConvertBackContext implements LinkProofContext {

  /**
   * Transaction type for ConfidentialMPTConvertBack (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CONVERT_BACK = 87;

  private static final AddressCodec ADDRESS_CODEC = AddressCodec.getInstance();

  private final byte[] contextHash;

  private ConfidentialMPTConvertBackContext(final byte[] contextHash) {
    this.contextHash = Arrays.copyOf(contextHash, contextHash.length);
  }

  /**
   * Generates a context hash for a ConfidentialMPTConvertBack transaction.
   *
   * @param account    The account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   * @param amount     The amount being converted back.
   * @param version    The confidential balance version from the MPToken ledger object.
   *
   * @return A {@link ConfidentialMPTConvertBackContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMPTConvertBackContext generate(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedLong amount,
    final UnsignedInteger version
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(version, "version must not be null");

    // Context = SHA512Half(txType || account || sequence || issuanceId || amount || version)
    // txType (2) + account (20) + sequence (4) + issuanceId (24) + amount (8) + version (4) = 62 bytes
    ByteBuffer buffer = ByteBuffer.allocate(62);
    buffer.order(ByteOrder.BIG_ENDIAN);

    // 1. add16(txType) - 2 bytes big-endian
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CONVERT_BACK);

    // 2. addBitString(account) - 20 bytes raw
    UnsignedByteArray accountBytes = ADDRESS_CODEC.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());

    // 3. add32(sequence) - 4 bytes big-endian
    buffer.putInt(sequence.intValue());

    // 4. addBitString(issuanceID) - 24 bytes raw
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);

    // 5. add64(amount) - 8 bytes big-endian
    buffer.putLong(amount.longValue());

    // 6. add32(version) - 4 bytes big-endian
    buffer.putInt(version.intValue());

    // Compute SHA512Half (first 32 bytes of SHA512)
    byte[] hash = HashingUtils.sha512Half(buffer.array()).toByteArray();

    return new ConfidentialMPTConvertBackContext(hash);
  }

  /**
   * Creates a context from raw bytes.
   *
   * @param bytes The 32-byte context hash.
   *
   * @return A {@link ConfidentialMPTConvertBackContext}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 32 bytes.
   */
  public static ConfidentialMPTConvertBackContext fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == CONTEXT_LENGTH,
      "Context hash must be %s bytes, but was %s bytes",
      CONTEXT_LENGTH, bytes.length
    );
    return new ConfidentialMPTConvertBackContext(bytes);
  }

  @Override
  public byte[] toBytes() {
    return Arrays.copyOf(contextHash, contextHash.length);
  }

  @Override
  public String hexValue() {
    return BaseEncoding.base16().encode(contextHash);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ConfidentialMPTConvertBackContext that = (ConfidentialMPTConvertBackContext) obj;
    return Arrays.equals(contextHash, that.contextHash);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(contextHash);
  }

  @Override
  public String toString() {
    return "ConfidentialMPTConvertBackContext{hash=" + hexValue() + "}";
  }
}

