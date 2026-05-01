package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.LoanBrokerLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.LoanLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.VaultLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.LoanManageFlags;
import org.xrpl.xrpl4j.model.flags.LoanPayFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.LoanBrokerObject;
import org.xrpl.xrpl4j.model.ledger.LoanObject;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.ledger.VaultObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.CounterpartySignature;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerCoverClawback;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerCoverDeposit;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerCoverWithdraw;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerData;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerDelete;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerSet;
import org.xrpl.xrpl4j.model.transactions.LoanData;
import org.xrpl.xrpl4j.model.transactions.LoanDelete;
import org.xrpl.xrpl4j.model.transactions.LoanManage;
import org.xrpl.xrpl4j.model.transactions.LoanPay;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.VaultCreate;
import org.xrpl.xrpl4j.model.transactions.VaultDeposit;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration tests for the Lending Protocol (XLS-66) transactions and ledger objects.
 *
 * <p>Tests exercise the full lifecycle: Vault creation, LoanBroker management,
 * Loan creation (with dual-signing), and payments.</p>
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "LendingProtocolIT only runs on local rippled node and Devnet")
public class LendingProtocolIT extends AbstractIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  // //////////////////////
  // LoanSet counterparty signing tests (XRP vault, broker initiates, borrower is counterparty)
  // //////////////////////

  // //////////////////////
  // Test 1: Broker single-signs, Borrower (counterparty) single-signs
  // //////////////////////

  @Test
  void loanSetWithSingleSignBrokerAndSingleSignCounterparty()
    throws JsonRpcClientErrorException, JsonProcessingException {

    final KeyPair brokerKeyPair = createRandomAccountEd25519();
    final KeyPair borrowerKeyPair = createRandomAccountEd25519();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBrokerAndXrpVault(brokerKeyPair, fee);

    // Build the unsigned base LoanSet transaction
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeLoanSetNetworkFees(feeResult, UnsignedInteger.ZERO, UnsignedInteger.ZERO).recommendedFee())
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(Amount.of("1000000"))
      .counterparty(borrowerKeyPair.publicKey().deriveAddress())
      .paymentTotal(UnsignedInteger.valueOf(3))
      .signingPublicKey(brokerKeyPair.publicKey())
      .build();

    // Broker (first-party) single-signs the unsigned transaction
    final SingleSignedTransaction<LoanSet> brokerSigned = signatureService.sign(
      brokerKeyPair.privateKey(), unsignedLoanSet
    );

    // Borrower (counterparty) single-signs the same unsigned transaction
    final Signature counterpartySig = signatureService.counterpartySign(
      borrowerKeyPair.privateKey(), unsignedLoanSet
    );
    final CounterpartySignature counterpartySignature = CounterpartySignature.of(
      borrowerKeyPair.publicKey(), counterpartySig
    );

    // Assemble the final transaction with both signatures
    final LoanSet signedLoanSet = LoanSet.builder().from(unsignedLoanSet)
      .counterpartySignature(counterpartySignature)
      .transactionSignature(brokerSigned.signature())
      .build();

    final LoanSet unsignedWithCounterparty = LoanSet.builder().from(unsignedLoanSet)
      .counterpartySignature(counterpartySignature)
      .build();

    final SingleSignedTransaction<LoanSet> finalTx = SingleSignedTransaction.<LoanSet>builder()
      .unsignedTransaction(unsignedWithCounterparty)
      .signature(brokerSigned.signature())
      .signedTransaction(signedLoanSet)
      .build();

    submitSingleSignedLoanSet(finalTx);
  }

  // //////////////////////
  // Test 2: Broker single-signs, Borrower (counterparty) multi-signs
  // //////////////////////

  @Test
  void loanSetWithSingleSignBrokerAndMultiSignCounterparty()
    throws JsonRpcClientErrorException, JsonProcessingException {

    final KeyPair brokerKeyPair = createRandomAccountEd25519();
    final KeyPair borrowerKeyPair = createRandomAccountEd25519();
    final KeyPair borrowerSigner1KeyPair = createRandomAccountEd25519();
    final KeyPair borrowerSigner2KeyPair = createRandomAccountEd25519();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBrokerAndXrpVault(brokerKeyPair, fee);
    setupSignerList(borrowerKeyPair, borrowerSigner1KeyPair, borrowerSigner2KeyPair, fee);

    // Build the unsigned base LoanSet transaction
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeLoanSetNetworkFees(
        feeResult, UnsignedInteger.ZERO, UnsignedInteger.valueOf(2)
      ).recommendedFee())
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(Amount.of("1000000"))
      .counterparty(borrowerKeyPair.publicKey().deriveAddress())
      .paymentTotal(UnsignedInteger.valueOf(3))
      .signingPublicKey(brokerKeyPair.publicKey())
      .build();

    // Broker (first-party) single-signs the unsigned transaction
    final SingleSignedTransaction<LoanSet> brokerSigned = signatureService.sign(
      brokerKeyPair.privateKey(), unsignedLoanSet
    );

    // Borrower (counterparty) multi-signs the same unsigned transaction
    final Set<Signer> counterpartySigners = Lists.newArrayList(
      borrowerSigner1KeyPair, borrowerSigner2KeyPair
    ).stream().map(signerKeyPair -> Signer.builder()
      .signingPublicKey(signerKeyPair.publicKey())
      .transactionSignature(signatureService.counterpartyMultiSign(signerKeyPair.privateKey(), unsignedLoanSet))
      .build()
    ).collect(Collectors.toSet());

    final CounterpartySignature counterpartySignature = CounterpartySignature.of(counterpartySigners);

    // Assemble the final transaction with both signatures
    final LoanSet signedLoanSet = LoanSet.builder().from(unsignedLoanSet)
      .counterpartySignature(counterpartySignature)
      .transactionSignature(brokerSigned.signature())
      .build();

    final LoanSet unsignedWithCounterparty = LoanSet.builder().from(unsignedLoanSet)
      .counterpartySignature(counterpartySignature)
      .build();

    final SingleSignedTransaction<LoanSet> finalTx = SingleSignedTransaction.<LoanSet>builder()
      .unsignedTransaction(unsignedWithCounterparty)
      .signature(brokerSigned.signature())
      .signedTransaction(signedLoanSet)
      .build();

    submitSingleSignedLoanSet(finalTx);
  }

  // //////////////////////
  // Test 3: Broker multi-signs, Borrower (counterparty) single-signs
  // //////////////////////

  @Test
  void loanSetWithMultiSignBrokerAndSingleSignCounterparty()
    throws JsonRpcClientErrorException, JsonProcessingException {

    final KeyPair brokerKeyPair = createRandomAccountEd25519();
    final KeyPair brokerSigner1KeyPair = createRandomAccountEd25519();
    final KeyPair brokerSigner2KeyPair = createRandomAccountEd25519();
    final KeyPair borrowerKeyPair = createRandomAccountEd25519();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBrokerAndXrpVault(brokerKeyPair, fee);
    setupSignerList(brokerKeyPair, brokerSigner1KeyPair, brokerSigner2KeyPair, fee);

    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeLoanSetNetworkFees(
        feeResult, UnsignedInteger.valueOf(2), UnsignedInteger.ZERO
      ).recommendedFee())
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(Amount.of("1000000"))
      .counterparty(borrowerKeyPair.publicKey().deriveAddress())
      .paymentTotal(UnsignedInteger.valueOf(3))
      .build();

    // Broker (first-party) multi-signs the unsigned transaction
    final Set<Signer> brokerSigners = Lists.newArrayList(
      brokerSigner1KeyPair, brokerSigner2KeyPair
    ).stream().map(signerKeyPair -> Signer.builder()
      .signingPublicKey(signerKeyPair.publicKey())
      .transactionSignature(signatureService.multiSign(signerKeyPair.privateKey(), unsignedLoanSet))
      .build()
    ).collect(Collectors.toSet());

    // Borrower (counterparty) single-signs the same unsigned transaction
    final Signature counterpartySig = signatureService.counterpartySign(
      borrowerKeyPair.privateKey(), unsignedLoanSet
    );
    final CounterpartySignature counterpartySignature = CounterpartySignature.of(
      borrowerKeyPair.publicKey(), counterpartySig
    );

    // Assemble the final multi-signed transaction with counterparty signature
    final LoanSet unsignedWithCounterparty = LoanSet.builder().from(unsignedLoanSet)
      .counterpartySignature(counterpartySignature)
      .build();

    final MultiSignedTransaction<LoanSet> finalTx = MultiSignedTransaction.<LoanSet>builder()
      .unsignedTransaction(unsignedWithCounterparty)
      .signerSet(brokerSigners)
      .build();

    submitMultiSignedLoanSet(finalTx);
  }

  // //////////////////////
  // Test 4: Broker multi-signs, Borrower (counterparty) multi-signs
  // //////////////////////

  @Test
  void loanSetWithMultiSignBrokerAndMultiSignCounterparty()
    throws JsonRpcClientErrorException, JsonProcessingException {

    final KeyPair brokerKeyPair = createRandomAccountEd25519();
    final KeyPair brokerSigner1KeyPair = createRandomAccountEd25519();
    final KeyPair brokerSigner2KeyPair = createRandomAccountEd25519();
    final KeyPair borrowerKeyPair = createRandomAccountEd25519();
    final KeyPair borrowerSigner1KeyPair = createRandomAccountEd25519();
    final KeyPair borrowerSigner2KeyPair = createRandomAccountEd25519();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBrokerAndXrpVault(brokerKeyPair, fee);
    setupSignerList(brokerKeyPair, brokerSigner1KeyPair, brokerSigner2KeyPair, fee);
    setupSignerList(borrowerKeyPair, borrowerSigner1KeyPair, borrowerSigner2KeyPair, fee);

    // Build the unsigned base LoanSet transaction
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeLoanSetNetworkFees(
        feeResult, UnsignedInteger.valueOf(2), UnsignedInteger.valueOf(2)
      ).recommendedFee())
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(Amount.of("1000000"))
      .counterparty(borrowerKeyPair.publicKey().deriveAddress())
      .paymentTotal(UnsignedInteger.valueOf(3))
      .build();

    // Broker (first-party) multi-signs the unsigned transaction
    final Set<Signer> brokerSigners = Lists.newArrayList(
      brokerSigner1KeyPair, brokerSigner2KeyPair
    ).stream().map(signerKeyPair -> Signer.builder()
      .signingPublicKey(signerKeyPair.publicKey())
      .transactionSignature(signatureService.multiSign(signerKeyPair.privateKey(), unsignedLoanSet))
      .build()
    ).collect(Collectors.toSet());

    // Borrower (counterparty) multi-signs the same unsigned transaction
    final Set<Signer> counterpartySigners = Lists.newArrayList(
      borrowerSigner1KeyPair, borrowerSigner2KeyPair
    ).stream().map(signerKeyPair -> Signer.builder()
      .signingPublicKey(signerKeyPair.publicKey())
      .transactionSignature(signatureService.counterpartyMultiSign(signerKeyPair.privateKey(), unsignedLoanSet))
      .build()
    ).collect(Collectors.toSet());

    final CounterpartySignature counterpartySignature = CounterpartySignature.of(counterpartySigners);

    // Assemble the final multi-signed transaction with counterparty signature
    final LoanSet unsignedWithCounterparty = LoanSet.builder().from(unsignedLoanSet)
      .counterpartySignature(counterpartySignature)
      .build();

    final MultiSignedTransaction<LoanSet> finalTx = MultiSignedTransaction.<LoanSet>builder()
      .unsignedTransaction(unsignedWithCounterparty)
      .signerSet(brokerSigners)
      .build();

    submitMultiSignedLoanSet(finalTx);
  }

  // //////////////////////
  // Full lending lifecycle test
  // //////////////////////

  /**
   * Full lending lifecycle with IOU asset: create vault, create loan broker, deposit cover,
   * create loan (dual-signed), make payments, default, delete loan, withdraw cover, delete broker.
   */
  @Test
  void lendingProtocolLifecycle() throws Exception {
    // Step 1: Create accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    // Step 2: Enable clawback on issuer account
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet enableClawback = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedEnableClawback = signatureService.sign(
      issuerKeyPair.privateKey(), enableClawback
    );
    SubmitResult<AccountSet> enableClawbackResult = xrplClient.submit(signedEnableClawback);
    assertThat(enableClawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedEnableClawback.hash(), AccountSet.class)
    );

    // Enable default ripple on issuer account
    setDefaultRipple(issuerKeyPair, feeResult);

    // Step 3: Define IOU asset
    final IouIssue usdIssue = IouIssue.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .build();

    IssuedCurrencyAmount usdAmount = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .value("1000000")
      .build();

    KeyPair loanBrokerKeyPair = createRandomAccountEd25519();
    KeyPair depositorKeyPair = createRandomAccountEd25519();
    KeyPair borrowerKeyPair = createRandomAccountEd25519();

    // Step 4: Set up trust lines for depositor and borrower
    createTrustLine(depositorKeyPair, usdAmount, fee);
    createTrustLine(borrowerKeyPair, usdAmount, fee);

    // Also set up trust line for the vault owner/loan broker (for cover deposit)
    createTrustLine(loanBrokerKeyPair, usdAmount, fee);

    // Step 5: Fund accounts with IOU
    sendIssuedCurrency(issuerKeyPair, depositorKeyPair,
      IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("500000").build(), fee);
    sendIssuedCurrency(issuerKeyPair, borrowerKeyPair,
      IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("100000").build(), fee);
    sendIssuedCurrency(issuerKeyPair, loanBrokerKeyPair,
      IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("200000").build(), fee);

    // ========== VAULT CREATION ==========
    AccountInfoResult loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .asset(usdIssue)
      .assetsMaximum(Amount.of("500000"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      loanBrokerKeyPair.privateKey(), vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );

    // Get vault ID
    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(loanBrokerKeyPair.publicKey().deriveAddress())
          .seq(vaultCreate.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 vaultId = vaultLedgerEntry.node().index();
    logger.info("Vault created: {}", vaultId);

    // Deposit into vault
    AccountInfoResult depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit vaultDeposit = VaultDeposit.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("100000").build())
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit = signatureService.sign(
      depositorKeyPair.privateKey(), vaultDeposit
    );
    xrplClient.submit(signedDeposit);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit.hash(), VaultDeposit.class)
    );

    // ========== LOAN BROKER SET ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .debtMaximum(Amount.of("250000"))
      .managementFeeRate(UnsignedInteger.valueOf(10000))
      .data(LoanBrokerData.of("010203"))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerSet> signedLoanBrokerSet = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanBrokerSet
    );
    SubmitResult<LoanBrokerSet> loanBrokerSetResult =
      xrplClient.submit(signedLoanBrokerSet);
    assertThat(loanBrokerSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanBrokerSet.hash(), LoanBrokerSet.class)
    );

    // Get LoanBroker ID via ledger_entry
    LedgerEntryResult<LoanBrokerObject> loanBrokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loanBroker(
        LoanBrokerLedgerEntryParams.builder()
          .owner(loanBrokerKeyPair.publicKey().deriveAddress())
          .seq(loanBrokerSet.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 loanBrokerId = loanBrokerEntry.node().index();
    LoanBrokerObject loanBrokerObject = loanBrokerEntry.node();
    logger.info("LoanBroker created: {}", loanBrokerId);

    // Verify LoanBroker fields
    assertThat(loanBrokerObject.owner()).isEqualTo(loanBrokerKeyPair.publicKey().deriveAddress());
    assertThat(loanBrokerObject.vaultId()).isEqualTo(vaultId);
    assertThat(loanBrokerObject.debtMaximum()).isNotEmpty().get().isEqualTo(Amount.of("250000"));
    assertThat(loanBrokerObject.managementFeeRate()).isNotEmpty()
      .get().isEqualTo(UnsignedInteger.valueOf(10000));
    assertThat(loanBrokerObject.data()).isNotEmpty().get().isEqualTo(LoanBrokerData.of("010203"));

    // Verify via helper
    assertLoanBrokerEntryEqualsObjectFromAccountObjects(
      loanBrokerKeyPair.publicKey().deriveAddress(), loanBrokerSet.sequence()
    );

    // ========== LOAN BROKER SET - Update ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanBrokerSet loanBrokerUpdate = LoanBrokerSet.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .loanBrokerId(loanBrokerId)
      .debtMaximum(Amount.of("500000"))
      .data(LoanBrokerData.of("AABB"))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerSet> signedLoanBrokerUpdate = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanBrokerUpdate
    );
    SubmitResult<LoanBrokerSet> loanBrokerUpdateResult = xrplClient.submit(signedLoanBrokerUpdate);
    assertThat(loanBrokerUpdateResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanBrokerUpdate.hash(), LoanBrokerSet.class)
    );

    // Verify updated fields
    loanBrokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanBrokerId, LoanBrokerObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(loanBrokerEntry.node().debtMaximum()).isNotEmpty().get().isEqualTo(Amount.of("500000"));
    assertThat(loanBrokerEntry.node().data()).isNotEmpty().get().isEqualTo(LoanBrokerData.of("AABB"));
    // Fixed fields should remain unchanged
    assertThat(loanBrokerEntry.node().managementFeeRate()).isNotEmpty()
      .get().isEqualTo(UnsignedInteger.valueOf(10000));

    // ========== LOAN BROKER COVER DEPOSIT ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanBrokerCoverDeposit coverDeposit = LoanBrokerCoverDeposit.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("50000").build())
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerCoverDeposit> signedCoverDeposit = signatureService.sign(
      loanBrokerKeyPair.privateKey(), coverDeposit
    );
    SubmitResult<LoanBrokerCoverDeposit> coverDepositResult =
      xrplClient.submit(signedCoverDeposit);
    assertThat(coverDepositResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedCoverDeposit.hash(), LoanBrokerCoverDeposit.class)
    );

    // Verify CoverAvailable updated
    loanBrokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanBrokerId, LoanBrokerObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(loanBrokerEntry.node().coverAvailable()).isNotEmpty().get().isEqualTo(Amount.of("50000"));

    // ========== LOAN SET (Dual-Signed) ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    // Build the LoanSet transaction with broker's signing key
    XrpCurrencyAmount loanSetFee = FeeUtils.computeLoanSetNetworkFees(
      feeResult, UnsignedInteger.ZERO, UnsignedInteger.ZERO
    ).recommendedFee();
    LoanSet loanSetBase = LoanSet.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(loanSetFee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(Amount.of("50000"))
      .counterparty(borrowerKeyPair.publicKey().deriveAddress())
      .paymentTotal(UnsignedInteger.valueOf(3))
      .data(LoanData.of("AABBCC"))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    // Borrower counterparty-signs the transaction
    Signature borrowerCounterpartySig = signatureService.counterpartySign(
      borrowerKeyPair.privateKey(), loanSetBase
    );
    CounterpartySignature counterpartySig = CounterpartySignature.of(
      borrowerKeyPair.publicKey(), borrowerCounterpartySig
    );

    // Build final LoanSet with CounterpartySignature
    LoanSet loanSetFinal = LoanSet.builder().from(loanSetBase)
      .counterpartySignature(counterpartySig)
      .build();

    // Broker signs the final transaction (same signable bytes since CounterpartySignature is excluded)
    SingleSignedTransaction<LoanSet> signedLoanSet = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanSetFinal
    );
    SubmitResult<LoanSet> loanSetResult =
      xrplClient.submit(signedLoanSet);
    assertThat(loanSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanSet.hash(), LoanSet.class)
    );

    // Get Loan ID via ledger_entry
    LedgerEntryResult<LoanObject> loanEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loan(
        LoanLedgerEntryParams.builder()
          .loanBrokerId(loanBrokerId)
          .loanSeq(loanBrokerObject.loanSequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 loanId = loanEntry.node().index();
    LoanObject loanObject = loanEntry.node();
    logger.info("Loan created: {}", loanId);

    // Verify Loan fields
    assertThat(loanObject.borrower()).isEqualTo(borrowerKeyPair.publicKey().deriveAddress());
    assertThat(loanObject.loanBrokerId()).isEqualTo(loanBrokerId);
    assertThat(loanObject.principalOutstanding()).isNotEmpty();
    assertThat(loanObject.paymentRemaining()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(3));

    // Verify via helper
    assertLoanEntryEqualsObjectFromAccountObjects(borrowerKeyPair.publicKey().deriveAddress(), loanBrokerId,
      loanBrokerObject.loanSequence());

    // ========== LOAN PAY ==========
    AccountInfoResult borrowerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(borrowerKeyPair.publicKey().deriveAddress())
    );

    LoanPay loanPay = LoanPay.builder()
      .account(borrowerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(borrowerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .amount(IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("25000").build())
      .signingPublicKey(borrowerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanPay> signedLoanPay = signatureService.sign(
      borrowerKeyPair.privateKey(), loanPay
    );
    SubmitResult<LoanPay> loanPayResult =
      xrplClient.submit(signedLoanPay);
    assertThat(loanPayResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanPay.hash(), LoanPay.class)
    );

    // Verify loan state after payment
    loanEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanId, LoanObject.class, LedgerSpecifier.VALIDATED)
    );
    LoanObject paidLoan = loanEntry.node();
    assertThat(paidLoan.paymentRemaining()).isNotEmpty()
      .get().isEqualTo(UnsignedInteger.valueOf(2));

    // ========== LOAN MANAGE - Impair ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanManage loanManageImpair = LoanManage.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .flags(LoanManageFlags.of(LoanManageFlags.LOAN_IMPAIR.getValue()))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanManage> signedImpair = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanManageImpair
    );
    SubmitResult<LoanManage> impairResult =
      xrplClient.submit(signedImpair);
    assertThat(impairResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedImpair.hash(), LoanManage.class)
    );

    // Verify loan is impaired
    loanEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanId, LoanObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(loanEntry.node().flags().lsfLoanImpaired()).isTrue();

    // ========== LOAN MANAGE - Unimpair ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanManage loanManageUnimpair = LoanManage.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .flags(LoanManageFlags.of(LoanManageFlags.LOAN_UNIMPAIR.getValue()))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanManage> signedUnimpair = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanManageUnimpair
    );
    SubmitResult<LoanManage> unimpairResult = xrplClient.submit(signedUnimpair);
    assertThat(unimpairResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedUnimpair.hash(), LoanManage.class)
    );

    // Verify loan is no longer impaired
    loanEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanId, LoanObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(loanEntry.node().flags().lsfLoanImpaired()).isFalse();

    // ========== LOAN PAY - Full payment ==========
    borrowerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(borrowerKeyPair.publicKey().deriveAddress())
    );

    // Get remaining outstanding to pay in full
    String remainingOutstanding = loanEntry.node().totalValueOutstanding()
      .map(Amount::value)
      .orElse("50000");

    LoanPay fullPayment = LoanPay.builder()
      .account(borrowerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(borrowerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .amount(IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value(remainingOutstanding).build())
      .flags(LoanPayFlags.of(LoanPayFlags.LOAN_FULL_PAYMENT.getValue()))
      .signingPublicKey(borrowerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanPay> signedFullPayment = signatureService.sign(
      borrowerKeyPair.privateKey(), fullPayment
    );
    SubmitResult<LoanPay> fullPayResult =
      xrplClient.submit(signedFullPayment);
    assertThat(fullPayResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedFullPayment.hash(), LoanPay.class)
    );

    // ========== LOAN DELETE ==========
    borrowerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(borrowerKeyPair.publicKey().deriveAddress())
    );

    LoanDelete loanDelete = LoanDelete.builder()
      .account(borrowerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(borrowerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .signingPublicKey(borrowerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanDelete> signedLoanDelete = signatureService.sign(
      borrowerKeyPair.privateKey(), loanDelete
    );
    SubmitResult<LoanDelete> loanDeleteResult =
      xrplClient.submit(signedLoanDelete);
    assertThat(loanDeleteResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanDelete.hash(), LoanDelete.class)
    );

    // Verify loan is deleted
    assertThat(this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.LOAN)
            .account(borrowerKeyPair.publicKey().deriveAddress())
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> true
    )).isEmpty();

    // ========== LOAN BROKER COVER WITHDRAW ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanBrokerCoverWithdraw coverWithdraw = LoanBrokerCoverWithdraw.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("25000").build())
      .destination(loanBrokerKeyPair.publicKey().deriveAddress())
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerCoverWithdraw> signedCoverWithdraw = signatureService.sign(
      loanBrokerKeyPair.privateKey(), coverWithdraw
    );
    SubmitResult<LoanBrokerCoverWithdraw> coverWithdrawResult =
      xrplClient.submit(signedCoverWithdraw);
    assertThat(coverWithdrawResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedCoverWithdraw.hash(), LoanBrokerCoverWithdraw.class)
    );

    // ========== LOAN BROKER COVER CLAWBACK (issuer claws back remaining cover) ==========
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    LoanBrokerCoverClawback coverClawback = LoanBrokerCoverClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(IssuedCurrencyAmount.builder().currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress()).value("25000").build())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerCoverClawback> signedCoverClawback = signatureService.sign(
      issuerKeyPair.privateKey(), coverClawback
    );
    SubmitResult<LoanBrokerCoverClawback> coverClawbackResult =
      xrplClient.submit(signedCoverClawback);
    assertThat(coverClawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedCoverClawback.hash(), LoanBrokerCoverClawback.class)
    );

    // ========== LOAN BROKER DELETE ==========
    loanBrokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(loanBrokerKeyPair.publicKey().deriveAddress())
    );

    LoanBrokerDelete loanBrokerDelete = LoanBrokerDelete.builder()
      .account(loanBrokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(loanBrokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerDelete> signedLoanBrokerDelete = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanBrokerDelete
    );
    SubmitResult<LoanBrokerDelete> loanBrokerDeleteResult =
      xrplClient.submit(signedLoanBrokerDelete);
    assertThat(loanBrokerDeleteResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanBrokerDelete.hash(), LoanBrokerDelete.class)
    );

    // Verify loan broker is deleted
    assertThat(this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.LOAN_BROKER)
            .account(loanBrokerKeyPair.publicKey().deriveAddress())
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> true
    )).isEmpty();
  }

  /**
   * Helper method to verify that the LoanBroker from ledger_entry matches account_objects.
   */
  private void assertLoanBrokerEntryEqualsObjectFromAccountObjects(
    Address owner,
    UnsignedInteger ownerSequence
  ) throws JsonRpcClientErrorException {
    LedgerEntryResult<LoanBrokerObject> loanBrokerLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loanBroker(
        LoanBrokerLedgerEntryParams.builder()
          .owner(owner)
          .seq(ownerSequence)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    LoanBrokerObject expectedBroker = loanBrokerLedgerEntry.node();

    LoanBrokerObject brokerFromAccountObjects = this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.LOAN_BROKER)
            .account(owner)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> !result.isEmpty()
    ).stream()
      .filter(obj -> obj instanceof LoanBrokerObject)
      .map(obj -> (LoanBrokerObject) obj)
      .filter(broker -> broker.index().equals(expectedBroker.index()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("LoanBroker not found in account_objects"));

    assertThat(brokerFromAccountObjects).isEqualTo(expectedBroker);

    // Verify by index
    LedgerEntryResult<LoanBrokerObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(expectedBroker.index(), LoanBrokerObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndex.node()).isEqualTo(expectedBroker);

    // Verify untyped
    LedgerEntryResult<LedgerObject> entryByIndexUntyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(expectedBroker.index(), LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndexUntyped.node()).isEqualTo(expectedBroker);
  }

  /**
   * Helper method to verify that the Loan from ledger_entry matches account_objects.
   */
  private void assertLoanEntryEqualsObjectFromAccountObjects(
    Address borrower,
    Hash256 loanBrokerId,
    UnsignedInteger loanSeq
  ) throws JsonRpcClientErrorException {
    LedgerEntryResult<LoanObject> loanLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loan(
        LoanLedgerEntryParams.builder()
          .loanBrokerId(loanBrokerId)
          .loanSeq(loanSeq)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    LoanObject expectedLoan = loanLedgerEntry.node();

    LoanObject loanFromAccountObjects = this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.LOAN)
            .account(borrower)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> !result.isEmpty()
    ).stream()
      .filter(obj -> obj instanceof LoanObject)
      .map(obj -> (LoanObject) obj)
      .filter(loan -> loan.index().equals(expectedLoan.index()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("Loan not found in account_objects"));

    assertThat(loanFromAccountObjects).isEqualTo(expectedLoan);

    // Verify by index
    LedgerEntryResult<LoanObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(expectedLoan.index(), LoanObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndex.node()).isEqualTo(expectedLoan);

    // Verify untyped
    LedgerEntryResult<LedgerObject> entryByIndexUntyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(expectedLoan.index(), LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndexUntyped.node()).isEqualTo(expectedLoan);
  }

  // //////////////////////
  // Counterparty signing helpers
  // //////////////////////

  private void submitSingleSignedLoanSet(SingleSignedTransaction<LoanSet> signedTx)
    throws JsonRpcClientErrorException, JsonProcessingException {
    final SubmitResult<LoanSet> submitResult = xrplClient.submit(signedTx);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    final TransactionResult<LoanSet> validatedResult = this.scanForResult(
      () -> this.getValidatedTransaction(signedTx.hash(), LoanSet.class)
    );
    assertThat(validatedResult.metadata()
      .orElseThrow(() -> new RuntimeException("Metadata is missing."))
      .transactionResult()
    ).isEqualTo(SUCCESS_STATUS);
  }

  private void submitMultiSignedLoanSet(MultiSignedTransaction<LoanSet> multiSignedTx)
    throws JsonRpcClientErrorException, JsonProcessingException {
    final SubmitMultiSignedResult<LoanSet> submitResult = xrplClient.submitMultisigned(multiSignedTx);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    final TransactionResult<LoanSet> validatedResult = this.scanForResult(
      () -> this.getValidatedTransaction(multiSignedTx.hash(), LoanSet.class)
    );
    assertThat(validatedResult.metadata()
      .orElseThrow(() -> new RuntimeException("Metadata is missing."))
      .transactionResult()
    ).isEqualTo(SUCCESS_STATUS);
  }

  /**
   * Creates an XRP vault, deposits funds, creates a loan broker, and deposits cover. Returns the LoanBroker ID.
   */
  private Hash256 setupLoanBrokerAndXrpVault(KeyPair brokerKeyPair, XrpCurrencyAmount fee)
    throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyPair depositorKeyPair = createRandomAccountEd25519();

    AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );

    final VaultCreate vaultCreate = VaultCreate.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(brokerAccountInfo.accountData().sequence())
      .asset(Issue.XRP)
      .assetsMaximum(Amount.of("10000000000"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .signingPublicKey(brokerKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      brokerKeyPair.privateKey(), vaultCreate
    );
    final SubmitResult<VaultCreate> vaultResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class));

    final LedgerEntryResult<VaultObject> vaultEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(brokerKeyPair.publicKey().deriveAddress())
          .seq(vaultCreate.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    final Hash256 vaultId = vaultEntry.node().index();

    final AccountInfoResult depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    final VaultDeposit vaultDeposit = VaultDeposit.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(5000000))
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<VaultDeposit> signedDeposit = signatureService.sign(
      depositorKeyPair.privateKey(), vaultDeposit
    );
    final SubmitResult<VaultDeposit> depositResult = xrplClient.submit(signedDeposit);
    assertThat(depositResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signedDeposit.hash(), VaultDeposit.class));

    brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );

    final LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(brokerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .debtMaximum(Amount.of("5000000000"))
      .signingPublicKey(brokerKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<LoanBrokerSet> signedBrokerSet = signatureService.sign(
      brokerKeyPair.privateKey(), loanBrokerSet
    );
    final SubmitResult<LoanBrokerSet> brokerSetResult = xrplClient.submit(signedBrokerSet);
    assertThat(brokerSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signedBrokerSet.hash(), LoanBrokerSet.class));

    final LedgerEntryResult<LoanBrokerObject> brokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loanBroker(
        LoanBrokerLedgerEntryParams.builder()
          .owner(brokerKeyPair.publicKey().deriveAddress())
          .seq(loanBrokerSet.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    final Hash256 loanBrokerId = brokerEntry.node().index();

    brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerKeyPair.publicKey().deriveAddress())
    );

    final LoanBrokerCoverDeposit coverDeposit = LoanBrokerCoverDeposit.builder()
      .account(brokerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(XrpCurrencyAmount.ofDrops(5000000))
      .signingPublicKey(brokerKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<LoanBrokerCoverDeposit> signedCoverDeposit = signatureService.sign(
      brokerKeyPair.privateKey(), coverDeposit
    );
    final SubmitResult<LoanBrokerCoverDeposit> coverResult = xrplClient.submit(signedCoverDeposit);
    assertThat(coverResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signedCoverDeposit.hash(), LoanBrokerCoverDeposit.class));

    return loanBrokerId;
  }

  /**
   * Sets up a signer list with two signers on the given account (quorum=2, each weight=1).
   */
  private void setupSignerList(
    KeyPair accountKeyPair,
    KeyPair signer1KeyPair,
    KeyPair signer2KeyPair,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    final AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(accountKeyPair.publicKey().deriveAddress())
    );

    final SignerListSet signerListSet = SignerListSet.builder()
      .account(accountKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(accountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(signer1KeyPair.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(signer2KeyPair.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(accountKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      accountKeyPair.privateKey(), signerListSet
    );
    final SubmitResult<SignerListSet> submitResult = xrplClient.submit(signedSignerListSet);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedAccountInfo(accountKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().signerLists().size() == 1
    );
  }
}
