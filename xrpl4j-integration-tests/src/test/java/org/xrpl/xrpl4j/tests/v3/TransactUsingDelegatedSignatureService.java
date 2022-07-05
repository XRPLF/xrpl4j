package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Integration tests for submitting payment transactions to the XRPL using a {@link DelegatedSignatureService} for all
 * signing operations.
 */
public class TransactUsingDelegatedSignatureService extends AbstractIT {

  @Test
  public void sendPaymentFromEd25519Account() throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyMetadata sourceKeyMetadata = constructKeyMetadata("sourceWallet");
    final PublicKey sourceWalletPublicKey = delegatedSignatureServiceEd25519.getPublicKey(sourceKeyMetadata);
    final Address sourceWalletAddress = BcAddressUtils.getInstance().deriveAddress(sourceWalletPublicKey);
    this.fundAccount(sourceWalletAddress);

    final KeyMetadata destinationKeyMetadata = constructKeyMetadata("destinationWallet");
    final PublicKey destinationWalletPublicKey = delegatedSignatureServiceEd25519.getPublicKey(destinationKeyMetadata);
    final Address destinationWalletAddress = BcAddressUtils.getInstance().deriveAddress(destinationWalletPublicKey);
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(getComputedNetworkFee(feeResult))
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.base16Value())
      .build();

    SingleSingedTransaction<Payment> signedTransaction = delegatedSignatureServiceEd25519.sign(sourceKeyMetadata,
      payment);
    SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  public void sendPaymentFromSecp256k1Account() throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyMetadata sourceKeyMetadata = constructKeyMetadata("sourceWallet");
    final PublicKey sourceWalletPublicKey = delegatedSignatureServiceSecp256k1.getPublicKey(sourceKeyMetadata);
    final Address sourceWalletAddress = BcAddressUtils.getInstance().deriveAddress(sourceWalletPublicKey);
    this.fundAccount(sourceWalletAddress);

    final KeyMetadata destinationKeyMetadata = constructKeyMetadata("destinationWallet");
    final PublicKey destinationWalletPublicKey = delegatedSignatureServiceSecp256k1.getPublicKey(
      destinationKeyMetadata);
    final Address destinationWalletAddress = BcAddressUtils.getInstance().deriveAddress(destinationWalletPublicKey);
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(getComputedNetworkFee(feeResult))
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.base16Value())
      .build();

    SingleSingedTransaction<Payment> transactionWithSignature = delegatedSignatureServiceSecp256k1
      .sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(transactionWithSignature);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  void multiSigSendPaymentFromEd25519Account() throws JsonRpcClientErrorException, JsonProcessingException {
    this.multiSigSendPaymentHelper(this.delegatedSignatureServiceEd25519);
  }

  @Test
  void multiSigSendPaymentFromSecp256k1Account() throws JsonRpcClientErrorException, JsonProcessingException {
    this.multiSigSendPaymentHelper(this.delegatedSignatureServiceSecp256k1);
  }

  /**
   * Helper to send a multisign payment using a designated {@link DelegatedSignatureService}.
   *
   * @param delegatedSignatureService A particular type of {@link DelegatedSignatureService} for a given key type.
   */
  private void multiSigSendPaymentHelper(final DelegatedSignatureService delegatedSignatureService)
    throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(delegatedSignatureService);

    KeyMetadata sourceKeyMetadata = KeyMetadata.builder().from(KeyMetadata.EMPTY)
      .keyIdentifier("source")
      .build();
    final Address sourceAddress = BcAddressUtils.getInstance()
      .deriveAddress(delegatedSignatureService.getPublicKey(sourceKeyMetadata));
    this.fundAccount(sourceAddress);

    KeyMetadata aliceKeyMetadata = KeyMetadata.builder().from(KeyMetadata.EMPTY)
      .keyIdentifier("alice")
      .build();
    final Address aliceAddress = BcAddressUtils.getInstance()
      .deriveAddress(delegatedSignatureService.getPublicKey(aliceKeyMetadata));
    this.fundAccount(aliceAddress);

    KeyMetadata bobKeyMetadata = KeyMetadata.builder().from(KeyMetadata.EMPTY)
      .keyIdentifier("bob")
      .build();
    final Address bobAddress = BcAddressUtils.getInstance()
      .deriveAddress(delegatedSignatureService.getPublicKey(bobKeyMetadata));
    this.fundAccount(bobAddress);

    KeyMetadata destinationKeyMetadata = KeyMetadata.builder().from(KeyMetadata.EMPTY)
      .keyIdentifier("destination")
      .build();
    final Address destinationAddress = BcAddressUtils.getInstance()
      .deriveAddress(delegatedSignatureService.getPublicKey(destinationKeyMetadata));
    this.fundAccount(destinationAddress);

    /////////////////////////////
    // Wait for all accounts to show up in a validated ledger
    final AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(sourceAddress)
    );
    scanForResult(() -> this.getValidatedAccountInfo(aliceAddress));
    scanForResult(() -> this.getValidatedAccountInfo(bobAddress));
    scanForResult(() -> this.getValidatedAccountInfo(destinationAddress));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    FeeResult feeResult = xrplClient.fee();
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourceAddress)
      .fee(getComputedNetworkFee(feeResult))
      .sequence(sourceAccountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(aliceAddress)
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(bobAddress)
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(toPublicKey(sourceKeyMetadata, delegatedSignatureService).base16Value())
      .build();

    SingleSingedTransaction<SignerListSet> signedSignerListSet = delegatedSignatureService.sign(
      sourceKeyMetadata, signerListSet
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
      () -> this.getValidatedAccountInfo(sourceAddress),
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
      .account(sourceAddress)
      .fee(
        FeeUtils.computeMultiSigFee(
          feeResult.drops().openLedgerFee(),
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        )
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(destinationAddress)
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys using the "multiSign" method.
    List<SignerWrapper> signers = Lists.newArrayList(aliceKeyMetadata, bobKeyMetadata).stream()
      .map(keyMetadata -> {
          Signature signatureWithKeyMetadata = delegatedSignatureService.multiSign(keyMetadata, unsignedPayment);
          return SignerWrapper.of(Signer.builder()
            .account(toAddress(keyMetadata, delegatedSignatureService))
            .signingPublicKey(toPublicKey(keyMetadata, delegatedSignatureService).base16Value())
            .transactionSignature(signatureWithKeyMetadata.base16Value())
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

    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transaction().hash()
    );
  }

  private PublicKey toPublicKey(final KeyMetadata keyMetadata,
    final DelegatedSignatureService delegatedSignatureService) {
    return delegatedSignatureService.getPublicKey(keyMetadata);
  }

  private Address toAddress(final KeyMetadata keyMetadata, final DelegatedSignatureService delegatedSignatureService) {
    return BcAddressUtils.getInstance().deriveAddress(toPublicKey(keyMetadata, delegatedSignatureService));
  }
}
