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
import org.junit.jupiter.api.Nested;
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
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;
import org.xrpl.xrpl4j.model.flags.SponsorshipSetFlags;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;
import org.xrpl.xrpl4j.model.ledger.CheckObject;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.ledger.SponsorshipObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.SponsorSignature;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.SponsorshipTransfer;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Integration tests for the Sponsorship feature (XLS-0068).
 *
 * <p>This test class covers:</p>
 * <ul>
 *   <li>Fee sponsorship - sponsor pays transaction fees for sponsee</li>
 *   <li>Reserve sponsorship - sponsor covers reserve requirements for sponsee's ledger objects</li>
 *   <li>SponsorshipSet transaction - creating and managing sponsorship relationships</li>
 *   <li>SponsorshipTransfer transaction - transferring sponsorship ownership</li>
 *   <li>account_sponsoring RPC method - querying sponsorship information</li>
 * </ul>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 * @see "https://github.com/XRPLF/rippled/pull/5887"
 */
// Requires a rippled build that registers the featureSponsorship amendment (PR #5887) and has it
// enabled in xrpld.cfg. The xrpld:sponsor-local image referenced by RippledContainer satisfies
// this requirement.
public class SponsorshipIT extends AbstractIT {

  // ==================== Fee Sponsorship Tests ====================

  /**
   * Tests for fee sponsorship where the sponsor pays transaction fees for the sponsee.
   */
  @Nested
  class FeeSponsorshipTests {

