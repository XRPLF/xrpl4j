package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
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
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
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
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
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
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
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
 * Loan creation (with dual-signing), payments, and cleanup.</p>
 */
public class LendingProtocolIT extends AbstractIT {

  private static final String SUCCESS_STATUS = TransactionResultCodes.TES_SUCCESS;

  private static final ObjectMapper objectMapper =
    ObjectMapperFactory.create();

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
    final Address brokerAddress = brokerKeyPair.publicKey().deriveAddress();
    final Address borrowerAddress = borrowerKeyPair.publicKey().deriveAddress();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBroker(brokerKeyPair, fee);

    // Build the unsigned base LoanSet transaction
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerAddress)
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerAddress)
      .fee(XrpCurrencyAmount.of(UnsignedLong.valueOf(fee.value().longValue() * 3)))
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(AssetAmount.of("1000000"))
      .counterparty(borrowerAddress)
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

    final SingleSignedTransaction<LoanSet> finalTx = SingleSignedTransaction.<LoanSet>builder()
      .unsignedTransaction(unsignedLoanSet)
      .signature(brokerSigned.signature())
      .signedTransaction(signedLoanSet)
      .build();

    submitAndVerifySingleSignedLoanSet(finalTx);
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
    final Address brokerAddress = brokerKeyPair.publicKey().deriveAddress();
    final Address borrowerAddress = borrowerKeyPair.publicKey().deriveAddress();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBroker(brokerKeyPair, fee);
    setupSignerList(borrowerKeyPair, borrowerSigner1KeyPair, borrowerSigner2KeyPair, fee);

    // Build the unsigned base LoanSet transaction
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerAddress)
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerAddress)
      .fee(XrpCurrencyAmount.of(UnsignedLong.valueOf(fee.value().longValue() * 5)))
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(AssetAmount.of("1000000"))
      .counterparty(borrowerAddress)
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

    final SingleSignedTransaction<LoanSet> finalTx = SingleSignedTransaction.<LoanSet>builder()
      .unsignedTransaction(unsignedLoanSet)
      .signature(brokerSigned.signature())
      .signedTransaction(signedLoanSet)
      .build();

    submitAndVerifySingleSignedLoanSet(finalTx);
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
    final Address brokerAddress = brokerKeyPair.publicKey().deriveAddress();
    final Address borrowerAddress = borrowerKeyPair.publicKey().deriveAddress();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBroker(brokerKeyPair, fee);
    setupSignerList(brokerKeyPair, brokerSigner1KeyPair, brokerSigner2KeyPair, fee);

    // Build the unsigned base LoanSet transaction (empty SigningPubKey for multi-sign)
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerAddress)
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerAddress)
      .fee(XrpCurrencyAmount.of(UnsignedLong.valueOf(fee.value().longValue() * 5)))
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(AssetAmount.of("1000000"))
      .counterparty(borrowerAddress)
      .paymentTotal(UnsignedInteger.valueOf(3))
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
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

    submitAndVerifyMultiSignedLoanSet(finalTx);
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
    final Address brokerAddress = brokerKeyPair.publicKey().deriveAddress();
    final Address borrowerAddress = borrowerKeyPair.publicKey().deriveAddress();

    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Hash256 loanBrokerId = setupLoanBroker(brokerKeyPair, fee);
    setupSignerList(brokerKeyPair, brokerSigner1KeyPair, brokerSigner2KeyPair, fee);
    setupSignerList(borrowerKeyPair, borrowerSigner1KeyPair, borrowerSigner2KeyPair, fee);

    // Build the unsigned base LoanSet transaction (empty SigningPubKey for multi-sign)
    final AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerAddress)
    );
    final LoanSet unsignedLoanSet = LoanSet.builder()
      .account(brokerAddress)
      .fee(XrpCurrencyAmount.of(UnsignedLong.valueOf(fee.value().longValue() * 7)))
      .sequence(brokerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(AssetAmount.of("1000000"))
      .counterparty(borrowerAddress)
      .paymentTotal(UnsignedInteger.valueOf(3))
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
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

    submitAndVerifyMultiSignedLoanSet(finalTx);
  }

  // //////////////////////
  // Full lending lifecycle test
  // //////////////////////

  /**
   * Logs the JSON representation of an object for test capture.
   *
   * @param label a short label describing the object
   * @param obj the object to serialize
   * @throws Exception if serialization fails
   */
  private void logJson(String label, Object obj) throws Exception {
    String json = objectMapper
      .writerWithDefaultPrettyPrinter()
      .writeValueAsString(obj);
    logger.info("{} JSON: {}", label, json);
  }

  /**
   * Full lending lifecycle with IOU asset: create vault, create loan broker, deposit cover,
   * create loan (dual-signed), make payments, default, delete loan, withdraw cover, delete broker.
   */
  @Test
  void lendingProtocolWithIouAsset() throws Exception {
    // Step 1: Create accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    KeyPair depositorKeyPair = createRandomAccountEd25519();
    KeyPair borrowerKeyPair = createRandomAccountEd25519();

    // The Vault Owner and Loan Broker must be on the same account
    final KeyPair loanBrokerKeyPair = vaultOwnerKeyPair;

    FeeResult feeResult = xrplClient.fee();
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();

    final Address issuerAddress = issuerKeyPair.publicKey().deriveAddress();
    final Address vaultOwnerAddress = vaultOwnerKeyPair.publicKey().deriveAddress();
    final Address depositorAddress = depositorKeyPair.publicKey().deriveAddress();
    final Address borrowerAddress = borrowerKeyPair.publicKey().deriveAddress();

    // Step 2: Enable clawback on issuer account
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerAddress)
    );

    AccountSet enableClawback = AccountSet.builder()
      .account(issuerAddress)
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
      .issuer(issuerAddress)
      .build();

    IssuedCurrencyAmount usdAmount = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerAddress)
      .value("1000000")
      .build();

    // Step 4: Set up trust lines for depositor and borrower
    createTrustLine(depositorKeyPair, usdAmount, fee);
    createTrustLine(borrowerKeyPair, usdAmount, fee);

    // Also set up trust line for the vault owner/loan broker (for cover deposit)
    createTrustLine(vaultOwnerKeyPair, usdAmount, fee);

    // Step 5: Fund accounts with IOU
    sendIssuedCurrency(issuerKeyPair, depositorKeyPair,
      IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("500000").build(), fee);
    sendIssuedCurrency(issuerKeyPair, borrowerKeyPair,
      IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("100000").build(), fee);
    sendIssuedCurrency(issuerKeyPair, vaultOwnerKeyPair,
      IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("200000").build(), fee);

    // ========== VAULT CREATION ==========
    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerAddress)
      .fee(fee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(usdIssue)
      .assetsMaximum(AssetAmount.of("500000"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(), vaultCreate
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
          .owner(vaultOwnerAddress)
          .seq(vaultCreate.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 vaultId = vaultLedgerEntry.node().index();
    logger.info("Vault created: {}", vaultId);

    // Deposit into vault
    AccountInfoResult depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorAddress)
    );

    VaultDeposit vaultDeposit = VaultDeposit.builder()
      .account(depositorAddress)
      .fee(fee)
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("100000").build())
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
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(vaultOwnerAddress)
      .fee(fee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .debtMaximum(AssetAmount.of("250000"))
      .managementFeeRate(UnsignedInteger.valueOf(10000))
      .data(LoanBrokerData.of("010203"))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerSet> signedLoanBrokerSet = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanBrokerSet
    );
    SubmitResult<LoanBrokerSet> loanBrokerSetResult =
      xrplClient.submit(signedLoanBrokerSet);
    logJson("LoanBrokerSet SubmitResult", loanBrokerSetResult);
    assertThat(loanBrokerSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanBrokerSet.hash(), LoanBrokerSet.class)
    );
    logInfo(loanBrokerSet.transactionType(), signedLoanBrokerSet.hash());

    // Get LoanBroker ID via ledger_entry
    LedgerEntryResult<LoanBrokerObject> loanBrokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loanBroker(
        LoanBrokerLedgerEntryParams.builder()
          .owner(vaultOwnerAddress)
          .seq(loanBrokerSet.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 loanBrokerId = loanBrokerEntry.node().index();
    LoanBrokerObject loanBrokerObject = loanBrokerEntry.node();
    logJson("LoanBrokerObject after creation", loanBrokerObject);
    logger.info("LoanBroker created: {}", loanBrokerId);

    // Verify LoanBroker fields
    assertThat(loanBrokerObject.owner()).isEqualTo(vaultOwnerAddress);
    assertThat(loanBrokerObject.vaultId()).isEqualTo(vaultId);
    assertThat(loanBrokerObject.debtMaximum()).isNotEmpty().get().isEqualTo(AssetAmount.of("250000"));
    assertThat(loanBrokerObject.managementFeeRate()).isNotEmpty()
      .get().isEqualTo(UnsignedInteger.valueOf(10000));
    assertThat(loanBrokerObject.data()).isNotEmpty().get().isEqualTo(LoanBrokerData.of("010203"));

    // Verify via helper
    assertLoanBrokerEntryEqualsObjectFromAccountObjects(vaultOwnerAddress, loanBrokerSet.sequence());

    // ========== LOAN BROKER COVER DEPOSIT ==========
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    LoanBrokerCoverDeposit coverDeposit = LoanBrokerCoverDeposit.builder()
      .account(vaultOwnerAddress)
      .fee(fee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("50000").build())
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerCoverDeposit> signedCoverDeposit = signatureService.sign(
      loanBrokerKeyPair.privateKey(), coverDeposit
    );
    SubmitResult<LoanBrokerCoverDeposit> coverDepositResult =
      xrplClient.submit(signedCoverDeposit);
    logJson("CoverDeposit SubmitResult", coverDepositResult);
    assertThat(coverDepositResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedCoverDeposit.hash(), LoanBrokerCoverDeposit.class)
    );
    logInfo(coverDeposit.transactionType(), signedCoverDeposit.hash());

    // Verify CoverAvailable updated
    loanBrokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanBrokerId, LoanBrokerObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(loanBrokerEntry.node().coverAvailable()).isNotEmpty().get().isEqualTo(AssetAmount.of("50000"));

    // ========== LOAN SET (Dual-Signed) ==========
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    // Build the LoanSet transaction with broker's signing key
    // LoanSet is larger due to CounterpartySignature, so use a higher fee
    XrpCurrencyAmount loanSetFee = XrpCurrencyAmount.of(
      UnsignedLong.valueOf(fee.value().longValue() * 3)
    );
    LoanSet loanSetBase = LoanSet.builder()
      .account(vaultOwnerAddress)
      .fee(loanSetFee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .principalRequested(AssetAmount.of("50000"))
      .counterparty(borrowerAddress)
      .paymentTotal(UnsignedInteger.valueOf(3))
      .data(LoanData.of("AABBCC"))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    // Dual-signing: borrower signs the same transaction payload
    // Since CounterpartySignature has isSigningField=false, both parties sign the same bytes
    SingleSignedTransaction<LoanSet> borrowerSigned = signatureService.sign(
      borrowerKeyPair.privateKey(), loanSetBase
    );

    // Extract borrower's signature and build CounterpartySignature
    CounterpartySignature counterpartySig = CounterpartySignature.builder()
      .signingPubKey(borrowerKeyPair.publicKey().base16Value())
      .txnSignature(borrowerSigned.signature().base16Value())
      .build();

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
    logJson("LoanSet SubmitResult", loanSetResult);
    assertThat(loanSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanSet.hash(), LoanSet.class)
    );
    logInfo(loanSetFinal.transactionType(), signedLoanSet.hash());

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
    logJson("LoanObject after creation", loanObject);
    logger.info("Loan created: {}", loanId);

    // Verify Loan fields
    assertThat(loanObject.borrower()).isEqualTo(borrowerAddress);
    assertThat(loanObject.loanBrokerId()).isEqualTo(loanBrokerId);
    assertThat(loanObject.principalOutstanding()).isNotEmpty();
    assertThat(loanObject.paymentRemaining()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(3));

    // Verify via helper
    assertLoanEntryEqualsObjectFromAccountObjects(borrowerAddress, loanBrokerId,
      loanBrokerObject.loanSequence());

    // ========== LOAN PAY ==========
    AccountInfoResult borrowerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(borrowerAddress)
    );

    LoanPay loanPay = LoanPay.builder()
      .account(borrowerAddress)
      .fee(fee)
      .sequence(borrowerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("25000").build())
      .signingPublicKey(borrowerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanPay> signedLoanPay = signatureService.sign(
      borrowerKeyPair.privateKey(), loanPay
    );
    SubmitResult<LoanPay> loanPayResult =
      xrplClient.submit(signedLoanPay);
    logJson("LoanPay SubmitResult", loanPayResult);
    assertThat(loanPayResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanPay.hash(), LoanPay.class)
    );
    logInfo(loanPay.transactionType(), signedLoanPay.hash());

    // Verify loan state after payment
    loanEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanId, LoanObject.class, LedgerSpecifier.VALIDATED)
    );
    LoanObject paidLoan = loanEntry.node();
    logJson("LoanObject after first payment", paidLoan);
    assertThat(paidLoan.paymentRemaining()).isNotEmpty()
      .get().isEqualTo(UnsignedInteger.valueOf(2));

    // ========== LOAN MANAGE - Impair ==========
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    LoanManage loanManageImpair = LoanManage.builder()
      .account(vaultOwnerAddress)
      .fee(fee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .flags(LoanManageFlags.of(LoanManageFlags.LOAN_IMPAIR.getValue()))
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanManage> signedImpair = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanManageImpair
    );
    SubmitResult<LoanManage> impairResult =
      xrplClient.submit(signedImpair);
    logJson("LoanManage Impair SubmitResult", impairResult);
    assertThat(impairResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedImpair.hash(), LoanManage.class)
    );
    logInfo(loanManageImpair.transactionType(), signedImpair.hash());

    // Verify loan is impaired
    loanEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(loanId, LoanObject.class, LedgerSpecifier.VALIDATED)
    );
    logJson("LoanObject after impairment", loanEntry.node());
    assertThat(loanEntry.node().flags().lsfLoanImpaired()).isTrue();

    // ========== LOAN PAY - Full payment (clears impairment) ==========
    borrowerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(borrowerAddress)
    );

    // Get remaining outstanding to pay in full
    String remainingOutstanding = loanEntry.node().totalValueOutstanding()
      .map(AssetAmount::value)
      .orElse("50000");

    LoanPay fullPayment = LoanPay.builder()
      .account(borrowerAddress)
      .fee(fee)
      .sequence(borrowerAccountInfo.accountData().sequence())
      .loanId(loanId)
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value(remainingOutstanding).build())
      .flags(LoanPayFlags.of(LoanPayFlags.LOAN_FULL_PAYMENT.getValue()))
      .signingPublicKey(borrowerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanPay> signedFullPayment = signatureService.sign(
      borrowerKeyPair.privateKey(), fullPayment
    );
    SubmitResult<LoanPay> fullPayResult =
      xrplClient.submit(signedFullPayment);
    logJson("LoanPay Full SubmitResult", fullPayResult);
    assertThat(fullPayResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedFullPayment.hash(), LoanPay.class)
    );
    logInfo(fullPayment.transactionType(), signedFullPayment.hash());

    // ========== LOAN DELETE ==========
    borrowerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(borrowerAddress)
    );

    LoanDelete loanDelete = LoanDelete.builder()
      .account(borrowerAddress)
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
    logJson("LoanDelete SubmitResult", loanDeleteResult);
    assertThat(loanDeleteResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanDelete.hash(), LoanDelete.class)
    );
    logInfo(loanDelete.transactionType(), signedLoanDelete.hash());

    // Verify loan is deleted
    assertThat(this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.LOAN)
            .account(borrowerAddress)
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
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    LoanBrokerCoverWithdraw coverWithdraw = LoanBrokerCoverWithdraw.builder()
      .account(vaultOwnerAddress)
      .fee(fee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("25000").build())
      .destination(vaultOwnerAddress)
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerCoverWithdraw> signedCoverWithdraw = signatureService.sign(
      loanBrokerKeyPair.privateKey(), coverWithdraw
    );
    SubmitResult<LoanBrokerCoverWithdraw> coverWithdrawResult =
      xrplClient.submit(signedCoverWithdraw);
    logJson("CoverWithdraw SubmitResult", coverWithdrawResult);
    assertThat(coverWithdrawResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedCoverWithdraw.hash(), LoanBrokerCoverWithdraw.class)
    );
    logInfo(coverWithdraw.transactionType(), signedCoverWithdraw.hash());

    // ========== LOAN BROKER COVER CLAWBACK (issuer claws back remaining cover) ==========
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerAddress)
    );

    LoanBrokerCoverClawback coverClawback = LoanBrokerCoverClawback.builder()
      .account(issuerAddress)
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(issuerAddress).value("25000").build())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerCoverClawback> signedCoverClawback = signatureService.sign(
      issuerKeyPair.privateKey(), coverClawback
    );
    SubmitResult<LoanBrokerCoverClawback> coverClawbackResult =
      xrplClient.submit(signedCoverClawback);
    logJson("CoverClawback SubmitResult", coverClawbackResult);
    assertThat(coverClawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedCoverClawback.hash(), LoanBrokerCoverClawback.class)
    );
    logInfo(coverClawback.transactionType(), signedCoverClawback.hash());

    // ========== LOAN BROKER DELETE ==========
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerAddress)
    );

    LoanBrokerDelete loanBrokerDelete = LoanBrokerDelete.builder()
      .account(vaultOwnerAddress)
      .fee(fee)
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .loanBrokerId(loanBrokerId)
      .signingPublicKey(loanBrokerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<LoanBrokerDelete> signedLoanBrokerDelete = signatureService.sign(
      loanBrokerKeyPair.privateKey(), loanBrokerDelete
    );
    SubmitResult<LoanBrokerDelete> loanBrokerDeleteResult =
      xrplClient.submit(signedLoanBrokerDelete);
    logJson("LoanBrokerDelete SubmitResult", loanBrokerDeleteResult);
    assertThat(loanBrokerDeleteResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signedLoanBrokerDelete.hash(), LoanBrokerDelete.class)
    );
    logInfo(loanBrokerDelete.transactionType(), signedLoanBrokerDelete.hash());

    // Verify loan broker is deleted
    assertThat(this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.LOAN_BROKER)
            .account(vaultOwnerAddress)
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

  private void submitAndVerifySingleSignedLoanSet(SingleSignedTransaction<LoanSet> signedTx)
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

    logInfo(signedTx.signedTransaction().transactionType(), signedTx.hash());
  }

  private void submitAndVerifyMultiSignedLoanSet(MultiSignedTransaction<LoanSet> multiSignedTx)
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

    logInfo(multiSignedTx.signedTransaction().transactionType(), multiSignedTx.hash());
  }

  /**
   * Creates an XRP vault and deposits funds into it. Returns the vault ID.
   */
  private Hash256 setupXrpVault(KeyPair ownerKeyPair, XrpCurrencyAmount fee)
    throws JsonRpcClientErrorException, JsonProcessingException {
    final Address ownerAddress = ownerKeyPair.publicKey().deriveAddress();
    final KeyPair depositorKeyPair = createRandomAccountEd25519();

    AccountInfoResult ownerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(ownerAddress)
    );

    final VaultCreate vaultCreate = VaultCreate.builder()
      .account(ownerAddress)
      .fee(fee)
      .sequence(ownerAccountInfo.accountData().sequence())
      .asset(Issue.XRP)
      .assetsMaximum(AssetAmount.of("10000000000"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .signingPublicKey(ownerKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      ownerKeyPair.privateKey(), vaultCreate
    );
    final SubmitResult<VaultCreate> vaultResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class));
    logInfo(vaultCreate.transactionType(), signedVaultCreate.hash());

    final LedgerEntryResult<VaultObject> vaultEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder().owner(ownerAddress).seq(vaultCreate.sequence()).build(),
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
    logInfo(vaultDeposit.transactionType(), signedDeposit.hash());

    return vaultId;
  }

  /**
   * Creates a loan broker on the given vault and deposits cover. Returns the LoanBroker ID.
   */
  private Hash256 setupLoanBroker(KeyPair brokerKeyPair, XrpCurrencyAmount fee)
    throws JsonRpcClientErrorException, JsonProcessingException {
    final Address brokerAddress = brokerKeyPair.publicKey().deriveAddress();
    final Hash256 vaultId = setupXrpVault(brokerKeyPair, fee);

    AccountInfoResult brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerAddress)
    );

    final LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(brokerAddress)
      .fee(fee)
      .sequence(brokerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .debtMaximum(AssetAmount.of("5000000000"))
      .signingPublicKey(brokerKeyPair.publicKey())
      .build();

    final SingleSignedTransaction<LoanBrokerSet> signedBrokerSet = signatureService.sign(
      brokerKeyPair.privateKey(), loanBrokerSet
    );
    final SubmitResult<LoanBrokerSet> brokerSetResult = xrplClient.submit(signedBrokerSet);
    assertThat(brokerSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signedBrokerSet.hash(), LoanBrokerSet.class));
    logInfo(loanBrokerSet.transactionType(), signedBrokerSet.hash());

    final LedgerEntryResult<LoanBrokerObject> brokerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.loanBroker(
        LoanBrokerLedgerEntryParams.builder().owner(brokerAddress).seq(loanBrokerSet.sequence()).build(),
        LedgerSpecifier.VALIDATED
      )
    );
    final Hash256 loanBrokerId = brokerEntry.node().index();

    brokerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(brokerAddress)
    );

    final LoanBrokerCoverDeposit coverDeposit = LoanBrokerCoverDeposit.builder()
      .account(brokerAddress)
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
    logInfo(coverDeposit.transactionType(), signedCoverDeposit.hash());

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
    logInfo(signerListSet.transactionType(), signedSignerListSet.hash());
  }

  /**
   * Typo-compatible AssertionError to match SingleAssetVaultIT pattern.
   */
  private static class AssertionError extends RuntimeException {
    AssertionError(String message) {
      super(message);
    }
  }
}
