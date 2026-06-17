package org.xrpl.xrpl4j.crypto.signing;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.Attestation;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

/**
 * Utility methods to help with generating, validating, and manipulating digital signatures.
 */
public class SignatureUtils {

  private static final SignatureUtils INSTANCE = new SignatureUtils(
    ObjectMapperFactory.create(),
    XrplBinaryCodec.getInstance()
  );

  /**
   * Get the singleton instance of {@link SignatureUtils}.
   *
   * @return An {@link SignatureUtils}.
   */
  public static SignatureUtils getInstance() {
    return INSTANCE;
  }

  private final ObjectMapper objectMapper;
  private final XrplBinaryCodec binaryCodec;

  /**
   * Required-args constructor.
   *
   * @param objectMapper A {@link ObjectMapper}.
   * @param binaryCodec  A {@link XrplBinaryCodec}.
   */
  public SignatureUtils(final ObjectMapper objectMapper, final XrplBinaryCodec binaryCodec) {
    this.objectMapper = Objects.requireNonNull(objectMapper);
    this.binaryCodec = Objects.requireNonNull(binaryCodec);
  }

  /**
   * Helper method to convert a {@link Transaction} into bytes that can be used directly for signing.
   *
   * @param transaction A {@link Transaction} to be signed.
   *
   * @return An {@link UnsignedByteArray}.
   */
  public UnsignedByteArray toSignableBytes(final Transaction transaction) {
    Objects.requireNonNull(transaction);
    try {
      final String unsignedJson = objectMapper.writeValueAsString(transaction);
      final String unsignedBinaryHex = binaryCodec.encodeForSigning(unsignedJson);
      return UnsignedByteArray.fromHex(unsignedBinaryHex);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Helper method to convert an {@link UnsignedClaim} into bytes that can be used directly for signing.
   *
   * @param unsignedClaim An {@link UnsignedClaim} to be signed.
   *
   * @return An {@link UnsignedByteArray}.
   */
  public UnsignedByteArray toSignableBytes(final UnsignedClaim unsignedClaim) {
    Objects.requireNonNull(unsignedClaim);
    try {
      final String unsignedJson = objectMapper.writeValueAsString(unsignedClaim);
      // Note `encodeForSigningClaim` creates a special binary encoding
      final String unsignedBinaryHex = binaryCodec.encodeForSigningClaim(unsignedJson);
      return UnsignedByteArray.fromHex(unsignedBinaryHex);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Helper method to convert an {@link Attestation} into bytes that can be used directly for signing.
   *
   * <p>This method will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
   * is subject to change.</p>
   *
   * @param attestation An {@link Attestation} to be signed.
   *
   * @return An {@link UnsignedByteArray}.
   */
  @Beta
  public UnsignedByteArray toSignableBytes(final Attestation attestation) {
    Objects.requireNonNull(attestation);
    try {
      // Note: Even though this implementation appears to match `toSignableBytes(Transaction)`, an `Attestation` is not
      // a transaction, which is why this method is required.
      final String unsignedJson = objectMapper.writeValueAsString(attestation);
      final String unsignedBinaryHex = binaryCodec.encode(unsignedJson);
      return UnsignedByteArray.fromHex(unsignedBinaryHex);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Helper method to convert a {@link Transaction} into bytes that can be signed by multiple signers, as is the case
   * when the source account has set a SignerList.
   *
   * @param transaction   A {@link Transaction} to be signed.
   * @param signerAddress The {@link Address} of the signer of the transaction.
   *
   * @return An {@link UnsignedByteArray}.
   */
  public UnsignedByteArray toMultiSignableBytes(final Transaction transaction, final Address signerAddress) {
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(signerAddress);

    try {
      final String unsignedJson = objectMapper.writeValueAsString(transaction);
      final String unsignedBinaryHex = binaryCodec.encodeForMultiSigning(unsignedJson, signerAddress.value());
      return UnsignedByteArray.fromHex(unsignedBinaryHex);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  /**
   * Convert a {@link Batch} transaction to bytes that can be signed by a Batch inner transaction signer (single-sign
   * path). Per XLS-0056 V1_1, the payload is: {@code HashPrefix::Batch} + outer {@code Account} + sequence +
   * {@code Flags} + count + inner tx IDs + {@code batchSignerAddress}.
   *
   * @param batch              A {@link Batch} transaction.
   * @param batchSignerAddress The {@link Address} of the BatchSigner entry signing this batch.
   *
   * @return An {@link UnsignedByteArray} containing the bytes to be signed.
   */
  @Beta
  public UnsignedByteArray toSignableInnerBytes(final Batch batch, final Address batchSignerAddress) {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(batchSignerAddress);
    return binaryCodec.encodeForBatchInnerSigning(batch, batchSignerAddress);
  }

  /**
   * Helper method to convert a {@link LoanSet} transaction into bytes that can be multi-signed by a counterparty
   * signer.
   *
   * <p>This method will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API
   * is subject to change.</p>
   *
   * @param transaction   A {@link LoanSet} to be counterparty multi-signed.
   * @param signerAddress The {@link Address} of the counterparty signer.
   *
   * @return An {@link UnsignedByteArray}.
   */
  @Beta
  public UnsignedByteArray toCounterpartyMultiSignableBytes(final LoanSet transaction, final Address signerAddress) {
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(signerAddress);

    try {
      final String unsignedJson = objectMapper.writeValueAsString(transaction);
      final String unsignedBinaryHex = binaryCodec.encodeForMultiSigningWithSigningPubKey(
        unsignedJson, signerAddress.value()
      );
      return UnsignedByteArray.fromHex(unsignedBinaryHex);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Converts a {@link Batch} to multi-signable bytes for a specific signer. This is used when a multi-sig account acts
   * as a BatchSigner with nested Signers. Per XLS-0056 V1_1 / rippled's {@code checkBatchMultiSign}, the payload is the
   * base batch serialization followed by {@code batchSignerAddress} then {@code nestedSignerAddress}.
   *
   * @param batch               The {@link Batch} to convert.
   * @param batchSignerAddress  The {@link Address} of the BatchSigner entry (outer multi-sig account).
   * @param nestedSignerAddress The {@link Address} of the individual signer within the BatchSigner's Signers list.
   *
   * @return An {@link UnsignedByteArray} containing the batch serialization with both account ID suffixes.
   */
  @Beta
  public UnsignedByteArray toMultiSignableInnerBytes(
    final Batch batch, final Address batchSignerAddress, final Address nestedSignerAddress
  ) {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(batchSignerAddress);
    Objects.requireNonNull(nestedSignerAddress);
    return binaryCodec.encodeForBatchInnerMultiSigning(batch, batchSignerAddress, nestedSignerAddress);
  }

}
