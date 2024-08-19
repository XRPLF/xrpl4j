package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.AddressConstants;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.XChainModifyBridgeFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.AttestationClaim;
import org.xrpl.xrpl4j.model.ledger.AttestationCreateAccount;
import org.xrpl.xrpl4j.model.ledger.BridgeObject;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.ledger.XChainClaimAttestation;
import org.xrpl.xrpl4j.model.ledger.XChainClaimProofSig;
import org.xrpl.xrpl4j.model.ledger.XChainCreateAccountAttestation;
import org.xrpl.xrpl4j.model.ledger.XChainCreateAccountProofSig;
import org.xrpl.xrpl4j.model.ledger.XChainOwnedClaimIdObject;
import org.xrpl.xrpl4j.model.ledger.XChainOwnedCreateAccountClaimIdObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.XChainAccountCreateCommit;
import org.xrpl.xrpl4j.model.transactions.XChainAddAccountCreateAttestation;
import org.xrpl.xrpl4j.model.transactions.XChainAddClaimAttestation;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainClaim;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCommit;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XChainCreateBridge;
import org.xrpl.xrpl4j.model.transactions.XChainCreateClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainModifyBridge;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableMetaXChainCreateAccountAttestation;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableMetaXChainCreateAccountProofSig;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaXChainOwnedCreateAccountClaimIdObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@DisabledIf(value = "shouldRun", disabledReason = "XChainIT only runs with local rippled nodes.")
public class XChainIT extends AbstractIT {

  static boolean shouldRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useDevnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  TestBridge testBridge;

  {
    try {
      testBridge = setupXrpToXrpBridge();
    } catch (JsonRpcClientErrorException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testXChainAccountCreateCommit() throws JsonRpcClientErrorException, JsonProcessingException {
    XrpCurrencyAmount initialDoorBalance = this.getValidatedAccountInfo(testBridge.bridge().lockingChainDoor())
      .accountData()
      .balance();

    KeyPair sender = this.createRandomAccountEd25519();
    KeyPair destination = Seed.ed25519Seed().deriveKeyPair();
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(10000000);

    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(sender.publicKey().deriveAddress())
    );

    FeeResult feeResult = xrplClient.fee();
    XChainAccountCreateCommit transaction = XChainAccountCreateCommit.builder()
      .account(sender.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(senderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .signingPublicKey(sender.publicKey())
      .xChainBridge(testBridge.bridge())
      .amount(amount)
      .signatureReward(testBridge.signatureReward())
      .destination(destination.publicKey().deriveAddress())
      .build();

    this.signSubmitAndWait(transaction, sender, XChainAccountCreateCommit.class);

    XrpCurrencyAmount finalBalance = this.getValidatedAccountInfo(testBridge.bridge().lockingChainDoor())
      .accountData()
      .balance();

    assertThat(finalBalance).isEqualTo(initialDoorBalance.plus(amount).plus(testBridge.signatureReward()));
  }

  @Test
  void testXChainAddAccountCreateAttestation() throws JsonProcessingException, JsonRpcClientErrorException {
    KeyPair source = this.createRandomAccountEd25519();
    KeyPair destination = this.createRandomAccountEd25519();
    KeyPair otherChainSource = this.createRandomAccountEd25519();

    // Create an attestation for witness 1 to sign
    AttestationCreateAccount attestation = AttestationCreateAccount.builder()
      .xChainBridge(testBridge.bridge())
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(300)))
      .attestationRewardAccount(testBridge.witnessKeyPair().publicKey().deriveAddress())
      .wasLockingChainSend(false)
      .destination(destination.publicKey().deriveAddress())
      .signatureReward(testBridge.signatureReward())
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ONE))
      .build();

