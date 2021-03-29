package org.xrpl.xrpl4j.crypto.signing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;

import java.util.Objects;

/**
 * Utility methods to help with generating, validating, and manipulating digital signatures.
 */
public class SignatureUtils {

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

  public UnsignedByteArray toMultiSignableBytes(final Transaction transaction, String signerAddress) {
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(signerAddress);

    try {
      final String unsignedJson = objectMapper.writeValueAsString(transaction);
      final String unsignedBinaryHex = binaryCodec.encodeForMultiSigning(unsignedJson, signerAddress);
      return UnsignedByteArray.fromHex(unsignedBinaryHex);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Add {@link Transaction#transactionSignature()} to the given unsignedTransaction. Because {@link Transaction} is not
   * an Immutable object, it does not have a generated builder like its subclasses do. Thus, this method needs to
   * rebuild transactions based on their runtime type.
   *
   * @param unsignedTransaction An unsigned {@link Transaction} to add a signature to. {@link
   *                            Transaction#transactionSignature()} must not be provided, and {@link
   *                            Transaction#signingPublicKey()} must be provided.
   * @param signature           A {@link Signature} containing the transaction signature.
   *
   * @return A copy of {@code unsignedTransaction} with the {@link Transaction#transactionSignature()} field added.
   */
  public SignedTransaction addSignatureToTransaction(
    final Transaction unsignedTransaction,
    final Signature signature
  ) {
    Objects.requireNonNull(unsignedTransaction);
    Objects.requireNonNull(signature);

    Preconditions.checkArgument(
      !unsignedTransaction.transactionSignature().isPresent(),
      "Transactions to be signed must not already include a signature."
    );
    Preconditions.checkArgument(
      unsignedTransaction.signingPublicKey().isPresent(),
      "Transactions to be signed must include a public key that corresponds to the signing key."
    );

    final Transaction signedTransaction;
    if (Payment.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = Payment.builder().from((Payment) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (AccountSet.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = AccountSet.builder().from((AccountSet) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (AccountDelete.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = AccountDelete.builder().from((AccountDelete) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (CheckCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = CheckCancel.builder().from((CheckCancel) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (CheckCash.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = CheckCash.builder().from((CheckCash) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (CheckCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = CheckCreate.builder().from((CheckCreate) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (DepositPreAuth.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = DepositPreAuth.builder().from((DepositPreAuth) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (EscrowCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = EscrowCreate.builder().from((EscrowCreate) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (EscrowCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = EscrowCancel.builder().from((EscrowCancel) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (EscrowFinish.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = EscrowFinish.builder().from((EscrowFinish) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (TrustSet.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = TrustSet.builder().from((TrustSet) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (OfferCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = OfferCreate.builder().from((OfferCreate) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (OfferCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = OfferCancel.builder().from((OfferCancel) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (PaymentChannelCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = PaymentChannelCreate.builder().from((PaymentChannelCreate) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (PaymentChannelClaim.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = PaymentChannelClaim.builder().from((PaymentChannelClaim) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (PaymentChannelFund.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = PaymentChannelFund.builder().from((PaymentChannelFund) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (SetRegularKey.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = SetRegularKey.builder().from((SetRegularKey) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else if (SignerListSet.class.isAssignableFrom(unsignedTransaction.getClass())) {
      signedTransaction = SignerListSet.builder().from((SignerListSet) unsignedTransaction)
        .transactionSignature(signature.base16Value())
        .build();
    } else {
      // Should never happen, but will in a unit test if we miss one.
      throw new IllegalArgumentException("Signing fields could not be added to the unsignedTransaction.");
    }
    try {
      String signedJson = objectMapper.writeValueAsString(signedTransaction);
      String signedBlob = binaryCodec.encode(signedJson); // <-- txBlob must be binary-encoded.
      return SignedTransaction.builder()
        .unsignedTransaction(unsignedTransaction)
        .signature(signature)
        .signedTransaction(signedTransaction)
        .signedTransactionBytes(UnsignedByteArray.of(BaseEncoding.base16().decode(signedBlob)))
        .build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
