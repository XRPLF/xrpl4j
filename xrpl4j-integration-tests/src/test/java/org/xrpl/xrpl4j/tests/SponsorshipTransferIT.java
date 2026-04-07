package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.SponsorSignature;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.SponsorshipTransfer;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Integration tests for SponsorshipTransfer with co-signing scenarios.
 *
 * <p>This test class demonstrates the four co-signing scenarios for sponsorship transfer:</p>
 * <ul>
 *   <li>Single-signature sponsee + single-signature sponsor</li>
 *   <li>Single-signature sponsee + multi-signature sponsor</li>
 *   <li>Multi-signature sponsee + single-signature sponsor</li>
 *   <li>Multi-signature sponsee + multi-signature sponsor</li>
 * </ul>
 *
 * <p>This test is disabled by default because the SponsorshipTransfer transaction requires
 * the featureSponsorship amendment to be enabled, which may not be available on all test networks.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Disabled("SponsorshipTransfer requires featureSponsorship amendment which may not be enabled on test networks")
public class SponsorshipTransferIT extends AbstractIT {

  /**
   * Test SponsorshipTransfer with single-signature sponsee and single-signature new sponsor.
   * Scenario: tfSponsorshipCreate - Creating a new sponsorship where both parties single-sign.
   */
  @Test
  public void testSponsorshipTransferSingleSigneeSingleSponsor() throws JsonRpcClientErrorException,
    JsonProcessingException {
    // Create sponsee (current owner) and new sponsor accounts
    KeyPair sponseeKeyPair = createRandomAccountEd25519();
    KeyPair newSponsorKeyPair = createRandomAccountEd25519();
    Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
    Address newSponsorAddress = newSponsorKeyPair.publicKey().deriveAddress();

    // Get current fee and account info
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sponseeAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponseeAddress)
    );

    // Create a SponsorshipTransfer transaction with tfSponsorshipCreate flag
    // Sponsee creates the transaction, new sponsor co-signs via SponsorSignature
    SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
      .account(sponseeAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sponseeAccountInfo.accountData().sequence())
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .sponsor(newSponsorAddress)
      .signingPublicKey(sponseeKeyPair.publicKey())
      .build();

    // Sponsee signs the transaction first
    SingleSignedTransaction<SponsorshipTransfer> sponseeSigned = signatureService.sign(
      sponseeKeyPair.privateKey(),
      unsignedTransfer
    );

    // New sponsor co-signs (single signature) - using sponsorSign
    Signature sponsorSig = signatureService.sponsorSign(
      newSponsorKeyPair.privateKey(),
      unsignedTransfer
    );
    SponsorSignature sponsorSignature = SponsorSignature.builder()
      .signingPublicKey(newSponsorKeyPair.publicKey())
      .transactionSignature(sponsorSig)
      .build();

    // Build the final transaction with both signatures (following LoanSet pattern)
    SponsorshipTransfer signedTransfer = SponsorshipTransfer.builder()
      .from(unsignedTransfer)
      .sponsorSignature(sponsorSignature)
      .transactionSignature(sponseeSigned.signature())
      .build();

    // Build unsigned transaction with sponsor signature (for SingleSignedTransaction wrapper)
    SponsorshipTransfer unsignedWithSponsorSig = SponsorshipTransfer.builder()
      .from(unsignedTransfer)
      .sponsorSignature(sponsorSignature)
      .build();

    // Wrap in SingleSignedTransaction for submission
    SingleSignedTransaction<SponsorshipTransfer> finalTx = SingleSignedTransaction
      .<SponsorshipTransfer>builder()
      .unsignedTransaction(unsignedWithSponsorSig)
      .signature(sponseeSigned.signature())
      .signedTransaction(signedTransfer)
      .build();

    // Submit the transaction
    SubmitResult<SponsorshipTransfer> submitResult = xrplClient.submit(finalTx);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logInfo(signedTransfer.transactionType(), submitResult.transactionResult().hash());

    // Wait for validation
    TransactionResult<SponsorshipTransfer> validatedTx = this.scanForResult(
      () -> this.getValidatedTransaction(finalTx.hash(), SponsorshipTransfer.class)
    );
    assertThat(validatedTx.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  /**
   * Test SponsorshipTransfer with single-signature sponsee and multi-signature new sponsor.
   * Scenario: tfSponsorshipCreate - Creating a new sponsorship where sponsor uses multi-sig.
   */
  @Test
  public void testSponsorshipTransferSingleSponseeMultiSponsor() throws JsonRpcClientErrorException,
    JsonProcessingException {
    // Create sponsee and two sponsor signers
    KeyPair sponseeKeyPair = createRandomAccountEd25519();
    KeyPair sponsor1KeyPair = createRandomAccountEd25519();
    KeyPair sponsor2KeyPair = createRandomAccountEd25519();
    Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
    Address sponsor1Address = sponsor1KeyPair.publicKey().deriveAddress();

    // Get current fee and account info
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sponseeAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponseeAddress)
    );

    // Create unsigned SponsorshipTransfer - sponsor field uses sponsor1 address but both sponsors will sign
    SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
      .account(sponseeAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sponseeAccountInfo.accountData().sequence())
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .sponsor(sponsor1Address)
      .signingPublicKey(sponseeKeyPair.publicKey())
      .build();

    // Sponsee signs the transaction first
    SingleSignedTransaction<SponsorshipTransfer> sponseeSigned = signatureService.sign(
      sponseeKeyPair.privateKey(),
      unsignedTransfer
    );

    // Both sponsors multi-sign using sponsorMultiSign (preserves SigningPubKey)
    List<Signer> sponsorSigners = Lists.newArrayList(sponsor1KeyPair, sponsor2KeyPair).stream()
      .map(keyPair -> {
        PublicKey signingPublicKey = signatureService.derivePublicKey(keyPair.privateKey());
        Signature signature = signatureService.sponsorMultiSign(
          keyPair.privateKey(),
          unsignedTransfer
        );
        return Signer.builder()
          .signingPublicKey(signingPublicKey)
          .transactionSignature(signature)
          .build();
      })
      .collect(Collectors.toList());

    // Build SponsorSignature with multi-sig
    SponsorSignature sponsorSignature = SponsorSignature.builder()
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .signers(
        sponsorSigners.stream()
          .map(signer -> SignerWrapper.of(signer))
          .collect(Collectors.toList())
      )
      .build();

    // Build the final signed transaction with both signatures
    SponsorshipTransfer signedTransfer = SponsorshipTransfer.builder()
      .from(unsignedTransfer)
      .sponsorSignature(sponsorSignature)
      .transactionSignature(sponseeSigned.signature())
      .build();

    // Build unsigned transaction with sponsor signature (for SingleSignedTransaction wrapper)
    SponsorshipTransfer unsignedWithSponsorSig = SponsorshipTransfer.builder()
      .from(unsignedTransfer)
      .sponsorSignature(sponsorSignature)
      .build();

    // Wrap in SingleSignedTransaction for submission
    SingleSignedTransaction<SponsorshipTransfer> finalTx = SingleSignedTransaction
      .<SponsorshipTransfer>builder()
      .unsignedTransaction(unsignedWithSponsorSig)
      .signature(sponseeSigned.signature())
      .signedTransaction(signedTransfer)
      .build();

    // Submit the transaction
    SubmitResult<SponsorshipTransfer> submitResult = xrplClient.submit(finalTx);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logInfo(signedTransfer.transactionType(), submitResult.transactionResult().hash());

    // Wait for validation
    TransactionResult<SponsorshipTransfer> validatedTx = this.scanForResult(
      () -> this.getValidatedTransaction(finalTx.hash(), SponsorshipTransfer.class)
    );
    assertThat(validatedTx.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  /**
   * Test SponsorshipTransfer with multi-signature sponsee and single-signature new sponsor.
   * Scenario: tfSponsorshipCreate - Creating a new sponsorship where sponsee uses multi-sig.
   */
  @Test
  public void testSponsorshipTransferMultiSponseeSingleSponsor() throws JsonRpcClientErrorException,
    JsonProcessingException {
    // Create main sponsee account and signers for multi-sig
    KeyPair sponseeKeyPair = createRandomAccountEd25519();
    KeyPair aliceKeyPair = createRandomAccountEd25519();
    KeyPair bobKeyPair = createRandomAccountEd25519();
    KeyPair newSponsorKeyPair = createRandomAccountEd25519();
    Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
    Address newSponsorAddress = newSponsorKeyPair.publicKey().deriveAddress();

    // Set up multi-sig for sponsee account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sponseeAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponseeAddress)
    );

    // Create SignerListSet to set up multi-sig on sponsee account
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sponseeAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sponseeAccountInfo.accountData().sequence())
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
      .signingPublicKey(sponseeKeyPair.publicKey())
      .build();

    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sponseeKeyPair.privateKey(),
      signerListSet
    );
    SubmitResult<SignerListSet> signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertThat(signerListSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signerListSetResult.transactionResult().hash(), SignerListSet.class)
    );

    // Get updated sequence after SignerListSet
    AccountInfoResult updatedSponseeInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponseeAddress)
    );

    // Create unsigned SponsorshipTransfer with empty SigningPubKey (for multi-sig)
    SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
      .account(sponseeAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(updatedSponseeInfo.accountData().sequence())
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .sponsor(newSponsorAddress)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .build();

    // Alice and Bob multi-sign the transaction as sponsee
    List<Signer> sponseeSigners = Lists.newArrayList(aliceKeyPair, bobKeyPair).stream()
      .map(keyPair -> {
        PublicKey signingPublicKey = signatureService.derivePublicKey(keyPair.privateKey());
        Signature signature = signatureService.multiSign(keyPair.privateKey(), unsignedTransfer);
        return Signer.builder()
          .signingPublicKey(signingPublicKey)
          .transactionSignature(signature)
          .build();
      })
      .collect(Collectors.toList());

    // New sponsor single-signs using sponsorSign
    Signature sponsorSig = signatureService.sponsorSign(
      newSponsorKeyPair.privateKey(),
      unsignedTransfer
    );
    SponsorSignature sponsorSignature = SponsorSignature.builder()
      .signingPublicKey(newSponsorKeyPair.publicKey())
      .transactionSignature(sponsorSig)
      .build();

    // Build unsigned transaction with sponsor signature (for MultiSignedTransaction wrapper)
    SponsorshipTransfer unsignedWithSponsorSig = SponsorshipTransfer.builder()
      .from(unsignedTransfer)
      .sponsorSignature(sponsorSignature)
      .build();

    // Wrap in MultiSignedTransaction for submission
    MultiSignedTransaction<SponsorshipTransfer> finalTx = MultiSignedTransaction
      .<SponsorshipTransfer>builder()
      .unsignedTransaction(unsignedWithSponsorSig)
      .signerSet(sponseeSigners.stream().collect(Collectors.toSet()))
      .build();

    // Submit the multi-signed transaction
    SubmitMultiSignedResult<SponsorshipTransfer> submitResult = xrplClient.submitMultisigned(finalTx);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    TransactionResult<SponsorshipTransfer> validatedTx = this.scanForResult(
      () -> this.getValidatedTransaction(finalTx.hash(), SponsorshipTransfer.class)
    );
    assertThat(validatedTx.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  /**
   * Test SponsorshipTransfer with multi-signature sponsee and multi-signature new sponsor.
   * Scenario: tfSponsorshipCreate - Creating a new sponsorship where both parties use multi-sig.
   */
  @Test
  public void testSponsorshipTransferMultiSponseeMultiSponsor() throws JsonRpcClientErrorException,
    JsonProcessingException {
    // Create main sponsee account and signers
    KeyPair sponseeKeyPair = createRandomAccountEd25519();
    KeyPair aliceKeyPair = createRandomAccountEd25519();
    KeyPair bobKeyPair = createRandomAccountEd25519();
    // Sponsor signers
    KeyPair sponsor1KeyPair = createRandomAccountEd25519();
    KeyPair sponsor2KeyPair = createRandomAccountEd25519();
    Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
    Address sponsor1Address = sponsor1KeyPair.publicKey().deriveAddress();

    // Set up multi-sig for sponsee account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sponseeAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponseeAddress)
    );

    SignerListSet signerListSet = SignerListSet.builder()
      .account(sponseeAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sponseeAccountInfo.accountData().sequence())
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
      .signingPublicKey(sponseeKeyPair.publicKey())
      .build();

    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sponseeKeyPair.privateKey(),
      signerListSet
    );
    SubmitResult<SignerListSet> signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertThat(signerListSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(
      () -> this.getValidatedTransaction(signerListSetResult.transactionResult().hash(), SignerListSet.class)
    );

    // Get updated sequence
    AccountInfoResult updatedSponseeInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponseeAddress)
    );

    // Create unsigned SponsorshipTransfer
    SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
      .account(sponseeAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(updatedSponseeInfo.accountData().sequence())
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .sponsor(sponsor1Address)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .build();

    // Alice and Bob multi-sign as sponsee
    List<Signer> sponseeSigners = Lists.newArrayList(aliceKeyPair, bobKeyPair).stream()
      .map(keyPair -> {
        PublicKey signingPublicKey = signatureService.derivePublicKey(keyPair.privateKey());
        Signature signature = signatureService.multiSign(keyPair.privateKey(), unsignedTransfer);
        return Signer.builder()
          .signingPublicKey(signingPublicKey)
          .transactionSignature(signature)
          .build();
      })
      .collect(Collectors.toList());

    // Both sponsors multi-sign (using sponsorMultiSign to preserve SigningPubKey=empty for multi-sig)
    List<Signer> sponsorSigners = Lists.newArrayList(sponsor1KeyPair, sponsor2KeyPair).stream()
      .map(keyPair -> {
        PublicKey signingPublicKey = signatureService.derivePublicKey(keyPair.privateKey());
        Signature signature = signatureService.sponsorMultiSign(keyPair.privateKey(), unsignedTransfer);
        return Signer.builder()
          .signingPublicKey(signingPublicKey)
          .transactionSignature(signature)
          .build();
      })
      .collect(Collectors.toList());

    // Build SponsorSignature with multi-sig
    SponsorSignature sponsorSignature = SponsorSignature.builder()
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .signers(
        sponsorSigners.stream()
          .map(signer -> SignerWrapper.of(signer))
          .collect(Collectors.toList())
      )
      .build();

    // Build unsigned transaction with sponsor signature (for MultiSignedTransaction wrapper)
    SponsorshipTransfer unsignedWithSponsorSig = SponsorshipTransfer.builder()
      .from(unsignedTransfer)
      .sponsorSignature(sponsorSignature)
      .build();

    // Wrap in MultiSignedTransaction for submission
    MultiSignedTransaction<SponsorshipTransfer> finalTx = MultiSignedTransaction
      .<SponsorshipTransfer>builder()
      .unsignedTransaction(unsignedWithSponsorSig)
      .signerSet(sponseeSigners.stream().collect(Collectors.toSet()))
      .build();

    // Submit the multi-signed transaction
    SubmitMultiSignedResult<SponsorshipTransfer> submitResult = xrplClient.submitMultisigned(finalTx);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    TransactionResult<SponsorshipTransfer> validatedTx = this.scanForResult(
      () -> this.getValidatedTransaction(finalTx.hash(), SponsorshipTransfer.class)
    );
    assertThat(validatedTx.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }
}

