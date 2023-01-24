package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SubmitMultisignedIT extends AbstractIT {

  protected final ObjectMapper objectMapper = ObjectMapperFactory.create();
  protected final XrplBinaryCodec binaryCodec = new XrplBinaryCodec();

  /////////////////////////////
  // Create four accounts, one for the multisign account owner, one for their two friends,
  // and one to send a Payment to.
  KeyPair sourceKeyPair = createRandomAccountEd25519();
  KeyPair aliceKeyPair = createRandomAccountEd25519();
  KeyPair bobKeyPair = createRandomAccountEd25519();
  KeyPair destinationKeyPair = createRandomAccountEd25519();

  FeeResult feeResult;
  AccountInfoResult sourceAccountInfoAfterSignerListSet;
  SubmitResult<SignerListSet> signerListSetResult;

  @BeforeEach
  public void setUp() throws JsonRpcClientErrorException, JsonProcessingException {

    /////////////////////////////
    // Wait for all of the accounts to show up in a validated ledger
    final AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );
    scanForResult(() -> this.getValidatedAccountInfo(aliceKeyPair.publicKey().deriveAddress()));
    scanForResult(() -> this.getValidatedAccountInfo(bobKeyPair.publicKey().deriveAddress()));
    scanForResult(() -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress()));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    feeResult = xrplClient.fee();
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(aliceKeyPair.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(bobKeyPair.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(sourceKeyPair.publicKey().base16Value())
      .build();

    /////////////////////////////
    // Validate that the transaction was submitted successfully
    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sourceKeyPair.privateKey(), signerListSet
    );
    signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertThat(signerListSetResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(signerListSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(signerListSetResult.transactionResult().hash());
    logInfo(
      signerListSetResult.transactionResult().transaction().transactionType(),
      signerListSetResult.transactionResult().hash()
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the source account's signer list
    // exists
    sourceAccountInfoAfterSignerListSet = scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().signerLists().size() == 1
    );

    assertThat(
      sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        .signerEntries().stream()
        .sorted(Comparator.comparing(entry -> entry.signerEntry().account()))
        .collect(Collectors.toList())
    ).isEqualTo(signerListSet.signerEntries().stream()
      .sorted(Comparator.comparing(entry -> entry.signerEntry().account()))
      .collect(Collectors.toList()));
  }

  @Test
  public void submitMultisignedAndVerifyHash() throws JsonRpcClientErrorException, JsonProcessingException {

    /////////////////////////////
    // Construct an unsigned Payment transaction to be multisigned
    Payment unsignedPayment = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(
        FeeUtils.computeMultisigNetworkFees(
          feeResult,
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        ).recommendedFee()
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys
    List<SignerWrapper> signers = Lists.newArrayList(aliceKeyPair, bobKeyPair).stream()
      .map(keyPair -> {
          Signature multiSignature = signatureService.multiSign(keyPair.privateKey(), unsignedPayment);
          return SignerWrapper.of(Signer.builder()
            .account(keyPair.publicKey().deriveAddress())
            .signingPublicKey(keyPair.publicKey().base16Value())
            .transactionSignature(multiSignature.base16Value())
            .build()
          );
        }
      )
      .collect(Collectors.toList());

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    Payment multiSigPayment = Payment.builder()
      .from(unsignedPayment)
      .signers(signers)
      .build();

    MultiSignedTransaction<Payment> signedTransaction = MultiSignedTransaction.<Payment>builder()
      .signedTransaction(multiSigPayment)
      .unsignedTransaction(unsignedPayment)
      .addAllSignatureWithPublicKeySet(
        signers.stream()
          .map(SignerWrapper::signer)
          .map(signer -> SignatureWithPublicKey.builder()
            .transactionSignature(Signature.fromBase16(signer.transactionSignature()))
            .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(signer.signingPublicKey()))
            .build()
          )
          .collect(Collectors.toList())
      )
      .signedTransactionBytes(
        UnsignedByteArray.fromHex(binaryCodec.encode(objectMapper.writeValueAsString(multiSigPayment)))
      )
      .build();
    String libraryCalculatedHash = signedTransaction.hash().value();

    SubmitMultiSignedResult<Payment> submitMultiSignedResult = xrplClient.submitMultisigned(multiSigPayment);

    assertThat(submitMultiSignedResult.transaction().hash().value()).isEqualTo(libraryCalculatedHash);

    assertThat(submitMultiSignedResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(signerListSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(signerListSetResult.transactionResult().hash());
    logInfo(
      submitMultiSignedResult.transaction().transaction().transactionType(),
      submitMultiSignedResult.transaction().hash()
    );
  }

  @Test
  public void submitMultisignedWithSignersInDescOrderAndVerifyHash() throws
    JsonRpcClientErrorException, JsonProcessingException {

    /////////////////////////////
    // Construct an unsigned Payment transaction to be multisigned
    Payment unsignedPayment = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(
        FeeUtils.computeMultisigNetworkFees(
          feeResult,
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        ).recommendedFee()
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys
    List<SignerWrapper> signers = Lists.newArrayList(aliceKeyPair, bobKeyPair).stream()
      .map(keyPair -> {
          Signature multiSignature = signatureService.multiSign(keyPair.privateKey(), unsignedPayment);
          return SignerWrapper.of(Signer.builder()
            .account(keyPair.publicKey().deriveAddress())
            .signingPublicKey(keyPair.publicKey().base16Value())
            .transactionSignature(multiSignature.base16Value())
            .build()
          );
        }
      )
      .collect(Collectors.toList());

    final AddressCodec addressCodec = AddressCodec.getInstance();
    signers = signers.stream().sorted(
      Comparator.comparing(
        signature -> new BigInteger(addressCodec.decodeAccountId(
          Address.of(signature.signer().account().value())
        ).hexValue(), 16),
        Comparator.reverseOrder()
      )
    ).collect(Collectors.toList());

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    Payment multiSigPayment = Payment.builder()
      .from(unsignedPayment)
      .signers(signers)
      .build();

    MultiSignedTransaction<Payment> signedTransaction = MultiSignedTransaction.<Payment>builder()
      .signedTransaction(multiSigPayment)
      .unsignedTransaction(unsignedPayment)
      .addAllSignatureWithPublicKeySet(
        signers.stream()
          .map(SignerWrapper::signer)
          .map(signer -> SignatureWithPublicKey.builder()
            .transactionSignature(Signature.fromBase16(signer.transactionSignature()))
            .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(signer.signingPublicKey()))
            .build()
          )
          .collect(Collectors.toList())
      )
      .signedTransactionBytes(
        UnsignedByteArray.fromHex(binaryCodec.encode(objectMapper.writeValueAsString(multiSigPayment)))
      )
      .build();
    String libraryCalculatedHash = signedTransaction.hash().value();

    SubmitMultiSignedResult<Payment> submitMultiSignedResult = xrplClient.submitMultisigned(multiSigPayment);
    assertThat(submitMultiSignedResult.transaction().hash().value()).isEqualTo(libraryCalculatedHash);
    assertThat(submitMultiSignedResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(signerListSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(signerListSetResult.transactionResult().hash());

    logInfo(
      submitMultiSignedResult.transaction().transaction().transactionType(),
      submitMultiSignedResult.transaction().hash()
    );

  }
}
