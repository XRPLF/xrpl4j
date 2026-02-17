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
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the context hash for a ConfidentialMPTClawback transaction.
 *
 * <p>The context hash is computed as SHA512Half of:
 * <ul>
 *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CLAWBACK (89)</li>
 *   <li>account (20 bytes) - issuer account</li>
 *   <li>sequence (4 bytes) - transaction sequence</li>
 *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
 *   <li>amount (8 bytes) - amount being clawed back</li>
 *   <li>holder (20 bytes) - holder account from which tokens are clawed back</li>
 * </ul>
 *
 * <p>This context is used in {@link org.xrpl.xrpl4j.crypto.mpt.bulletproofs.EqualityPlaintextProofGenerator}
 * to bind the proof to a specific clawback transaction.</p>
 */
public final class ConfidentialMPTClawbackContext {

  /**
   * The length of the context hash in bytes.
   */
  public static final int CONTEXT_LENGTH = 32;

  /**
   * Transaction type for ConfidentialMPTClawback (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CLAWBACK = 89;

  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private final byte[] contextHash;

  private ConfidentialMPTClawbackContext(final byte[] contextHash) {
    this.contextHash = Arrays.copyOf(contextHash, contextHash.length);
  }

  /**
   * Generates a context hash for a ConfidentialMPTClawback transaction.
   *
   * @param account    The issuer's account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   * @param amount     The amount being clawed back.
   * @param holder     The holder account from which tokens are being clawed back.
   *
   * @return A {@link ConfidentialMPTClawbackContext} containing the 32-byte context hash.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static ConfidentialMPTClawbackContext generate(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedLong amount,
    final Address holder
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(holder, "holder must not be null");

    // Total: 2 (txType) + 20 (account) + 4 (sequence) + 24 (issuanceId) + 8 (amount) + 20 (holder) = 78 bytes
    ByteBuffer buffer = ByteBuffer.allocate(78);
    buffer.order(ByteOrder.BIG_ENDIAN);

    // 1. add16(txType) - 2 bytes big-endian
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CLAWBACK);

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

    // 6. addBitString(holder) - 20 bytes raw
    UnsignedByteArray holderBytes = ADDRESS_CODEC.decodeAccountId(holder);
    buffer.put(holderBytes.toByteArray());

    // Compute SHA512Half (first 32 bytes of SHA512)
    byte[] hash = sha512Half(buffer.array());

    return new ConfidentialMPTClawbackContext(hash);
  }

  /**
   * Creates a context from raw bytes.
   *
   * @param bytes The 32-byte context hash.
   *
   * @return A {@link ConfidentialMPTClawbackContext}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 32 bytes.
   */
  public static ConfidentialMPTClawbackContext fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == CONTEXT_LENGTH,
      "Context hash must be %s bytes, but was %s bytes",
      CONTEXT_LENGTH, bytes.length
    );
    return new ConfidentialMPTClawbackContext(bytes);
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
    ConfidentialMPTClawbackContext that = (ConfidentialMPTClawbackContext) obj;
    return Arrays.equals(contextHash, that.contextHash);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(contextHash);
  }

  @Override
  public String toString() {
    return "ConfidentialMPTClawbackContext{hash=" + hexValue() + "}";
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
