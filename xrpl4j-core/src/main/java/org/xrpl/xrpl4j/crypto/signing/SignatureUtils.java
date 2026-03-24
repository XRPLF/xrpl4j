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
import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.Attestation;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AmmBid;
import org.xrpl.xrpl4j.model.transactions.AmmClawback;
import org.xrpl.xrpl4j.model.transactions.AmmCreate;
import org.xrpl.xrpl4j.model.transactions.AmmDelete;
import org.xrpl.xrpl4j.model.transactions.AmmDeposit;
import org.xrpl.xrpl4j.model.transactions.AmmVote;
import org.xrpl.xrpl4j.model.transactions.AmmWithdraw;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.Clawback;
import org.xrpl.xrpl4j.model.transactions.CredentialAccept;
import org.xrpl.xrpl4j.model.transactions.CredentialCreate;
import org.xrpl.xrpl4j.model.transactions.CredentialDelete;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.DidDelete;
import org.xrpl.xrpl4j.model.transactions.DidSet;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceDestroy;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.NfTokenAcceptOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenBurn;
import org.xrpl.xrpl4j.model.transactions.NfTokenCancelOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.OracleDelete;
import org.xrpl.xrpl4j.model.transactions.OracleSet;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainDelete;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainSet;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XChainAccountCreateCommit;
import org.xrpl.xrpl4j.model.transactions.XChainAddAccountCreateAttestation;
import org.xrpl.xrpl4j.model.transactions.XChainAddClaimAttestation;
import org.xrpl.xrpl4j.model.transactions.XChainClaim;
import org.xrpl.xrpl4j.model.transactions.XChainCommit;
import org.xrpl.xrpl4j.model.transactions.XChainCreateBridge;
import org.xrpl.xrpl4j.model.transactions.XChainCreateClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainModifyBridge;

import java.util.List;
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
   * Add {@link Transaction#transactionSignature()} to the given transaction. Because {@link Transaction} is not an
   * Immutable object, it does not have a generated builder like its subclasses do. Thus, this method needs to rebuild
   * transactions based on their runtime type.
   *
   * @param transaction An unsigned {@link Transaction} to add a signature to. Note that
   *                    {@link Transaction#transactionSignature()} must not be provided, and
   *                    {@link Transaction#signingPublicKey()} must be provided.
   * @param signature   A {@link Signature} containing the transaction signature.
   * @param <T>         extends {@link Transaction}.
   *
   * @return A copy of {@code transaction} with the {@link Transaction#transactionSignature()} field added.
   *
   * @deprecated This method will be removed in a future version. Prefer using
   *   {@link Transaction#withTransactionSignature(Signature)}
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public <T extends Transaction> SingleSignedTransaction<T> addSignatureToTransaction(
    final T transaction, final Signature signature
  ) {
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(signature);

    // This precondition will move to SingleSignedTransaction#check() once we remove this method.
    Preconditions.checkArgument(
      !transaction.transactionSignature().isPresent(),
      "Transactions to be signed must not already include a signature."
    );

    final Transaction transactionWithSignature = transaction.withTransactionSignature(signature);

    return SingleSignedTransaction.<T>builder()
      .unsignedTransaction(transaction)
      .signature(signature)
      .signedTransaction((T) transactionWithSignature)
      .build();
  }

  /**
   * Add {@link Transaction#signers()}} to the given transaction. Because {@link Transaction} is not an Immutable
   * object, it does not have a generated builder like its subclasses do. Thus, this method needs to rebuild
   * transactions based on their runtime type.
   *
   * @param transaction An unsigned {@link Transaction} to add a signature to. Note that
   *                    {@link Transaction#transactionSignature()} must not be provided, and
   *                    {@link Transaction#signingPublicKey()} must be an empty string.
   * @param signers     A {@link List} of {@link SignerWrapper}s containing the transaction signatures.
   * @param <T>         extends {@link Transaction}.
   *
   * @return A copy of {@code transaction} with the {@link Transaction#signers()}} field added.
   *
   * @deprecated This method will be removed in a future version. Prefer using
   *   {@link Transaction#withSigners(Iterable)}.
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public <T extends Transaction> T addMultiSignaturesToTransaction(T transaction, List<SignerWrapper> signers) {
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(signers);

    Preconditions.checkArgument(
      !transaction.transactionSignature().isPresent(),
      "Transactions to be signed must not already include a signature."
    );
    Preconditions.checkArgument(
      transaction.signingPublicKey().equals(PublicKey.MULTI_SIGN_PUBLIC_KEY),
      "Transactions to be multisigned must set signingPublicKey to an empty String."
    );

    return (T) transaction.withSigners(signers);
  }
}
