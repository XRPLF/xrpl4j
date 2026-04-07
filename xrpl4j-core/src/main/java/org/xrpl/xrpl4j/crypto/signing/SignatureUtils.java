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
   * Convert a {@link Batch} transaction to bytes that can be signed by a Batch inner transaction signer. Per XLS-0056,
   * BatchSigners sign the inner transaction in a specific format: HashPrefix::batch + flags + count + inner tx IDs.
   *
   * @param batch A {@link Batch} transaction.
   *
   * @return An {@link UnsignedByteArray} containing the bytes to be signed.
   */
  @Beta
  public UnsignedByteArray toSignableInnerBytes(final Batch batch) {
    Objects.requireNonNull(batch);
    try {
      return binaryCodec.encodeForBatchInnerSigning(batch);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
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
   * as a BatchSigner with nested Signers. Per rippled's checkBatchMultiSign, this uses batch serialization followed by
   * appending the signer's account ID.
   *
   * @param batch         The {@link Batch} to convert.
   * @param signerAddress The {@link Address} of the signer.
   *
   * @return An {@link UnsignedByteArray} containing the batch serialization with account ID suffix.
   */
  @Beta
  public UnsignedByteArray toMultiSignableInnerBytes(final Batch batch, final Address signerAddress) {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(signerAddress);
    try {
      return binaryCodec.encodeForBatchInnerMultiSigning(batch, signerAddress);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Helper method to convert a {@link Transaction} into bytes that can be multi-signed by a sponsor.
   *
   * <p>Unlike {@link #toMultiSignableBytes(Transaction, Address)}, this method preserves the existing
   * {@code SigningPubKey} field in the encoded bytes. This is necessary when a sponsor multi-signs a transaction
   * where the first-party signer's {@code SigningPubKey} must remain intact in the signed data.</p>
   *
   * <p>This method will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
   * Its API is subject to change.</p>
   *
   * @param transaction   A {@link Transaction} to be sponsor multi-signed.
   * @param signerAddress The {@link Address} of the sponsor signer.
   *
   * @return An {@link UnsignedByteArray}.
   */
  @Beta
  public UnsignedByteArray toSponsorMultiSignableBytes(final Transaction transaction, final Address signerAddress) {
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

}