    /**
     * Test: Alice sponsors Bob to pay for transaction fees.
     * 1. Alice creates a SponsorshipSet with FeeAmount for Bob
     * 2. Bob submits a Payment transaction with Alice as sponsor
     * 3. Assert that fees were deducted from Alice's FeeAmount, not Bob's balance
     *
     * <p>Reassignment of sponsorship for a sponsored object is not exercised here — a sponsored
     * Payment leaves no reassignable ledger object behind, and rippled does not treat the
     * {@link SponsorshipObject} relationship itself as a reassign target (see
     * {@code SponsorshipTransfer::getLedgerEntryOwner}). Reassignment is covered by
     * {@link SponsorshipTransferTests#testSponsorshipTransferReassign()}.
     */
    @Test
    void testFeeSponsorshipLifecycle() throws JsonRpcClientErrorException, JsonProcessingException {
      // Create Alice (sponsor) and Bob (sponsee) accounts
      KeyPair aliceKeyPair = createRandomAccountEd25519();
      KeyPair bobKeyPair = createRandomAccountEd25519();
      Address aliceAddress = aliceKeyPair.publicKey().deriveAddress();
      Address bobAddress = bobKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();

      // Step 1: Alice creates a SponsorshipSet with FeeAmount for Bob
      AccountInfoResult aliceAccountInfo = scanForResult(() -> getValidatedAccountInfo(aliceAddress));
      XrpCurrencyAmount feeAmount = XrpCurrencyAmount.ofDrops(1000000); // 1 XRP for fees

      SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
        .account(aliceAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(aliceAccountInfo.accountData().sequence())
        .sponsee(bobAddress)
        .feeAmount(feeAmount)
        .maxFee(XrpCurrencyAmount.ofDrops(1000)) // Max 1000 drops per tx
        .signingPublicKey(aliceKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedSponsorshipSet = signatureService.sign(
        aliceKeyPair.privateKey(), sponsorshipSet
      );
      SubmitResult<SponsorshipSet> sponsorshipSetResult = xrplClient.submit(signedSponsorshipSet);
      assertThat(sponsorshipSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      logInfo(sponsorshipSet.transactionType(), sponsorshipSetResult.transactionResult().hash());

      scanForResult(() -> getValidatedTransaction(
        sponsorshipSetResult.transactionResult().hash(), SponsorshipSet.class
      ));

      // Verify Sponsorship ledger object was created
      SponsorshipObject sponsorship = getSponsorshipObject(aliceAddress, bobAddress);
      assertThat(sponsorship.owner()).isEqualTo(aliceAddress);
      assertThat(sponsorship.sponsee()).isEqualTo(bobAddress);
      assertThat(sponsorship.feeAmount()).hasValue(feeAmount);

      // Step 2: Bob submits a Payment transaction with Alice as sponsor (fee sponsor)
      // Record initial balance before payment
      final XrpCurrencyAmount bobInitialBalance = scanForResult(
        () -> getValidatedAccountInfo(bobAddress)
      ).accountData().balance();

      KeyPair charlieKeyPair = createRandomAccountEd25519();
      Address charlieAddress = charlieKeyPair.publicKey().deriveAddress();
      AccountInfoResult bobAccountInfo = scanForResult(() -> getValidatedAccountInfo(bobAddress));
      XrpCurrencyAmount paymentAmount = XrpCurrencyAmount.ofDrops(100000); // 0.1 XRP

      Payment unsignedPayment = Payment.builder()
        .account(bobAddress)
        .destination(charlieAddress)
        .amount(paymentAmount)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(bobAccountInfo.accountData().sequence())
        .sponsor(aliceAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
        .signingPublicKey(bobKeyPair.publicKey())
        .build();

      // Bob signs first, then Alice co-signs as sponsor
      SingleSignedTransaction<Payment> bobSignedPayment = signatureService.sign(
        bobKeyPair.privateKey(), unsignedPayment
      );

      Signature aliceSponsorSig = signatureService.sponsorSign(
        aliceKeyPair.privateKey(), unsignedPayment
      );
      SponsorSignature sponsorSignature = SponsorSignature.builder()
        .signingPublicKey(aliceKeyPair.publicKey())
        .transactionSignature(aliceSponsorSig)
        .build();

      Payment signedPayment = Payment.builder()
        .from(unsignedPayment)
        .sponsorSignature(sponsorSignature)
        .transactionSignature(bobSignedPayment.signature())
        .build();

      Payment unsignedWithSponsorSig = Payment.builder()
        .from(unsignedPayment)
        .sponsorSignature(sponsorSignature)
        .build();

      SingleSignedTransaction<Payment> finalPaymentTx = SingleSignedTransaction
        .<Payment>builder()
        .unsignedTransaction(unsignedWithSponsorSig)
        .signature(bobSignedPayment.signature())
        .signedTransaction(signedPayment)
        .build();

      SubmitResult<Payment> paymentResult = xrplClient.submit(finalPaymentTx);
      assertThat(paymentResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      logInfo(signedPayment.transactionType(), paymentResult.transactionResult().hash());

      scanForResult(() -> getValidatedTransaction(finalPaymentTx.hash(), Payment.class));

      // Step 3: Assert that fees were paid by Alice (from FeeAmount), not Bob
      AccountInfoResult bobFinalInfo = scanForResult(() -> getValidatedAccountInfo(bobAddress));
      XrpCurrencyAmount bobFinalBalance = bobFinalInfo.accountData().balance();

      // Bob's balance should only decrease by the payment amount, not the fee
      XrpCurrencyAmount expectedBobBalance = XrpCurrencyAmount.ofDrops(
        bobInitialBalance.value().longValue() - paymentAmount.value().longValue()
      );
      assertThat(bobFinalBalance).isEqualTo(expectedBobBalance);

      // Alice's FeeAmount should have decreased
      SponsorshipObject updatedSponsorship = getSponsorshipObject(aliceAddress, bobAddress);
      XrpCurrencyAmount expectedFeeAmount = XrpCurrencyAmount.ofDrops(
        feeAmount.value().longValue() - feeResult.drops().openLedgerFee().value().longValue()
      );
      assertThat(updatedSponsorship.feeAmount()).hasValue(expectedFeeAmount);
    }

    /**
     * Test: Sponsor pays fees using pre-funded sponsorship (no co-signing needed).
     */
    @Test
    void testPreFundedFeeSponsorshipNoCosign() throws JsonRpcClientErrorException, JsonProcessingException {
      KeyPair sponsorKeyPair = createRandomAccountEd25519();
      KeyPair sponseeKeyPair = createRandomAccountEd25519();
      KeyPair destKeyPair = createRandomAccountEd25519();
      Address sponsorAddress = sponsorKeyPair.publicKey().deriveAddress();
      Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
      Address destAddress = destKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();

      // Sponsor creates SponsorshipSet without RequireSignForFee flag
      AccountInfoResult sponsorAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponsorAddress));

      SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
        .account(sponsorAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponsorAccountInfo.accountData().sequence())
        .sponsee(sponseeAddress)
        .feeAmount(XrpCurrencyAmount.ofDrops(500000))
        .signingPublicKey(sponsorKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedSponsorshipSet = signatureService.sign(
        sponsorKeyPair.privateKey(), sponsorshipSet
      );
      SubmitResult<SponsorshipSet> result = xrplClient.submit(signedSponsorshipSet);
      assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(result.transactionResult().hash(), SponsorshipSet.class));

      // Sponsee submits payment using pre-funded fee (no sponsor signature needed)
      AccountInfoResult sponseeAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponseeAddress));

      Payment payment = Payment.builder()
        .account(sponseeAddress)
        .destination(destAddress)
        .amount(XrpCurrencyAmount.ofDrops(50000))
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponseeAccountInfo.accountData().sequence())
        .sponsor(sponsorAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
        .signingPublicKey(sponseeKeyPair.publicKey())
        .build();

      SingleSignedTransaction<Payment> signedPayment = signatureService.sign(
        sponseeKeyPair.privateKey(), payment
      );

      SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);
      assertThat(paymentResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    }
  }

  // ==================== Reserve Sponsorship Tests ====================

