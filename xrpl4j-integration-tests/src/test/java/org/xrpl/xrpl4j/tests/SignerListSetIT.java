package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Comparator;
import java.util.List;
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
    Wallet sourceWallet = createRandomAccount();
    Wallet aliceWallet = createRandomAccount();
    Wallet bobWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    /////////////////////////////
    // Wait for all of the accounts to show up in a validated ledger
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
      .fee(feeResult.drops().openLedgerFee())
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
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/" +
        signerListSetResult.transactionResult().transaction().hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash."))
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
        Transaction.computeMultiSigFee(
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
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" +
        paymentResult.transaction().transaction().hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash."))
    );
  }

  @Test
  void addSignersToSignerListThenDeleteSignerList() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////////
    // Create three accounts, one for the multisign account owner, one for their two friends
    Wallet sourceWallet = createRandomAccount();
    Wallet aliceWallet = createRandomAccount();
    Wallet bobWallet = createRandomAccount();

    /////////////////////////////
    // Wait for all of the accounts to show up in a validated ledger
    AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );
    scanForResult(() -> this.getValidatedAccountInfo(aliceWallet.address()));
    scanForResult(() -> this.getValidatedAccountInfo(bobWallet.address()));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    FeeResult feeResult = xrplClient.fee();
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourceWallet.address())
      .fee(feeResult.drops().openLedgerFee())
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
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/" +
        signerListSetResult.transactionResult().transaction().hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash."))
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
    // Construct a SignerListSet transaction with 0 quorum and an empty list of signer entries to
    // delete the signer list
    SignerListSet deleteSignerList = SignerListSet.builder()
      .from(signerListSet)
      .signerQuorum(UnsignedInteger.ZERO)
      .signerEntries(Lists.emptyList())
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .build();

    SingleSingedTransaction<SignerListSet> signedDeleteSignerList = signatureService.sign(
      sourceWallet.privateKey(), deleteSignerList
    );
    SubmitResult<SignerListSet> signerListDeleteResult = xrplClient.submit(signedDeleteSignerList);
    assertThat(signerListDeleteResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/" +
        signerListDeleteResult.transactionResult().transaction().hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash."))
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the signer list has been deleted
    scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address()),
      infoResult -> infoResult.accountData().signerLists().size() == 0
    );

  }
}
