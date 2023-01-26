package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration tests to validate submission of SignerListSet transactions.
 */
public class SignerListSetIT extends AbstractIT {

  @Test
  void addSignersToSignerListAndSendPayment() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////////
    // Create four accounts, one for the multisign account owner, one for their two friends,
    // and one to send a Payment to.
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair aliceKeyPair = createRandomAccountEd25519();
    KeyPair bobKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    /////////////////////////////
    // Wait for all accounts to show up in a validated ledger
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
    FeeResult feeResult = xrplClient.fee();
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
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sourceKeyPair.privateKey(), signerListSet
    );
    SubmitResult<SignerListSet> signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertThat(signerListSetResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      signerListSetResult.transactionResult().hash()
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the source account's signer list
    // exists
    AccountInfoResult sourceAccountInfoAfterSignerListSet = scanForResult(
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
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys using the "multiSign" method.
    Set<SignatureWithPublicKey> signers = Lists.newArrayList(aliceKeyPair, bobKeyPair).stream()
      .map(wallet -> {
          Signature signedPayment = signatureService.multiSign(wallet.privateKey(), unsignedPayment);
          return SignatureWithPublicKey.builder()
            .signingPublicKey(wallet.publicKey())
            .transactionSignature(signedPayment)
            .build();
        }
      )
      .collect(Collectors.toSet());

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    MultiSignedTransaction<Payment> multiSigPayment = MultiSignedTransaction.<Payment>builder()
      .unsignedTransaction(unsignedPayment)
      .signatureWithPublicKeySet(signers)
      .build();

    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transaction().hash()
    );
  }

  @Test
  void addSignersToSignerListThenDeleteSignerList() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////////
    // Create three accounts, one for the multisign account owner, one for their two friends
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair aliceKeyPair = createRandomAccountEd25519();
    KeyPair bobKeyPair = createRandomAccountEd25519();

    /////////////////////////////
    // Wait for all accounts to show up in a validated ledger
    AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );
    scanForResult(() -> this.getValidatedAccountInfo(aliceKeyPair.publicKey().deriveAddress()));
    scanForResult(() -> this.getValidatedAccountInfo(bobKeyPair.publicKey().deriveAddress()));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    FeeResult feeResult = xrplClient.fee();
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
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sourceKeyPair.privateKey(), signerListSet
    );
    SubmitResult<SignerListSet> signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertThat(signerListSetResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      signerListSetResult.transactionResult().hash()
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the source account's signer list
    // exists
    AccountInfoResult sourceAccountInfoAfterSignerListSet = scanForResult(
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

    /////////////////////////////
    // Construct a SignerListSet transaction with 0 quorum and an empty list of signer entries to
    // delete the signer list
    SignerListSet deleteSignerList = SignerListSet.builder()
      .from(signerListSet)
      .signerQuorum(UnsignedInteger.ZERO)
      .signerEntries(Lists.emptyList())
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .build();

    SingleSignedTransaction<SignerListSet> signedDeleteSignerList = signatureService.sign(
      sourceKeyPair.privateKey(), deleteSignerList
    );
    SubmitResult<SignerListSet> signerListDeleteResult = xrplClient.submit(signedDeleteSignerList);
    assertThat(signerListDeleteResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      signerListDeleteResult.transactionResult().hash()
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the signer list has been deleted
    scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().signerLists().size() == 0
    );

  }
}