  /**
   * Tests for reserve sponsorship where the sponsor covers reserve requirements.
   */
  @Nested
  class ReserveSponsorshipTests {

    /**
     * Test: Sponsor covers reserve for CheckCreate.
     * 1. Alice creates SponsorshipSet with ReserveCount for Bob
     * 2. Bob creates a Check with Alice as reserve sponsor
     * 3. Assert that the Check is owned by Bob but reserve is covered by Alice
     */
    @Test
    void testReserveSponsorshipForCheckCreate() throws JsonRpcClientErrorException, JsonProcessingException {
      KeyPair aliceKeyPair = createRandomAccountEd25519();
      KeyPair bobKeyPair = createRandomAccountEd25519();
      Address aliceAddress = aliceKeyPair.publicKey().deriveAddress();
      Address bobAddress = bobKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();

      // Step 1: Alice creates SponsorshipSet with ReserveCount for Bob
      AccountInfoResult aliceAccountInfo = scanForResult(() -> getValidatedAccountInfo(aliceAddress));

      SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
        .account(aliceAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(aliceAccountInfo.accountData().sequence())
        .sponsee(bobAddress)
        .reserveCount(UnsignedInteger.valueOf(5))
        .signingPublicKey(aliceKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedSponsorshipSet = signatureService.sign(
        aliceKeyPair.privateKey(), sponsorshipSet
      );
      SubmitResult<SponsorshipSet> sponsorshipResult = xrplClient.submit(signedSponsorshipSet);
      assertThat(sponsorshipResult.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(
        sponsorshipResult.transactionResult().hash(), SponsorshipSet.class
      ));

      // Record initial sponsor-aware owner counts. XLS-68 attributes a sponsored object's reserve via
      // the sponsor's sfSponsoringOwnerCount and the sponsee's sfSponsoredOwnerCount; the sponsee's
      // raw sfOwnerCount still increments when it owns the object (netReserve is ownerCount minus
      // sponsoredOwnerCount). See rippled's adjustOwnerCount() in AccountRootHelpers.cpp.
      final UnsignedInteger bobInitialSponsoredOwnerCount = scanForResult(
        () -> getValidatedAccountInfo(bobAddress)
      ).accountData().sponsoredOwnerCount().orElse(UnsignedInteger.ZERO);
      final UnsignedInteger aliceInitialSponsoringOwnerCount = scanForResult(
        () -> getValidatedAccountInfo(aliceAddress)
      ).accountData().sponsoringOwnerCount().orElse(UnsignedInteger.ZERO);

      // Step 2: Bob creates a Check with Alice as reserve sponsor
      AccountInfoResult bobAccountInfo = scanForResult(() -> getValidatedAccountInfo(bobAddress));

      CheckCreate unsignedCheck = CheckCreate.builder()
        .account(bobAddress)
        .destination(aliceAddress)
        .sendMax(XrpCurrencyAmount.ofDrops(1000000))
        .fee(feeResult.drops().openLedgerFee())
        .sequence(bobAccountInfo.accountData().sequence())
        .sponsor(aliceAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
        .signingPublicKey(bobKeyPair.publicKey())
        .build();

      // Bob signs, Alice co-signs as sponsor
      SingleSignedTransaction<CheckCreate> bobSignedCheck = signatureService.sign(
        bobKeyPair.privateKey(), unsignedCheck
      );

      Signature aliceSponsorSig = signatureService.sponsorSign(
        aliceKeyPair.privateKey(), unsignedCheck
      );
      SponsorSignature sponsorSignature = SponsorSignature.builder()
        .signingPublicKey(aliceKeyPair.publicKey())
        .transactionSignature(aliceSponsorSig)
        .build();

      CheckCreate signedCheck = CheckCreate.builder()
        .from(unsignedCheck)
        .sponsorSignature(sponsorSignature)
        .transactionSignature(bobSignedCheck.signature())
        .build();

      CheckCreate unsignedWithSponsorSig = CheckCreate.builder()
        .from(unsignedCheck)
        .sponsorSignature(sponsorSignature)
        .build();

      SingleSignedTransaction<CheckCreate> finalCheckTx = SingleSignedTransaction
        .<CheckCreate>builder()
        .unsignedTransaction(unsignedWithSponsorSig)
        .signature(bobSignedCheck.signature())
        .signedTransaction(signedCheck)
        .build();

      SubmitResult<CheckCreate> checkResult = xrplClient.submit(finalCheckTx);
      assertThat(checkResult.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(finalCheckTx.hash(), CheckCreate.class));

      // Step 3: Assert reserve sponsorship
      // Bob's SponsoredOwnerCount should increase by 1 (his Check's reserve is attributed to Alice)
      AccountInfoResult bobFinalInfo = scanForResult(() -> getValidatedAccountInfo(bobAddress));
      assertThat(bobFinalInfo.accountData().sponsoredOwnerCount().orElse(UnsignedInteger.ZERO).intValue())
        .isEqualTo(bobInitialSponsoredOwnerCount.intValue() + 1);

      // Alice's SponsoringOwnerCount should increase (she's covering the reserve)
      AccountInfoResult aliceFinalInfo = scanForResult(() -> getValidatedAccountInfo(aliceAddress));
      assertThat(aliceFinalInfo.accountData().sponsoringOwnerCount().orElse(UnsignedInteger.ZERO).intValue())
        .isGreaterThan(aliceInitialSponsoringOwnerCount.intValue());
      final UnsignedInteger aliceSponsoringOwnerCountWithSponsorship =
        aliceFinalInfo.accountData().sponsoringOwnerCount().orElse(UnsignedInteger.ZERO);

      // Step 4: Bob ends the reserve sponsorship for the Check object
      // Get the Check object ID created in step 2
      AccountObjectsResult bobAccountObjects = xrplClient.accountObjects(
        AccountObjectsRequestParams.builder()
          .account(bobAddress)
          .ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .type(AccountObjectType.CHECK)
          .build()
      );
      Hash256 checkObjectId = ((CheckObject) bobAccountObjects.accountObjects().get(0)).index();

      AccountInfoResult bobInfoForTransfer = scanForResult(() -> getValidatedAccountInfo(bobAddress));

      SponsorshipTransfer endTransfer = SponsorshipTransfer.builder()
        .account(bobAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(bobInfoForTransfer.accountData().sequence())
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
        .objectId(checkObjectId)
        .signingPublicKey(bobKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipTransfer> signedEndTransfer = signatureService.sign(
        bobKeyPair.privateKey(), endTransfer
      );
      SubmitResult<SponsorshipTransfer> endResult = xrplClient.submit(signedEndTransfer);
      assertThat(endResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      logInfo(endTransfer.transactionType(), endResult.transactionResult().hash());

      scanForResult(() -> getValidatedTransaction(endResult.transactionResult().hash(), SponsorshipTransfer.class));

      // Step 5: Assert Alice's SponsoringOwnerCount returns toward initial (reserve released)
      AccountInfoResult aliceAfterEnd = scanForResult(() -> getValidatedAccountInfo(aliceAddress));
      assertThat(aliceAfterEnd.accountData().sponsoringOwnerCount().orElse(UnsignedInteger.ZERO).intValue())
        .isLessThan(aliceSponsoringOwnerCountWithSponsorship.intValue());

      // Bob's SponsoredOwnerCount should return to initial (Check's reserve no longer attributed elsewhere)
      AccountInfoResult bobAfterEnd = scanForResult(() -> getValidatedAccountInfo(bobAddress));
      assertThat(bobAfterEnd.accountData().sponsoredOwnerCount().orElse(UnsignedInteger.ZERO).intValue())
        .isEqualTo(bobInitialSponsoredOwnerCount.intValue());
    }
  }

  // ==================== SponsorshipTransfer Tests ====================

  /**
   * Tests for SponsorshipTransfer with various co-signing scenarios.
   */
  @Nested
  class SponsorshipTransferTests {

    /**
     * Test: Transfer reserve sponsorship for a sponsored Check from Alice to Charlie.
     *
     * <p>rippled's {@code SponsorshipTransfer::getLedgerEntryOwner} only returns a non-empty
     * owner for object types that have an owning account (Check, Escrow, Offer, etc.). It does
     * NOT treat a {@code Sponsorship} ledger entry as a reassignable target, so pointing
     * {@code objectId} at a {@code SponsorshipObject} index yields {@code tecNO_PERMISSION}.
     * This test therefore targets a sponsored {@link CheckObject} whose reserve burden is being
     * reassigned from one sponsor to another.
     *
     * <ol>
     *   <li>Alice sponsors Bob's reserves via SponsorshipSet.reserveCount.</li>
     *   <li>Bob creates a Check, with Alice co-signing as the reserve sponsor.</li>
     *   <li>Bob submits SponsorshipTransfer (tfSponsorshipReassign) naming Charlie as the new
     *       sponsor; Charlie co-signs. The Check's sponsor moves from Alice to Charlie.</li>
     *   <li>Assert the Check's sponsor field now equals Charlie.</li>
     * </ol>
     */
    @Test
    void testSponsorshipTransferReassign() throws JsonRpcClientErrorException, JsonProcessingException {
      KeyPair aliceKeyPair = createRandomAccountEd25519();
      KeyPair bobKeyPair = createRandomAccountEd25519();
      Address aliceAddress = aliceKeyPair.publicKey().deriveAddress();
      Address bobAddress = bobKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();

      // Step 1: Alice sponsors Bob's reserves.
      AccountInfoResult aliceAccountInfo = scanForResult(() -> getValidatedAccountInfo(aliceAddress));

      SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
        .account(aliceAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(aliceAccountInfo.accountData().sequence())
        .sponsee(bobAddress)
        .reserveCount(UnsignedInteger.valueOf(5))
        .signingPublicKey(aliceKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedSponsorshipSet = signatureService.sign(
        aliceKeyPair.privateKey(), sponsorshipSet
      );
      SubmitResult<SponsorshipSet> result = xrplClient.submit(signedSponsorshipSet);
      assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(result.transactionResult().hash(), SponsorshipSet.class));

      // Step 2: Bob creates a Check with Alice as reserve sponsor.
      AccountInfoResult bobAccountInfo = scanForResult(() -> getValidatedAccountInfo(bobAddress));

      CheckCreate unsignedCheck = CheckCreate.builder()
        .account(bobAddress)
        .destination(aliceAddress)
        .sendMax(XrpCurrencyAmount.ofDrops(1000000))
        .fee(feeResult.drops().openLedgerFee())
        .sequence(bobAccountInfo.accountData().sequence())
        .sponsor(aliceAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
        .signingPublicKey(bobKeyPair.publicKey())
        .build();

      SingleSignedTransaction<CheckCreate> bobSignedCheck = signatureService.sign(
        bobKeyPair.privateKey(), unsignedCheck
      );

      Signature aliceSponsorSig = signatureService.sponsorSign(
        aliceKeyPair.privateKey(), unsignedCheck
      );
      SponsorSignature aliceSponsorSignature = SponsorSignature.builder()
        .signingPublicKey(aliceKeyPair.publicKey())
        .transactionSignature(aliceSponsorSig)
        .build();

      CheckCreate signedCheck = CheckCreate.builder()
        .from(unsignedCheck)
        .sponsorSignature(aliceSponsorSignature)
        .transactionSignature(bobSignedCheck.signature())
        .build();

      CheckCreate unsignedCheckWithSponsorSig = CheckCreate.builder()
        .from(unsignedCheck)
        .sponsorSignature(aliceSponsorSignature)
        .build();

      SingleSignedTransaction<CheckCreate> finalCheckTx = SingleSignedTransaction
        .<CheckCreate>builder()
        .unsignedTransaction(unsignedCheckWithSponsorSig)
        .signature(bobSignedCheck.signature())
        .signedTransaction(signedCheck)
        .build();

      SubmitResult<CheckCreate> checkResult = xrplClient.submit(finalCheckTx);
      assertThat(checkResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      scanForResult(() -> getValidatedTransaction(finalCheckTx.hash(), CheckCreate.class));

      // Look up the newly-created Check by scanning Bob's account_objects.
      AccountObjectsResult bobAccountObjects = xrplClient.accountObjects(
        AccountObjectsRequestParams.builder()
          .account(bobAddress)
          .ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .type(AccountObjectType.CHECK)
          .build()
      );
      CheckObject initialCheck = (CheckObject) bobAccountObjects.accountObjects().get(0);
      assertThat(initialCheck.sponsor()).hasValue(aliceAddress);
      Hash256 checkObjectId = initialCheck.index();

      // Step 3: Bob reassigns the Check's reserve sponsorship to Charlie.
      AccountInfoResult bobInfoForTransfer = scanForResult(() -> getValidatedAccountInfo(bobAddress));
      KeyPair charlieKeyPair = createRandomAccountEd25519();
      Address charlieAddress = charlieKeyPair.publicKey().deriveAddress();

      SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
        .account(bobAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(bobInfoForTransfer.accountData().sequence())
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
        .objectId(checkObjectId)
        .sponsor(charlieAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
        .signingPublicKey(bobKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipTransfer> bobSignedTransfer = signatureService.sign(
        bobKeyPair.privateKey(), unsignedTransfer
      );

      Signature charlieSponsorSig = signatureService.sponsorSign(
        charlieKeyPair.privateKey(), unsignedTransfer
      );
      SponsorSignature transferSponsorSig = SponsorSignature.builder()
        .signingPublicKey(charlieKeyPair.publicKey())
        .transactionSignature(charlieSponsorSig)
        .build();

      SponsorshipTransfer signedTransfer = SponsorshipTransfer.builder()
        .from(unsignedTransfer)
        .sponsorSignature(transferSponsorSig)
        .transactionSignature(bobSignedTransfer.signature())
        .build();

      SponsorshipTransfer unsignedTransferWithSponsorSig = SponsorshipTransfer.builder()
        .from(unsignedTransfer)
        .sponsorSignature(transferSponsorSig)
        .build();

      SingleSignedTransaction<SponsorshipTransfer> finalTransferTx = SingleSignedTransaction
        .<SponsorshipTransfer>builder()
        .unsignedTransaction(unsignedTransferWithSponsorSig)
        .signature(bobSignedTransfer.signature())
        .signedTransaction(signedTransfer)
        .build();

      SubmitResult<SponsorshipTransfer> transferResult = xrplClient.submit(finalTransferTx);
      assertThat(transferResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      scanForResult(() -> getValidatedTransaction(finalTransferTx.hash(), SponsorshipTransfer.class));

      // Step 4: Assert Charlie is now the Check's reserve sponsor.
      AccountObjectsResult bobObjectsAfter = xrplClient.accountObjects(
        AccountObjectsRequestParams.builder()
          .account(bobAddress)
          .ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .type(AccountObjectType.CHECK)
          .build()
      );
      CheckObject reassignedCheck = (CheckObject) bobObjectsAfter.accountObjects().get(0);
      assertThat(reassignedCheck.index()).isEqualTo(checkObjectId);
      assertThat(reassignedCheck.sponsor()).hasValue(charlieAddress);
    }

    /**
     * Test: Single-signature sponsee + single-signature sponsor.
     */
    @Test
    void testSponsorshipTransferSingleSigneeSingleSponsor() throws JsonRpcClientErrorException,
      JsonProcessingException {
      KeyPair sponseeKeyPair = createRandomAccountEd25519();
      KeyPair newSponsorKeyPair = createRandomAccountEd25519();
      Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
      Address newSponsorAddress = newSponsorKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();
      AccountInfoResult sponseeAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponseeAddress));

      SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
        .account(sponseeAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponseeAccountInfo.accountData().sequence())
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
        .sponsor(newSponsorAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
        .signingPublicKey(sponseeKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipTransfer> sponseeSigned = signatureService.sign(
        sponseeKeyPair.privateKey(), unsignedTransfer
      );

      Signature sponsorSig = signatureService.sponsorSign(
        newSponsorKeyPair.privateKey(), unsignedTransfer
      );
      SponsorSignature sponsorSignature = SponsorSignature.builder()
        .signingPublicKey(newSponsorKeyPair.publicKey())
        .transactionSignature(sponsorSig)
        .build();

      SponsorshipTransfer signedTransfer = SponsorshipTransfer.builder()
        .from(unsignedTransfer)
        .sponsorSignature(sponsorSignature)
        .transactionSignature(sponseeSigned.signature())
        .build();

      SponsorshipTransfer unsignedWithSponsorSig = SponsorshipTransfer.builder()
        .from(unsignedTransfer)
        .sponsorSignature(sponsorSignature)
        .build();

      SingleSignedTransaction<SponsorshipTransfer> finalTx = SingleSignedTransaction
        .<SponsorshipTransfer>builder()
        .unsignedTransaction(unsignedWithSponsorSig)
        .signature(sponseeSigned.signature())
        .signedTransaction(signedTransfer)
        .build();

      SubmitResult<SponsorshipTransfer> submitResult = xrplClient.submit(finalTx);
      assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

      TransactionResult<SponsorshipTransfer> validatedTx = scanForResult(
        () -> getValidatedTransaction(finalTx.hash(), SponsorshipTransfer.class)
      );
      assertThat(validatedTx.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
    }

    /**
     * Test: Multi-signature sponsee + multi-signature sponsor.
     */
    @Test
    void testSponsorshipTransferMultiSponseeMultiSponsor() throws JsonRpcClientErrorException,
      JsonProcessingException {
      // Create accounts. sponsorAccount is the account being sponsor-multi-signed *for*;
      // sponsor1 and sponsor2 are the two signers on that account's SignerList.
      // (SetSignerList forbids including the account itself in its own signer list, so the
      // sponsor account must be distinct from sponsor1/sponsor2.)
      KeyPair sponseeKeyPair = createRandomAccountEd25519();
      KeyPair alice = createRandomAccountEd25519();
      KeyPair bob = createRandomAccountEd25519();
      KeyPair sponsorAccount = createRandomAccountEd25519();
      KeyPair sponsor1 = createRandomAccountEd25519();
      KeyPair sponsor2 = createRandomAccountEd25519();
      Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();
      Address sponsorAddress = sponsorAccount.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();

      // Set up multi-sig for sponsee
      AccountInfoResult sponseeAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponseeAddress));

      SignerListSet signerListSet = SignerListSet.builder()
        .account(sponseeAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponseeAccountInfo.accountData().sequence())
        .signerQuorum(UnsignedInteger.valueOf(2))
        .addSignerEntries(
          SignerEntryWrapper.of(SignerEntry.builder()
            .account(alice.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE).build()),
          SignerEntryWrapper.of(SignerEntry.builder()
            .account(bob.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE).build())
        )
        .signingPublicKey(sponseeKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
        sponseeKeyPair.privateKey(), signerListSet
      );
      SubmitResult<SignerListSet> signerListResult = xrplClient.submit(signedSignerListSet);
      assertThat(signerListResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      scanForResult(() -> getValidatedTransaction(
        signerListResult.transactionResult().hash(), SignerListSet.class
      ));

      // Set up multi-sig for the sponsor account (signers = sponsor1, sponsor2, quorum 2).
      // Without this, rippled's Transactor::checkMultiSign returns tefNOT_MULTI_SIGNING because
      // keylet::signers(sponsorAddress) does not resolve when the sponsor multi-signs.
      AccountInfoResult sponsorAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponsorAddress));

      SignerListSet sponsorSignerListSet = SignerListSet.builder()
        .account(sponsorAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponsorAccountInfo.accountData().sequence())
        .signerQuorum(UnsignedInteger.valueOf(2))
        .addSignerEntries(
          SignerEntryWrapper.of(SignerEntry.builder()
            .account(sponsor1.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE).build()),
          SignerEntryWrapper.of(SignerEntry.builder()
            .account(sponsor2.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE).build())
        )
        .signingPublicKey(sponsorAccount.publicKey())
        .build();

      SingleSignedTransaction<SignerListSet> signedSponsorSignerList = signatureService.sign(
        sponsorAccount.privateKey(), sponsorSignerListSet
      );
      SubmitResult<SignerListSet> sponsorSignerListResult = xrplClient.submit(signedSponsorSignerList);
      assertThat(sponsorSignerListResult.engineResult()).isEqualTo(SUCCESS_STATUS);
      scanForResult(() -> getValidatedTransaction(
        sponsorSignerListResult.transactionResult().hash(), SignerListSet.class
      ));

      // Create unsigned SponsorshipTransfer
      AccountInfoResult updatedSponseeInfo = scanForResult(() -> getValidatedAccountInfo(sponseeAddress));

      // Fee must cover baseFee * (1 + sponseeSigners + sponsorSigners). With 2 sponsee
      // signers + 2 sponsor signers that's 5x the reference base fee, further scaled by
      // load (see Transactor::calculateBaseFee() + scaleFeeLoad()). Use a generous fixed
      // amount to avoid flake from load scaling between the preceding SignerListSets.
      XrpCurrencyAmount multiSignFee = XrpCurrencyAmount.ofDrops(100000);

      SponsorshipTransfer unsignedTransfer = SponsorshipTransfer.builder()
        .account(sponseeAddress)
        .fee(multiSignFee)
        .sequence(updatedSponseeInfo.accountData().sequence())
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
        .sponsor(sponsorAddress)
        .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
        .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
        .build();

      // Alice and Bob multi-sign as sponsee
      List<Signer> sponseeSigners = Lists.newArrayList(alice, bob).stream()
        .map(keyPair -> Signer.builder()
          .account(keyPair.publicKey().deriveAddress())
          .signingPublicKey(signatureService.derivePublicKey(keyPair.privateKey()))
          .transactionSignature(signatureService.multiSign(keyPair.privateKey(), unsignedTransfer))
          .build())
        .collect(Collectors.toList());

      // Both sponsors multi-sign
      List<Signer> sponsorSigners = Lists.newArrayList(sponsor1, sponsor2).stream()
        .map(keyPair -> Signer.builder()
          .account(keyPair.publicKey().deriveAddress())
          .signingPublicKey(signatureService.derivePublicKey(keyPair.privateKey()))
          .transactionSignature(signatureService.sponsorMultiSign(keyPair.privateKey(), unsignedTransfer))
          .build())
        .collect(Collectors.toList());

      SponsorSignature sponsorSignature = SponsorSignature.builder()
        .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
        .signers(sponsorSigners.stream().map(SignerWrapper::of).collect(Collectors.toList()))
        .build();

      SponsorshipTransfer unsignedWithSponsorSig = SponsorshipTransfer.builder()
        .from(unsignedTransfer)
        .sponsorSignature(sponsorSignature)
        .build();

      MultiSignedTransaction<SponsorshipTransfer> finalTx = MultiSignedTransaction
        .<SponsorshipTransfer>builder()
        .unsignedTransaction(unsignedWithSponsorSig)
        .signerSet(sponseeSigners.stream().collect(Collectors.toSet()))
        .build();

      SubmitMultiSignedResult<SponsorshipTransfer> submitResult = xrplClient.submitMultisigned(finalTx);
      assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

      TransactionResult<SponsorshipTransfer> validatedTx = scanForResult(
        () -> getValidatedTransaction(finalTx.hash(), SponsorshipTransfer.class)
      );
      assertThat(validatedTx.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
    }
  }

  // account_sponsoring RPC integration tests intentionally omitted: rippled's `sponsor` branch
  // does not yet register the `account_sponsoring` RPC handler, so end-to-end coverage is not
  // possible against xrpld:sponsor-local. The client-side request/result types and
  // XrplClient.accountSponsoring() are exercised by unit tests in xrpl4j-core and xrpl4j-client.

  // ==================== SponsorshipSet Flag Tests ====================

  /**
   * Tests for SponsorshipSet with various flags.
   */
  @Nested
  class SponsorshipSetFlagTests {

    @Test
    void testSponsorshipSetWithRequireSignForFee() throws JsonRpcClientErrorException, JsonProcessingException {
      KeyPair sponsorKeyPair = createRandomAccountEd25519();
      KeyPair sponseeKeyPair = createRandomAccountEd25519();
      Address sponsorAddress = sponsorKeyPair.publicKey().deriveAddress();
      Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();
      AccountInfoResult sponsorAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponsorAddress));

      SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
        .account(sponsorAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponsorAccountInfo.accountData().sequence())
        .sponsee(sponseeAddress)
        .feeAmount(XrpCurrencyAmount.ofDrops(500000))
        .flags(SponsorshipSetFlags.builder().tfRequireSignForFee().build())
        .signingPublicKey(sponsorKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedTx = signatureService.sign(
        sponsorKeyPair.privateKey(), sponsorshipSet
      );
      SubmitResult<SponsorshipSet> result = xrplClient.submit(signedTx);
      assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(result.transactionResult().hash(), SponsorshipSet.class));

      // Verify the flag is set on the ledger object
      SponsorshipObject sponsorship = getSponsorshipObject(sponsorAddress, sponseeAddress);
      assertThat(sponsorship.flags().lsfSponsorshipRequireSignForFee()).isTrue();
    }

    @Test
    void testSponsorshipSetDelete() throws JsonRpcClientErrorException, JsonProcessingException {
      KeyPair sponsorKeyPair = createRandomAccountEd25519();
      KeyPair sponseeKeyPair = createRandomAccountEd25519();
      Address sponsorAddress = sponsorKeyPair.publicKey().deriveAddress();
      Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();

      FeeResult feeResult = xrplClient.fee();

      // Create sponsorship
      AccountInfoResult sponsorAccountInfo = scanForResult(() -> getValidatedAccountInfo(sponsorAddress));

      SponsorshipSet createTx = SponsorshipSet.builder()
        .account(sponsorAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(sponsorAccountInfo.accountData().sequence())
        .sponsee(sponseeAddress)
        .feeAmount(XrpCurrencyAmount.ofDrops(100000))
        .signingPublicKey(sponsorKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedCreate = signatureService.sign(
        sponsorKeyPair.privateKey(), createTx
      );
      SubmitResult<SponsorshipSet> createResult = xrplClient.submit(signedCreate);
      assertThat(createResult.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(createResult.transactionResult().hash(), SponsorshipSet.class));

      // Verify sponsorship exists
      SponsorshipObject sponsorship = getSponsorshipObject(sponsorAddress, sponseeAddress);
      assertThat(sponsorship).isNotNull();

      // Delete sponsorship
      AccountInfoResult updatedSponsorInfo = scanForResult(() -> getValidatedAccountInfo(sponsorAddress));

      SponsorshipSet deleteTx = SponsorshipSet.builder()
        .account(sponsorAddress)
        .fee(feeResult.drops().openLedgerFee())
        .sequence(updatedSponsorInfo.accountData().sequence())
        .sponsee(sponseeAddress)
        .flags(SponsorshipSetFlags.builder().tfDeleteObject().build())
        .signingPublicKey(sponsorKeyPair.publicKey())
        .build();

      SingleSignedTransaction<SponsorshipSet> signedDelete = signatureService.sign(
        sponsorKeyPair.privateKey(), deleteTx
      );
      SubmitResult<SponsorshipSet> deleteResult = xrplClient.submit(signedDelete);
      assertThat(deleteResult.engineResult()).isEqualTo(SUCCESS_STATUS);

      scanForResult(() -> getValidatedTransaction(deleteResult.transactionResult().hash(), SponsorshipSet.class));

      // Verify sponsorship no longer exists
      AccountObjectsResult accountObjects = xrplClient.accountObjects(
        AccountObjectsRequestParams.builder()
          .account(sponsorAddress)
          .ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .type(AccountObjectType.SPONSORSHIP)
          .build()
      );

      Optional<SponsorshipObject> deletedSponsorship = accountObjects.accountObjects().stream()
        .filter(obj -> obj instanceof SponsorshipObject)
        .map(obj -> (SponsorshipObject) obj)
        .filter(s -> s.sponsee().equals(sponseeAddress))
        .findFirst();

      assertThat(deletedSponsorship).isEmpty();
    }
  }

  // ==================== Helper Methods ====================

  private SponsorshipObject getSponsorshipObject(Address sponsor, Address sponsee)
    throws JsonRpcClientErrorException {
    AccountObjectsResult accountObjects = xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .account(sponsor)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .type(AccountObjectType.SPONSORSHIP)
        .build()
    );

    return accountObjects.accountObjects().stream()
      .filter(obj -> obj instanceof SponsorshipObject)
      .map(obj -> (SponsorshipObject) obj)
      .filter(s -> s.sponsee().equals(sponsee))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Sponsorship object not found"));
  }
}
