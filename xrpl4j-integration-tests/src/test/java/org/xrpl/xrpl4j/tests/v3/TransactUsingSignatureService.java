package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.calculateFeeDynamically;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
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
 * Integration tests for submitting payment transactions to the XRPL using a {@link SignatureService} for all signing
 * operations.
 */
public class TransactUsingSignatureService extends AbstractIT {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @BeforeEach
  public void setUp() {
  }

  @Test
  public void sendPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    final DelegatedSignatureService delegatedSignatureService = this.constructDelegatedSignatureServiceEd25519();

    final KeyMetadata sourceKeyMetadata = constructKeyMetadata("sourceWallet");
    final PublicKey sourceWalletPublicKey = delegatedSignatureService.getPublicKey(sourceKeyMetadata);
    final Address sourceWalletAddress = BcAddressUtils.getInstance().deriveAddress(sourceWalletPublicKey);
    this.fundAccount(sourceWalletAddress);

    final KeyMetadata destinationKeyMetadata = constructKeyMetadata("destinationWallet");
    final PublicKey destinationWalletPublicKey = delegatedSignatureService.getPublicKey(destinationKeyMetadata);
    final Address destinationWalletAddress = BcAddressUtils.getInstance().deriveAddress(destinationWalletPublicKey);
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(calculateFeeDynamically(feeResult))
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.base16Value())
      .build();

    SingleSingedTransaction<Payment> signedTransaction = delegatedSignatureService.sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    final DelegatedSignatureService delegatedSignatureService = this.constructDelegatedSignatureServiceSecp256k1();

    final KeyMetadata sourceKeyMetadata = constructKeyMetadata("sourceWallet");
    final PublicKey sourceWalletPublicKey = delegatedSignatureService.getPublicKey(sourceKeyMetadata);
    final Address sourceWalletAddress = BcAddressUtils.getInstance().deriveAddress(sourceWalletPublicKey);
    this.fundAccount(sourceWalletAddress);

    final KeyMetadata destinationKeyMetadata = constructKeyMetadata("destinationWallet");
    final PublicKey destinationWalletPublicKey = delegatedSignatureService.getPublicKey(destinationKeyMetadata);
    final Address destinationWalletAddress = BcAddressUtils.getInstance().deriveAddress(destinationWalletPublicKey);
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(calculateFeeDynamically(feeResult))
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.base16Value())
      .build();

    SingleSingedTransaction<Payment> transactionWithSignature = delegatedSignatureService
      .sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(transactionWithSignature);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  void multiSigPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create four accounts: one for the source account; two for the signers; one for the destination
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet aliceWallet = createRandomAccountEd25519();
    Wallet bobWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    this.multiSigSendPaymentHelper(sourceWallet, aliceWallet, bobWallet, destinationWallet, signatureService);
  }

  @Test
  void multiSigPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create four accounts: one for the source account; two for the signers; one for the destination
    Wallet sourceWallet = createRandomAccountSecp256k1();
    Wallet aliceWallet = createRandomAccountSecp256k1();
    Wallet bobWallet = createRandomAccountSecp256k1();
    Wallet destinationWallet = createRandomAccountSecp256k1();

    this.multiSigSendPaymentHelper(sourceWallet, aliceWallet, bobWallet, destinationWallet, signatureService);
  }

  /**
   * Helper to send a multisign payment using a designated {@link SignatureService}.
   *
   * @param signatureService A particular type of {@link SignatureService} for a given key type.
   */
  private void multiSigSendPaymentHelper(
    final Wallet sourceWallet,
    final Wallet aliceWallet,
    final Wallet bobWallet,
    final Wallet destinationWallet,
    final SignatureService signatureService
  ) throws JsonRpcClientErrorException, JsonProcessingException {

    Objects.requireNonNull(sourceWallet);
    Objects.requireNonNull(aliceWallet);
    Objects.requireNonNull(bobWallet);
    Objects.requireNonNull(destinationWallet);
    Objects.requireNonNull(signatureService);

    /////////////////////////////
    // Wait for all accounts to show up in a validated ledger
    final AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );
    scanForResult(() -> this.getValidatedAccountInfo(aliceWallet.address()));
    scanForResult(() -> this.getValidatedAccountInfo(bobWallet.address()));
    scanForResult(() -> this.getValidatedAccountInfo(destinationWallet.address()));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    FeeResult feeResult = xrplClient.fee();
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(sourceAccountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(aliceWallet.address())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(bobWallet.address())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sourceWallet.privateKey(), signerListSet
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
      () -> this.getValidatedAccountInfo(sourceWallet.address()),
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
      .account(sourceWallet.address())
      .fee(
        FeeUtils.computeMultiSigFee(
          feeResult.drops().openLedgerFee(),
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        )
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(destinationWallet.address())
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys using the "multiSign" method.
    List<SignerWrapper> signers = Lists.newArrayList(aliceWallet, bobWallet).stream()
      .map(wallet -> {
          Signature signedPayment = signatureService.multiSign(wallet.privateKey(), unsignedPayment);
          return SignerWrapper.of(Signer.builder()
            .account(wallet.address())
            .signingPublicKey(wallet.publicKey().base16Value())
            .transactionSignature(signedPayment.base16Value())
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
}