    // Witness 1 signs the attestation
    Signature attestationSignature = signatureService.sign(testBridge.witnessKeyPair().privateKey(), attestation);

    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(source.publicKey().deriveAddress())
    );
    FeeResult feeResult = xrplClient.fee();
    // Add the attestation from Witness 1
    XChainAddAccountCreateAttestation transaction = XChainAddAccountCreateAttestation.builder()
      .account(source.publicKey().deriveAddress())
      .xChainBridge(testBridge.bridge())
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(300)))
      .wasLockingChainSend(false)
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ONE))
      .destination(destination.publicKey().deriveAddress())
      .signatureReward(testBridge.signatureReward())
      .publicKey(testBridge.witnessKeyPair().publicKey())
      .signature(attestationSignature)
      .attestationRewardAccount(testBridge.witnessKeyPair().publicKey().deriveAddress())
      .attestationSignerAccount(testBridge.witnessKeyPair().publicKey().deriveAddress())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(source.publicKey())
      .build();

    TransactionResult<XChainAddAccountCreateAttestation> validatedTransaction = this.signSubmitAndWait(
      transaction,
      source,
      XChainAddAccountCreateAttestation.class
    );

    assertThat(validatedTransaction.metadata()).isPresent();
    TransactionMetadata metadata = validatedTransaction.metadata().get();
    Optional<AffectedNode> maybeCreatedClaimId = metadata.affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID))
      .findFirst();

    assertThat(maybeCreatedClaimId).isNotEmpty();
    AffectedNode createdClaimId = maybeCreatedClaimId.get();
    assertThat(createdClaimId).isInstanceOf(CreatedNode.class);
    MetaXChainOwnedCreateAccountClaimIdObject newFields =
      ((CreatedNode<MetaXChainOwnedCreateAccountClaimIdObject>) createdClaimId)
        .newFields();
    assertThat(newFields.account()).isNotEmpty().get().isEqualTo(testBridge.bridge().lockingChainDoor());
    assertThat(newFields.xChainBridge()).isNotEmpty().get().isEqualTo(testBridge.bridge());
    assertThat(newFields.xChainAccountCreateCount()).isNotEmpty().get().isEqualTo(XChainCount.of(UnsignedLong.ONE));
    assertThat(newFields.xChainCreateAccountAttestations()).containsExactly(
      ImmutableMetaXChainCreateAccountAttestation.builder()
        .xChainCreateAccountProofSig(
          ImmutableMetaXChainCreateAccountProofSig.builder()
            .amount(transaction.amount())
            .signatureReward(transaction.signatureReward())
            .attestationRewardAccount(transaction.attestationRewardAccount())
            .attestationSignerAccount(transaction.attestationSignerAccount())
            .destination(transaction.destination())
            .publicKey(testBridge.witnessKeyPair().publicKey())
            .wasLockingChainSend(false)
            .build()
        )
        .build()
    );

    XChainOwnedCreateAccountClaimIdObject claimIdObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        createdClaimId.ledgerIndex(), XChainOwnedCreateAccountClaimIdObject.class, LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(claimIdObject.account()).isEqualTo(testBridge.bridge().lockingChainDoor());
    assertThat(claimIdObject.xChainAccountCreateCount()).isEqualTo(XChainCount.of(UnsignedLong.ONE));
    assertThat(claimIdObject.xChainBridge()).isEqualTo(testBridge.bridge());
    assertThat(claimIdObject.xChainCreateAccountAttestations()).containsExactly(
      XChainCreateAccountAttestation.of(
        XChainCreateAccountProofSig.builder()
          .amount(transaction.amount())
          .signatureReward(transaction.signatureReward().get())
          .attestationRewardAccount(transaction.attestationRewardAccount())
          .attestationSignerAccount(transaction.attestationSignerAccount())
          .destination(transaction.destination())
          .publicKey(testBridge.witnessKeyPair().publicKey())
          .wasLockingChainSend(false)
          .build()
      )
    );

    // Create an attestation for witness 2 to sign
    AttestationCreateAccount attestation2 = AttestationCreateAccount.builder()
      .xChainBridge(testBridge.bridge())
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(300)))
      .attestationRewardAccount(testBridge.witnessKeyPair2().publicKey().deriveAddress())
      .wasLockingChainSend(false)
      .destination(destination.publicKey().deriveAddress())
      .signatureReward(testBridge.signatureReward())
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ONE))
      .build();

    // Witness 2 signs the attestation
    Signature attestationSignature2 = signatureService.sign(testBridge.witnessKeyPair2().privateKey(), attestation2);

    AccountInfoResult sourceAccountInfo2 = this.scanForResult(
      () -> getValidatedAccountInfo(source.publicKey().deriveAddress())
    );
    // Add witness 2's attestation
    XChainAddAccountCreateAttestation transaction2 = XChainAddAccountCreateAttestation.builder()
      .account(source.publicKey().deriveAddress())
      .xChainBridge(testBridge.bridge())
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(300)))
      .wasLockingChainSend(false)
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ONE))
      .destination(destination.publicKey().deriveAddress())
      .signatureReward(testBridge.signatureReward())
      .publicKey(testBridge.witnessKeyPair2().publicKey())
      .signature(attestationSignature2)
      .attestationRewardAccount(testBridge.witnessKeyPair2().publicKey().deriveAddress())
      .attestationSignerAccount(testBridge.witnessKeyPair2().publicKey().deriveAddress())
      .sequence(sourceAccountInfo2.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(sourceAccountInfo2.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(source.publicKey())
      .build();

    TransactionResult<XChainAddAccountCreateAttestation> validatedTransaction2 = this.signSubmitAndWait(
      transaction2,
      source,
      XChainAddAccountCreateAttestation.class
    );

    // The XChainOwnedCreateAccountClaimID should have gotten deleted
    assertThat(validatedTransaction2.metadata()).isNotEmpty();
    TransactionMetadata metadata2 = validatedTransaction2.metadata().get();
    Optional<AffectedNode> maybeDeletedClaimId = metadata2.affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID))
      .findFirst();

    assertThat(maybeDeletedClaimId).isNotEmpty();
    AffectedNode deletedClaimId = maybeDeletedClaimId.get();
    assertThat(deletedClaimId).isInstanceOf(DeletedNode.class);

    // And the destination account should exist now.
    assertThatNoException().isThrownBy(() -> this.getValidatedAccountInfo(destination.publicKey().deriveAddress()));
  }

  @Test
  void testXChainAddClaimAttestationXrpToXrpBridge() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair otherChainSource = Seed.ed25519Seed().deriveKeyPair();
    KeyPair source = this.createRandomAccountEd25519();
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10));

    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(source.publicKey().deriveAddress())
    );
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();
    XChainOwnedClaimIdObject claimIdObject = createClaimId(
      sourceAccountInfo,
      fee,
      source,
      testBridge,
      otherChainSource
    );

    XChainAddClaimAttestation addAttestation = addClaimAttestation(
      otherChainSource,
      amount,
      source,
      fee,
      testBridge.bridge(),
      testBridge.witnessKeyPair(),
      false).transaction();

    XChainOwnedClaimIdObject claimIdObjectAfterAdd = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        claimIdObject.index(),
        XChainOwnedClaimIdObject.class,
        LedgerSpecifier.VALIDATED
      )).node();

    assertThat(claimIdObjectAfterAdd.xChainClaimAttestations()).containsExactly(
      XChainClaimAttestation.of(
        XChainClaimProofSig.builder()
          .amount(amount)
          .publicKey(addAttestation.publicKey())
          .wasLockingChainSend(false)
          .attestationSignerAccount(addAttestation.attestationSignerAccount())
          .attestationRewardAccount(addAttestation.attestationRewardAccount())
          .destination(source.publicKey().deriveAddress())
          .build()
      )
    );

    XrpCurrencyAmount initialBalance = this.getValidatedAccountInfo(source.publicKey().deriveAddress())
      .accountData().balance();

    addClaimAttestation(
      otherChainSource,
      amount,
      source,
      fee,
      testBridge.bridge(),
      testBridge.witnessKeyPair2(),
      false).transaction();

    XrpCurrencyAmount finalBalance = this.getValidatedAccountInfo(source.publicKey().deriveAddress())
      .accountData().balance();

    assertThat(finalBalance).isEqualTo(
      initialBalance.plus(amount).minus(testBridge.signatureReward())
    );
  }

  @Test
  void testAddClaimAttestationIouToIouBridge() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair lockingDoor = Seed.ed25519Seed().deriveKeyPair();
    KeyPair lockingChainIssuer = Seed.ed25519Seed().deriveKeyPair();

    KeyPair lockingChainSource = this.createRandomAccountEd25519();
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();
    enableRippling(lockingChainSource, fee);

    KeyPair destination = this.createRandomAccountEd25519();

    this.createTrustLine(
      destination,
      IssuedCurrencyAmount.builder()
        .issuer(lockingChainSource.publicKey().deriveAddress())
        .currency("USD")
        .value("1000000000")
        .build(),
      fee
    );

    TestBridge iouBridge = setupBridge(
      lockingChainSource,
      XChainBridge.builder()
        .lockingChainDoor(lockingDoor.publicKey().deriveAddress())
        .lockingChainIssue(
          Issue.builder()
            .issuer(lockingChainIssuer.publicKey().deriveAddress())
            .currency("USD")
            .build()
        )
        .issuingChainDoor(lockingChainSource.publicKey().deriveAddress())
        .issuingChainIssue(
          Issue.builder()
            .issuer(lockingChainSource.publicKey().deriveAddress())
            .currency("USD")
            .build()
        )
        .build(),
      Optional.empty()
    );

    KeyPair otherChainSource = this.createRandomAccountEd25519();
    IssuedCurrencyAmount amount = IssuedCurrencyAmount.builder()
      .issuer(lockingChainIssuer.publicKey().deriveAddress())
      .currency("USD")
      .value("10")
      .build();

    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(lockingChainSource.publicKey().deriveAddress()));

    XChainOwnedClaimIdObject claimIdObject = createClaimId(
      sourceAccountInfo, fee, lockingChainSource, iouBridge, otherChainSource
    );

    XChainAddClaimAttestation addAttestation = addClaimAttestation(
      otherChainSource,
      amount,
      destination,
      fee,
      iouBridge.bridge(),
      iouBridge.witnessKeyPair(),
      true
    ).transaction();

    XChainOwnedClaimIdObject claimIdObjectAfterAdd = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        claimIdObject.index(),
        XChainOwnedClaimIdObject.class,
        LedgerSpecifier.VALIDATED
      )).node();

    assertThat(claimIdObjectAfterAdd.xChainClaimAttestations()).containsExactly(
      XChainClaimAttestation.of(
        XChainClaimProofSig.builder()
          .amount(amount)
          .publicKey(addAttestation.publicKey())
          .wasLockingChainSend(true)
          .attestationSignerAccount(addAttestation.attestationSignerAccount())
          .attestationRewardAccount(addAttestation.attestationRewardAccount())
          .destination(destination.publicKey().deriveAddress())
          .build()
      )
    );

    BigDecimal initialBalance = new BigDecimal(this.getValidatedAccountLines(
      destination.publicKey().deriveAddress(),
      iouBridge.bridge().issuingChainIssue().issuer().get()
    ).lines().get(0).balance());

    addClaimAttestation(
      otherChainSource,
      amount,
      destination,
      fee,
      iouBridge.bridge(),
      iouBridge.witnessKeyPair2(),
      true
    ).transaction();

    BigDecimal finalBalance = new BigDecimal(this.getValidatedAccountLines(
      destination.publicKey().deriveAddress(),
      iouBridge.bridge().issuingChainIssue().issuer().get()
    ).lines().get(0).balance());

    assertThat(finalBalance).isEqualTo(
      initialBalance.add(new BigDecimal(amount.value()))
    );
  }

  @Test
  void testXChainClaim() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair destination = this.createRandomAccountEd25519();
    KeyPair otherChainSource = Seed.ed25519Seed().deriveKeyPair();
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10));

    AccountInfoResult destinationAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destination.publicKey().deriveAddress())
    );
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();

    XChainOwnedClaimIdObject claimIdObject = createClaimId(
      destinationAccountInfo,
      fee,
      destination,
      testBridge,
      otherChainSource
    );

    XChainAddClaimAttestation addAttestation = addClaimAttestationForClaim(
      otherChainSource,
      amount,
      fee,
      testBridge.bridge(),
      testBridge.witnessKeyPair(),
      false
    ).transaction();

    XChainOwnedClaimIdObject claimIdObjectAfterAdd = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        claimIdObject.index(),
        XChainOwnedClaimIdObject.class,
        LedgerSpecifier.VALIDATED
      )).node();

    assertThat(claimIdObjectAfterAdd.xChainClaimAttestations()).containsExactly(
      XChainClaimAttestation.of(
        XChainClaimProofSig.builder()
          .amount(amount)
          .publicKey(addAttestation.publicKey())
          .wasLockingChainSend(false)
          .attestationSignerAccount(addAttestation.attestationSignerAccount())
          .attestationRewardAccount(addAttestation.attestationRewardAccount())
          .build()
      )
    );

    XChainAddClaimAttestation addAttestation2 = addClaimAttestationForClaim(
      otherChainSource,
      amount,
      fee,
      testBridge.bridge(),
      testBridge.witnessKeyPair2(),
      false
    ).transaction();

    XChainOwnedClaimIdObject claimIdObjectAfterAdd2 = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        claimIdObject.index(),
        XChainOwnedClaimIdObject.class,
        LedgerSpecifier.VALIDATED
      )).node();

    assertThat(claimIdObjectAfterAdd2.xChainClaimAttestations()).containsExactlyInAnyOrder(
      XChainClaimAttestation.of(
        XChainClaimProofSig.builder()
          .amount(amount)
          .publicKey(addAttestation.publicKey())
          .wasLockingChainSend(false)
          .attestationSignerAccount(addAttestation.attestationSignerAccount())
          .attestationRewardAccount(addAttestation.attestationRewardAccount())
          .build()
      ),
      XChainClaimAttestation.of(
        XChainClaimProofSig.builder()
          .amount(amount)
          .publicKey(addAttestation2.publicKey())
          .wasLockingChainSend(false)
          .attestationSignerAccount(addAttestation2.attestationSignerAccount())
          .attestationRewardAccount(addAttestation2.attestationRewardAccount())
          .build()
      )
    );

    XrpCurrencyAmount initialBalance = this.getValidatedAccountInfo(destination.publicKey().deriveAddress())
      .accountData()
      .balance();
    XChainClaim chainClaim = XChainClaim.builder()
      .account(destinationAccountInfo.accountData().account())
      .sequence(destinationAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(fee)
      .lastLedgerSequence(
        destinationAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(10))
      )
      .signingPublicKey(destination.publicKey())
      .xChainBridge(testBridge.bridge())
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .amount(amount)
      .destination(destination.publicKey().deriveAddress())
      .build();

    this.signSubmitAndWait(chainClaim, destination, XChainClaim.class);

    XrpCurrencyAmount finalBalance = this.getValidatedAccountInfo(destination.publicKey().deriveAddress())
      .accountData()
      .balance();

    assertThat(finalBalance).isEqualTo(
      initialBalance.plus(amount).minus(testBridge.signatureReward()).minus(fee)
    );
  }

  @Test
  void testXChainCommit() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair source = this.createRandomAccountEd25519();
    KeyPair destination = this.createRandomAccountEd25519();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(source.publicKey().deriveAddress()));
    this.scanForResult(() -> this.getValidatedAccountInfo(destination.publicKey().deriveAddress()));

    XrpCurrencyAmount initialDoorBalance = this.getValidatedAccountInfo(testBridge.bridge().lockingChainDoor())
      .accountData()
      .balance();

    XrpCurrencyAmount initialSourceBalance = sourceAccountInfo.accountData().balance();
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(10000000);
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();
    XChainCommit chainCommit = XChainCommit.builder()
      .account(source.publicKey().deriveAddress())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(source.publicKey())
      .xChainBridge(testBridge.bridge())
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .amount(amount)
      .otherChainDestination(destination.publicKey().deriveAddress())
      .build();

    this.signSubmitAndWait(chainCommit, source, XChainCommit.class);

    XrpCurrencyAmount finalDoorBalance = this.getValidatedAccountInfo(testBridge.bridge().lockingChainDoor())
      .accountData()
      .balance();

    XrpCurrencyAmount finalSourceBalance = this.getValidatedAccountInfo(source.publicKey().deriveAddress())
      .accountData()
      .balance();

    assertThat(finalDoorBalance).isEqualTo(
      initialDoorBalance.plus(amount)
    );

    assertThat(finalSourceBalance).isEqualTo(
      initialSourceBalance.minus(amount).minus(chainCommit.fee())
    );
  }

  @Test
  void testXChainModifyBridge() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair source = this.createRandomAccountEd25519();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(source.publicKey().deriveAddress())
    );

    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();

    XChainCreateBridge createBridge = XChainCreateBridge.builder()
      .account(sourceAccountInfo.accountData().account())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(source.publicKey())
      .xChainBridge(
        XChainBridge.builder()
          .lockingChainDoor(source.publicKey().deriveAddress())
          .lockingChainIssue(Issue.XRP)
          .issuingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .issuingChainIssue(Issue.XRP)
          .build()
      )
      .signatureReward(XrpCurrencyAmount.ofDrops(200))
      .minAccountCreateAmount(XrpCurrencyAmount.ofDrops(10000000))
      .build();

    Hash256 bridgeId = this.signSubmitAndWait(createBridge, source, XChainCreateBridge.class)
      .metadata()
      .flatMap(transactionMetadata -> transactionMetadata.affectedNodes().stream()
        .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.BRIDGE))
        .findFirst()
      ).map(AffectedNode::ledgerIndex)
      .get();

    BridgeObject bridgeObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        bridgeId,
        BridgeObject.class,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(bridgeObject.signatureReward()).isEqualTo(XrpCurrencyAmount.ofDrops(200));
    assertThat(bridgeObject.minAccountCreateAmount()).isNotEmpty().get().isEqualTo(XrpCurrencyAmount.ofDrops(10000000));

    XChainModifyBridge modifySignerReward = XChainModifyBridge.builder()
      .account(sourceAccountInfo.accountData().account())
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(10)))
      .signingPublicKey(source.publicKey())
      .xChainBridge(bridgeObject.xChainBridge())
      .signatureReward(XrpCurrencyAmount.ofDrops(300))
      .build();

    this.signSubmitAndWait(
      modifySignerReward,
      source,
      XChainModifyBridge.class
    );

    BridgeObject bridgeAfterModify = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        bridgeId,
        BridgeObject.class,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(bridgeAfterModify.signatureReward()).isEqualTo(XrpCurrencyAmount.ofDrops(300));
    assertThat(bridgeAfterModify.minAccountCreateAmount()).isNotEmpty().get()
      .isEqualTo(XrpCurrencyAmount.ofDrops(10000000));

    XChainModifyBridge deleteMinCreateAmount = XChainModifyBridge.builder()
      .account(sourceAccountInfo.accountData().account())
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(14)))
      .signingPublicKey(source.publicKey())
      .xChainBridge(bridgeObject.xChainBridge())
      .flags(XChainModifyBridgeFlags.CLEAR_ACCOUNT_CREATE_AMOUNT)
      .build();

    this.signSubmitAndWait(
      deleteMinCreateAmount,
      source,
      XChainModifyBridge.class
    );

    bridgeAfterModify = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        bridgeId,
        BridgeObject.class,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(bridgeAfterModify.signatureReward()).isEqualTo(XrpCurrencyAmount.ofDrops(300));
    assertThat(bridgeAfterModify.minAccountCreateAmount()).isEmpty();
  }

  private XChainOwnedClaimIdObject createClaimId(
    AccountInfoResult sourceAccountInfo,
    XrpCurrencyAmount fee,
    KeyPair source,
    TestBridge bridge,
    KeyPair otherChainSource
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    XChainCreateClaimId chainCreateClaimId = XChainCreateClaimId.builder()
      .account(sourceAccountInfo.accountData().account())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .signingPublicKey(source.publicKey())
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .xChainBridge(bridge.bridge())
      .signatureReward(bridge.signatureReward())
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .build();

    TransactionResult<XChainCreateClaimId> validatedCreateClaimId = this.signSubmitAndWait(
      chainCreateClaimId, source, XChainCreateClaimId.class
    );

    assertThat(validatedCreateClaimId.metadata()).isNotEmpty();
    TransactionMetadata createClaimIdMetadata = validatedCreateClaimId.metadata().get();
    Optional<AffectedNode> maybeAffectedNode = createClaimIdMetadata.affectedNodes().stream()
      .filter(node -> node.ledgerEntryType().equals(MetaLedgerEntryType.XCHAIN_OWNED_CLAIM_ID))
      .findFirst();
    assertThat(maybeAffectedNode).isNotEmpty();

    XChainOwnedClaimIdObject claimIdObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(maybeAffectedNode.get().ledgerIndex(),
        XChainOwnedClaimIdObject.class, LedgerSpecifier.VALIDATED
      )).node();

    assertThat(claimIdObject.xChainBridge()).isEqualTo(bridge.bridge());
    assertThat(claimIdObject.xChainClaimId()).isEqualTo(XChainClaimId.of(UnsignedLong.ONE));
    assertThat(claimIdObject.otherChainSource()).isEqualTo(otherChainSource.publicKey().deriveAddress());
    assertThat(claimIdObject.signatureReward()).isEqualTo(bridge.signatureReward());
    assertThat(claimIdObject.xChainClaimAttestations()).isEmpty();

    return claimIdObject;
  }

  private void enableRippling(KeyPair source, XrpCurrencyAmount fee)
    throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(source.publicKey().deriveAddress())
    );
    AccountSet accountSet = AccountSet.builder()
      .account(sourceAccountInfo.accountData().account())
      .sequence(sourceAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(sourceAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(source.publicKey())
      .setFlag(AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    this.signSubmitAndWait(accountSet, source, AccountSet.class);
  }

  private TransactionResult<XChainAddClaimAttestation> addClaimAttestation(
    KeyPair otherChainSource,
    CurrencyAmount amount,
    KeyPair destination,
    XrpCurrencyAmount fee,
    XChainBridge bridge,
    KeyPair witnessKeyPair,
    boolean wasLockingChainSend
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AttestationClaim attestation = AttestationClaim.builder()
      .xChainBridge(bridge)
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(amount)
      .attestationRewardAccount(witnessKeyPair.publicKey().deriveAddress())
      .wasLockingChainSend(wasLockingChainSend)
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .destination(destination.publicKey().deriveAddress())
      .build();

    Signature attestationSignature = signatureService.sign(witnessKeyPair.privateKey(), attestation);

    AccountInfoResult witnessAccountInfo = this.getValidatedAccountInfo(
      witnessKeyPair.publicKey().deriveAddress()
    );
    XChainAddClaimAttestation addAttestation = XChainAddClaimAttestation.builder()
      .account(witnessAccountInfo.accountData().account())
      .sequence(witnessAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(witnessAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(witnessKeyPair.publicKey())
      .xChainBridge(bridge)
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(amount)
      .wasLockingChainSend(wasLockingChainSend)
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .destination(destination.publicKey().deriveAddress())
      .publicKey(witnessKeyPair.publicKey())
      .signature(attestationSignature)
      .attestationSignerAccount(witnessAccountInfo.accountData().account())
      .attestationRewardAccount(witnessAccountInfo.accountData().account())
      .build();

    return this.signSubmitAndWait(
      addAttestation, witnessKeyPair, XChainAddClaimAttestation.class
    );
  }

  private TransactionResult<XChainAddClaimAttestation> addClaimAttestationForClaim(
    KeyPair otherChainSource,
    CurrencyAmount amount,
    XrpCurrencyAmount fee,
    XChainBridge bridge,
    KeyPair witnessKeyPair,
    boolean wasLockingChainSend
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AttestationClaim attestation = AttestationClaim.builder()
      .xChainBridge(bridge)
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(amount)
      .attestationRewardAccount(witnessKeyPair.publicKey().deriveAddress())
      .wasLockingChainSend(wasLockingChainSend)
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .build();

    Signature attestationSignature = signatureService.sign(witnessKeyPair.privateKey(), attestation);

    AccountInfoResult witnessAccountInfo = this.getValidatedAccountInfo(
      witnessKeyPair.publicKey().deriveAddress()
    );
    XChainAddClaimAttestation addAttestation = XChainAddClaimAttestation.builder()
      .account(witnessAccountInfo.accountData().account())
      .sequence(witnessAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(witnessAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .signingPublicKey(witnessKeyPair.publicKey())
      .xChainBridge(bridge)
      .otherChainSource(otherChainSource.publicKey().deriveAddress())
      .amount(amount)
      .wasLockingChainSend(wasLockingChainSend)
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .publicKey(witnessKeyPair.publicKey())
      .signature(attestationSignature)
      .attestationSignerAccount(witnessAccountInfo.accountData().account())
      .attestationRewardAccount(witnessAccountInfo.accountData().account())
      .build();

    TransactionResult<XChainAddClaimAttestation> result = this.signSubmitAndWait(
      addAttestation, witnessKeyPair, XChainAddClaimAttestation.class
    );
    logger.info(ObjectMapperFactory.create().writerWithDefaultPrettyPrinter().writeValueAsString(result));
    return result;
  }

  private TestBridge setupBridge(
    KeyPair doorKeyPair,
    XChainBridge bridge,
    Optional<XrpCurrencyAmount> minAccountCreateAmount
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    XrpCurrencyAmount signatureReward = XrpCurrencyAmount.ofDrops(200);
    Address doorAddress = doorKeyPair.publicKey().deriveAddress();

    AccountInfoResult doorAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(doorAddress));
    XChainCreateBridge createBridge = XChainCreateBridge.builder()
      .account(doorAddress)
      .xChainBridge(bridge)
      .signatureReward(signatureReward)
      .minAccountCreateAmount(minAccountCreateAmount)
      .sequence(doorAccountInfo.accountData().sequence())
      .fee(XrpCurrencyAmount.ofDrops(100))
      .signingPublicKey(doorKeyPair.publicKey())
      .lastLedgerSequence(doorAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .build();

    signSubmitAndWait(createBridge, doorKeyPair, XChainCreateBridge.class);

    AccountObjectsResult doorObjects = this.getValidatedAccountObjects(doorAddress);
    List<LedgerObject> bridgeObjects = doorObjects.accountObjects().stream()
      .filter(object -> BridgeObject.class.isAssignableFrom(object.getClass()))
      .collect(Collectors.toList());
    assertThat(bridgeObjects).hasSize(1);
    assertThat(bridgeObjects.get(0)).isInstanceOf(BridgeObject.class);
    BridgeObject bridgeObject = (BridgeObject) bridgeObjects.get(0);
    assertThat(bridgeObject.account()).isEqualTo(doorAddress);
    assertThat(bridgeObject.minAccountCreateAmount()).isEqualTo(createBridge.minAccountCreateAmount());
    assertThat(bridgeObject.signatureReward()).isEqualTo(signatureReward);
    assertThat(bridgeObject.xChainAccountClaimCount()).isEqualTo(XChainCount.of(UnsignedLong.ZERO));
    assertThat(bridgeObject.xChainAccountCreateCount()).isEqualTo(XChainCount.of(UnsignedLong.ZERO));
    assertThat(bridgeObject.xChainBridge()).isEqualTo(bridge);
    assertThat(bridgeObject.xChainClaimId()).isEqualTo(XChainClaimId.of(UnsignedLong.ZERO));

    LedgerEntryResult<BridgeObject> bridgeLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(
        bridgeObject.index(), BridgeObject.class, LedgerSpecifier.of(doorObjects.ledgerIndexSafe())
      )
    );

    assertThat(bridgeLedgerEntry.node()).isEqualTo(bridgeObject);

    LedgerEntryResult<BridgeObject> bridgeLedgerEntryTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.bridge(
        bridgeObject.account(), bridgeObject.xChainBridge(), LedgerSpecifier.of(doorObjects.ledgerIndexSafe())
      )
    );
    assertThat(bridgeLedgerEntryTyped).isEqualTo(bridgeLedgerEntry);

    KeyPair witnessKeyPair = this.createRandomAccountEd25519();
    Address witnessAddress = witnessKeyPair.publicKey().deriveAddress();
    KeyPair witnessKeyPair2 = this.createRandomAccountEd25519();
    Address witnessAddress2 = witnessKeyPair2.publicKey().deriveAddress();
    AccountInfoResult witnessAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(witnessAddress));
    SignerListSet signerListSet = SignerListSet.builder()
      .account(doorAddress)
      .sequence(doorAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .signingPublicKey(doorKeyPair.publicKey())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(witnessAddress)
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(witnessAddress2)
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .lastLedgerSequence(witnessAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .build();

    this.signSubmitAndWait(signerListSet, doorKeyPair, SignerListSet.class);
    return TestBridge.builder()
      .bridgeId(bridgeObject.index())
      .bridge(bridge)
      .witnessKeyPair(witnessKeyPair)
      .witnessKeyPair2(witnessKeyPair2)
      .signatureReward(signatureReward)
      .build();
  }

  private TestBridge setupXrpToXrpBridge() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair doorKeyPair = this.createRandomAccountEd25519();
    return setupBridge(
      doorKeyPair,
      XChainBridge.builder()
        .lockingChainDoor(doorKeyPair.publicKey().deriveAddress())
        .lockingChainIssue(Issue.XRP)
        .issuingChainDoor(AddressConstants.GENESIS_ACCOUNT)
        .issuingChainIssue(Issue.XRP)
        .build(),
      Optional.of(XrpCurrencyAmount.ofDrops(10000000))
    );
  }


  /**
   * A cross chain bridge that can be used in integration tests.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableTestBridge.class)
  @JsonDeserialize(as = ImmutableTestBridge.class)
  interface TestBridge {

    /**
     * Construct a {@code TestBridge} builder.
     *
     * @return An {@link ImmutableTestBridge.Builder}.
     */
    static ImmutableTestBridge.Builder builder() {
      return ImmutableTestBridge.builder();
    }

    /**
     * The ID of the {@link BridgeObject}.
     *
     * @return A {@link Hash256}.
     */
    Hash256 bridgeId();

    /**
     * The {@link XChainBridge} spec.
     *
     * @return An {@link XChainBridge}.
     */
    XChainBridge bridge();

    /**
     * The {@link KeyPair} of the first witness.
     *
     * @return A {@link KeyPair}.
     */
    KeyPair witnessKeyPair();

    /**
     * The {@link KeyPair} of the second witness.
     *
     * @return A {@link KeyPair}.
     */
    KeyPair witnessKeyPair2();

    /**
     * The signature reward of the bridge.
     *
     * @return An {@link XrpCurrencyAmount}.
     */
    XrpCurrencyAmount signatureReward();

  }
}
