package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;

/**
 * Unit tests for {@link Transaction#checkSponsorshipFields()}.
 */
public class TransactionSponsorshipFieldsTest {

  private static final String TEST_PUBLIC_KEY =
    "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A";
  private static final String TEST_SIGNATURE =
    "3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
      "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA";

  private static final Address ACCOUNT = Address.of("rN7n3zLHyQFRnHaPG4UBpWfXwcz5saAqBN");
  private static final Address DESTINATION = Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy");
  private static final Address SPONSOR = Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe");
  private static final XrpCurrencyAmount FEE = XrpCurrencyAmount.ofDrops(12);
  private static final XrpCurrencyAmount AMOUNT = XrpCurrencyAmount.ofDrops(1000000);

  @Test
  void noSponsorshipPasses() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .build();

    assertThat(payment.sponsor()).isEmpty();
    assertThat(payment.sponsorFlags()).isEmpty();
  }

  @Test
  void validSponsorshipPasses() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
      .build();

    assertThat(payment.sponsor()).isPresent().get().isEqualTo(SPONSOR);
  }

  @Test
  void bothFlagsPasses() {
    long bothFlags = SponsorFlags.SPONSOR_FEE.getValue() | SponsorFlags.SPONSOR_RESERVE.getValue();
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(bothFlags))
      .build();

    assertThat(payment.sponsorFlags()).isPresent().get().isEqualTo(UnsignedInteger.valueOf(bothFlags));
  }

  @Test
  void sponsorWithoutFlagsFails() {
    assertThatThrownBy(() -> Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      // No sponsorFlags
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsor field requires SponsorFlags to be set");
  }

  @Test
  void flagsWithoutSponsorFails() {
    assertThatThrownBy(() -> Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      // No sponsor
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorFlags must not be present without Sponsor field");
  }

  @Test
  void zeroFlagsFails() {
    assertThatThrownBy(() -> Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.ZERO)  // No flags set
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorFlags must have at least one flag set");
  }

  @Test
  void sponsorshipSetExemptFromValidation() {
    // SponsorshipSet uses Sponsor differently (to specify a new sponsor), so it is exempt from this check
    // even though it doesn't set SponsorFlags.
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(ACCOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .sponsee(DESTINATION)
      .build();

    assertThat(sponsorshipSet.sponsor()).isEmpty();
  }

  @Test
  void sponsorshipTransferExemptFromValidation() {
    // SponsorshipTransfer uses Sponsor differently (to identify the new sponsor in a reassignment), so it is
    // exempt from Transaction.checkSponsorshipFields() even though it sets Sponsor/SponsorFlags itself.
    SponsorshipTransfer sponsorshipTransfer = SponsorshipTransfer.builder()
      .account(ACCOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
      .build();

    assertThat(sponsorshipTransfer.sponsor()).isPresent().get().isEqualTo(SPONSOR);
  }

  @Test
  void innerBatchTxnWithFeeSponsorshipFails() {
    assertThatThrownBy(() -> Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .flags(PaymentFlags.builder().tfInnerBatchTxn(true).build())
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Fee sponsorship (spfSponsorFee) is not allowed on an inner Batch transaction");
  }

  @Test
  void innerBatchTxnWithReserveSponsorshipPasses() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .flags(PaymentFlags.builder().tfInnerBatchTxn(true).build())
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_RESERVE.getValue()))
      .build();

    assertThat(payment.sponsor()).isPresent().get().isEqualTo(SPONSOR);
  }

  @Test
  void innerBatchTxnWithEmptySponsorSignaturePasses() {
    SponsorSignature emptySponsorSignature = SponsorSignature.builder().build();

    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .flags(PaymentFlags.builder().tfInnerBatchTxn(true).build())
      .sponsorSignature(emptySponsorSignature)
      .build();

    assertThat(payment.sponsorSignature()).isPresent().get().isEqualTo(emptySponsorSignature);
  }

  @Test
  void innerBatchTxnWithNonEmptySponsorSignatureFails() {
    SponsorSignature realSponsorSignature = SponsorSignature.builder()
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
      .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
      .build();

    assertThatThrownBy(() -> Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .flags(PaymentFlags.builder().tfInnerBatchTxn(true).build())
      .sponsorSignature(realSponsorSignature)
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorSignature must omit TxnSignature and Signers on an inner Batch transaction");
  }

  @Test
  void nonBatchTxnWithEmptySponsorSignatureFails() {
    SponsorSignature emptySponsorSignature = SponsorSignature.builder().build();

    assertThatThrownBy(() -> Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .sponsorSignature(emptySponsorSignature)
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(
        "SponsorSignature must include either TxnSignature or Signers unless this is an inner Batch transaction"
      );
  }

  @Test
  void nonBatchTxnWithRealSponsorSignaturePasses() {
    SponsorSignature realSponsorSignature = SponsorSignature.builder()
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
      .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
      .build();

    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .sponsorSignature(realSponsorSignature)
      .build();

    assertThat(payment.sponsorSignature()).isPresent().get().isEqualTo(realSponsorSignature);
  }
}
