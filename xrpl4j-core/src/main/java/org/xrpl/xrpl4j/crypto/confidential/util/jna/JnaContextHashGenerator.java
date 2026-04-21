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
 * mpt-crypto C library for context hash generation.
 *
 * <p>This implementation calls the native {@code mpt_get_*_context_hash} functions from mpt_utility.h,
 * which compute SHA512Half over the transaction-specific serialization format.</p>
 */
public class JnaContextHashGenerator implements ContextHashGenerator {

  private static final int HASH_SIZE = 32;
  private static final AddressCodec ADDRESS_CODEC = new AddressCodec();

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge.
   */
  public JnaContextHashGenerator() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaContextHashGenerator(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
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

    byte[] accountBytes = ADDRESS_CODEC.decodeAccountId(account).toByteArray();
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    byte[] outHash = new byte[HASH_SIZE];

    int result = nativeCrypto.generateConvertContextHash(accountBytes, issuanceIdBytes, sequence.intValue(), outHash);
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

    byte[] accountBytes = ADDRESS_CODEC.decodeAccountId(account).toByteArray();
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    byte[] outHash = new byte[HASH_SIZE];

    int result = nativeCrypto.generateConvertBackContextHash(
      accountBytes, issuanceIdBytes, sequence.intValue(), version.intValue(), outHash
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

    byte[] accountBytes = ADDRESS_CODEC.decodeAccountId(account).toByteArray();
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    byte[] destBytes = ADDRESS_CODEC.decodeAccountId(destination).toByteArray();
    byte[] outHash = new byte[HASH_SIZE];

    int result = nativeCrypto.generateSendContextHash(
      accountBytes, issuanceIdBytes, sequence.intValue(), destBytes, version.intValue(), outHash
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

    byte[] accountBytes = ADDRESS_CODEC.decodeAccountId(account).toByteArray();
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    byte[] holderBytes = ADDRESS_CODEC.decodeAccountId(holder).toByteArray();
    byte[] outHash = new byte[HASH_SIZE];

    int result = nativeCrypto.generateClawbackContextHash(
      accountBytes, issuanceIdBytes, sequence.intValue(), holderBytes, outHash
    );
    if (result != 0) {
      throw new IllegalStateException("mpt_get_clawback_context_hash failed with error code: " + result);
    }

    return ConfidentialMptClawbackContext.of(UnsignedByteArray.of(outHash));
  }
}
