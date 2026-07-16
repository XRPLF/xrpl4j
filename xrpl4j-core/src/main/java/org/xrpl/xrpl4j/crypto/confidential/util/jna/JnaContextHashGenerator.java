package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.util.ContextHashGenerator;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;

/**
 * JNA-backed implementation of {@link ContextHashGenerator} that delegates to the native
 * mpt-crypto C library via {@link MptCryptoLibrary} for context hash generation.
 *
 * <p>This implementation calls the native {@code mpt_get_*_context_hash} functions from mpt_utility.h,
 * which compute SHA512Half over the transaction-specific serialization format.</p>
 */
public class JnaContextHashGenerator implements ContextHashGenerator {

  private static final int HASH_SIZE = 32;
  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaContextHashGenerator() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaContextHashGenerator(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public ConfidentialMptConvertContext generateConvertContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");

    MptCryptoLibrary.MptAccountId accountStruct = toAccountId(account);
    MptCryptoLibrary.MptIssuanceId issuanceStruct = toIssuanceId(issuanceId);
    byte[] outHash = new byte[HASH_SIZE];

    int result = lib.mpt_get_convert_context_hash(accountStruct, issuanceStruct, sequence.intValue(), outHash);
    if (result != 0) {
      throw new IllegalStateException("mpt_get_convert_context_hash failed with error code: " + result);
    }

    return ConfidentialMptConvertContext.of(UnsignedByteArray.of(outHash));
  }

  @Override
  public ConfidentialMptConvertBackContext generateConvertBackContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedInteger version
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(version, "version must not be null");

    MptCryptoLibrary.MptAccountId accountStruct = toAccountId(account);
    MptCryptoLibrary.MptIssuanceId issuanceStruct = toIssuanceId(issuanceId);
    byte[] outHash = new byte[HASH_SIZE];

    int result = lib.mpt_get_convert_back_context_hash(
      accountStruct, issuanceStruct, sequence.intValue(), version.intValue(), outHash
    );
    if (result != 0) {
      throw new IllegalStateException("mpt_get_convert_back_context_hash failed with error code: " + result);
    }

    return ConfidentialMptConvertBackContext.of(UnsignedByteArray.of(outHash));
  }

  @Override
  public ConfidentialMptSendContext generateSendContext(
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

    MptCryptoLibrary.MptAccountId accountStruct = toAccountId(account);
    MptCryptoLibrary.MptIssuanceId issuanceStruct = toIssuanceId(issuanceId);
    MptCryptoLibrary.MptAccountId destStruct = toAccountId(destination);
    byte[] outHash = new byte[HASH_SIZE];

    int result = lib.mpt_get_send_context_hash(
      accountStruct, issuanceStruct, sequence.intValue(), destStruct, version.intValue(), outHash
    );
    if (result != 0) {
      throw new IllegalStateException("mpt_get_send_context_hash failed with error code: " + result);
    }

    return ConfidentialMptSendContext.of(UnsignedByteArray.of(outHash));
  }

  @Override
  public ConfidentialMptClawbackContext generateClawbackContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final Address holder
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(holder, "holder must not be null");

    MptCryptoLibrary.MptAccountId accountStruct = toAccountId(account);
    MptCryptoLibrary.MptIssuanceId issuanceStruct = toIssuanceId(issuanceId);
    MptCryptoLibrary.MptAccountId holderStruct = toAccountId(holder);
    byte[] outHash = new byte[HASH_SIZE];

    int result = lib.mpt_get_clawback_context_hash(
      accountStruct, issuanceStruct, sequence.intValue(), holderStruct, outHash
    );
    if (result != 0) {
      throw new IllegalStateException("mpt_get_clawback_context_hash failed with error code: " + result);
    }

    return ConfidentialMptClawbackContext.of(UnsignedByteArray.of(outHash));
  }

  /**
   * Converts an {@link Address} to a JNA {@link MptCryptoLibrary.MptAccountId} struct.
   *
   * @param address The XRPL address.
   *
   * @return The populated struct.
   */
  private static MptCryptoLibrary.MptAccountId toAccountId(final Address address) {
    MptCryptoLibrary.MptAccountId accountId = new MptCryptoLibrary.MptAccountId();
    byte[] decoded = ADDRESS_CODEC.decodeAccountId(address).toByteArray();
    System.arraycopy(decoded, 0, accountId.bytes, 0, 20);
    return accountId;
  }

  /**
   * Converts an {@link MpTokenIssuanceId} to a JNA {@link MptCryptoLibrary.MptIssuanceId} struct.
   *
   * @param issuanceId The MPTokenIssuanceId.
   *
   * @return The populated struct.
   */
  private static MptCryptoLibrary.MptIssuanceId toIssuanceId(final MpTokenIssuanceId issuanceId) {
    MptCryptoLibrary.MptIssuanceId issId = new MptCryptoLibrary.MptIssuanceId();
    byte[] decoded = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    System.arraycopy(decoded, 0, issId.bytes, 0, 24);
    return issId;
  }
}
