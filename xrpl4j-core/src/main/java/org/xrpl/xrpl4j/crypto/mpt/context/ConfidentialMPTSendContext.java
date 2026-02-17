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
 * Represents the context hash for a ConfidentialMPTSend transaction.
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
 * <p>This context is used in {@link org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextMultiProofGenerator}
 * to bind the proof to a specific transaction.</p>
 */
public final class ConfidentialMPTSendContext {

  /**
   * The length of the context hash in bytes.
   */
  public static final int CONTEXT_LENGTH = 32;

  /**
   * Transaction type for ConfidentialMPTSend (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_SEND = 88;

  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private final byte[] contextHash;

  private ConfidentialMPTSendContext(final byte[] contextHash) {
    this.contextHash = Arrays.copyOf(contextHash, contextHash.length);
  }

  /**
   * Generates a context hash for a ConfidentialMPTSend transaction.
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
  public static ConfidentialMPTSendContext generate(
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

    // Serialize fields matching rippled's addCommonZKPFields + addBitString(destination) + add32(version)
    // Total: 2 (txType) + 20 (account) + 4 (sequence) + 24 (issuanceId) + 20 (destination) + 4 (version) = 74 bytes
    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    // 1. add16(txType) - 2 bytes big-endian
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_SEND);

    // 2. addBitString(account) - 20 bytes raw
    UnsignedByteArray accountBytes = ADDRESS_CODEC.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());

    // 3. add32(sequence) - 4 bytes big-endian
    buffer.putInt(sequence.intValue());

    // 4. addBitString(issuanceID) - 24 bytes raw
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);

    // 5. addBitString(destination) - 20 bytes raw
    UnsignedByteArray destinationBytes = ADDRESS_CODEC.decodeAccountId(destination);
    buffer.put(destinationBytes.toByteArray());

    // 6. add32(version) - 4 bytes big-endian
    buffer.putInt(version.intValue());

    // Compute SHA512Half (first 32 bytes of SHA512)
    byte[] hash = HashingUtils.sha512Half(buffer.array()).toByteArray();

    return new ConfidentialMPTSendContext(hash);
  }

  /**
   * Creates a context from raw bytes.
   *
   * @param bytes The 32-byte context hash.
   *
   * @return A {@link ConfidentialMPTSendContext}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 32 bytes.
   */
  public static ConfidentialMPTSendContext fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == CONTEXT_LENGTH,
      "Context hash must be %s bytes, but was %s bytes",
      CONTEXT_LENGTH, bytes.length
    );
    return new ConfidentialMPTSendContext(bytes);
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
    ConfidentialMPTSendContext that = (ConfidentialMPTSendContext) obj;
    return Arrays.equals(contextHash, that.contextHash);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(contextHash);
  }

  @Override
  public String toString() {
    return "ConfidentialMPTSendContext{hash=" + hexValue() + "}";
  }
}

