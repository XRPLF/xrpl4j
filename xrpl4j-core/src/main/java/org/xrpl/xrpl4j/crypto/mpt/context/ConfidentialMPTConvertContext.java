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
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the context hash for a ConfidentialMPTConvert transaction.
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
 * <p>This context is used in {@link SecretKeyProofGenerator} to bind the proof to a specific transaction.</p>
 */
public final class ConfidentialMPTConvertContext {

  /**
   * The length of the context hash in bytes.
   */
  public static final int CONTEXT_LENGTH = 32;

  /**
   * Transaction type for ConfidentialMPTConvert (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CONVERT = 85;

  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private final byte[] contextHash;

  private ConfidentialMPTConvertContext(final byte[] contextHash) {
    this.contextHash = Arrays.copyOf(contextHash, contextHash.length);
  }

  /**
   * Generates a context hash for a ConfidentialMPTConvert transaction.
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
  public static ConfidentialMPTConvertContext generate(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedLong amount
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");

    // Serialize fields matching rippled's addCommonZKPFields + add64(amount)
    // Total: 2 (txType) + 20 (account) + 4 (sequence) + 24 (issuanceId) + 8 (amount) = 58 bytes
    ByteBuffer buffer = ByteBuffer.allocate(58);
    buffer.order(ByteOrder.BIG_ENDIAN);

    // 1. add16(txType) - 2 bytes big-endian
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CONVERT);

    // 2. addBitString(account) - 20 bytes raw
    buffer.position(2);
    UnsignedByteArray accountBytes = ADDRESS_CODEC.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());

    // 3. add32(sequence) - 4 bytes big-endian
    buffer.position(22);
    buffer.putInt(sequence.intValue());

    // 4. addBitString(issuanceID) - 24 bytes raw
    buffer.position(26);
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);

    // 5. add64(amount) - 8 bytes big-endian
    buffer.position(50);
    buffer.putLong(amount.longValue());

    // Compute SHA512Half (first 32 bytes of SHA512)
    byte[] hash = sha512Half(buffer.array());

    return new ConfidentialMPTConvertContext(hash);
  }

  /**
   * Creates a context from raw bytes.
   *
   * @param bytes The 32-byte context hash.
   *
   * @return A {@link ConfidentialMPTConvertContext}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 32 bytes.
   */
  public static ConfidentialMPTConvertContext fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == CONTEXT_LENGTH,
      "Context hash must be %s bytes, but was %s bytes",
      CONTEXT_LENGTH, bytes.length
    );
    return new ConfidentialMPTConvertContext(bytes);
  }

  /**
   * Returns the context hash as a byte array.
   *
   * @return A copy of the 32-byte context hash.
   */
  public byte[] toBytes() {
    return Arrays.copyOf(contextHash, contextHash.length);
  }

  /**
   * Returns the context hash as an uppercase hex string.
   *
   * @return A 64-character uppercase hex string.
   */
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
    ConfidentialMPTConvertContext that = (ConfidentialMPTConvertContext) obj;
    return Arrays.equals(contextHash, that.contextHash);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(contextHash);
  }

  @Override
  public String toString() {
    return "ConfidentialMPTConvertContext{hash=" + hexValue() + "}";
  }

  /**
   * Computes SHA512Half (first 32 bytes of SHA512).
   *
   * @param data The data to hash.
   *
   * @return The first 32 bytes of the SHA512 hash.
   */
  private static byte[] sha512Half(final byte[] data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512");
      byte[] fullHash = digest.digest(data);
      return Arrays.copyOf(fullHash, 32);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-512 not available", e);
    }
  }
}
