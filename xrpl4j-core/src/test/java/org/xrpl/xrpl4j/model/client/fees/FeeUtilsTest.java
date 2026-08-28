package org.xrpl.xrpl4j.model.client.fees;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeMultisigNetworkFees;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeNetworkFees;
import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP;
import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP_IN_DROPS;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.SignerListFlags;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutableEscrowFinish;
import org.xrpl.xrpl4j.model.transactions.LoanPay;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Unit tests for {@link FeeUtils}.
 */
public class FeeUtilsTest {

  private static final Address ALICE = Address.of("r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV");
  private static final Address BOB = Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC");
  private static final Address CAROL = Address.of("r3ubyDp4gPGKH5bJx9KMmzpTSTW7EtRixS");
  private static final Address DAVE = Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z");
  private static final Address FRANK = Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK");

  private static final PublicKey PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(
    "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A"
  );


  @Test
  public void nullInputForComputeMultiSigFee() {
    assertThrows(
      NullPointerException.class,
      () -> computeMultisigNetworkFees(null, mock(SignerListObject.class))
    );

    assertThrows(
      NullPointerException.class,
      () -> computeMultisigNetworkFees(mock(FeeResult.class), null)
    );
  }

  @Test
  public void simpleComputeMultiSigFee() {
    FeeResult feeResult = feeResultBuilder().build();
    SignerListObject object = SignerListObject.builder()
      .flags(SignerListFlags.UNSET)
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("5904C0DC72C58A83AEFED2FFC5386356AA83FCA6A88C89D00646E51E687CDBE4"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(16061435))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
            .signerWeight(UnsignedInteger.valueOf(2))
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
            .signerWeight(UnsignedInteger.valueOf(1))
            .build()
        )
      )
      .signerListId(UnsignedInteger.ZERO)
      .signerQuorum(UnsignedInteger.valueOf(3))
      .index(Hash256.of("A9C28A28B85CD533217F5C0A0C7767666B093FA58A0F2D80026FCC4CD932DDC7"))
      .build();

    assertThat(computeMultisigNetworkFees(feeResult, object).recommendedFee())
      .isEqualTo(XrpCurrencyAmount.ofDrops(15024));
    assertThat(computeMultisigNetworkFees(feeResult, object).feeLow())
      .isEqualTo(XrpCurrencyAmount.ofDrops(3000));
    assertThat(computeMultisigNetworkFees(feeResult, object).feeMedium())
      .isEqualTo(XrpCurrencyAmount.ofDrops(15024));
    assertThat(computeMultisigNetworkFees(feeResult, object).feeHigh())
      .isEqualTo(XrpCurrencyAmount.ofDrops(30000));
  }

  @Test
  public void nullInputForCalculateFeeDynamically() {
    assertThrows(
      NullPointerException.class,
      () -> computeNetworkFees(null)
    );
  }

  @Test
  public void computeNetworkFeesForAlmostEmptyQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(1))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();

    ComputedNetworkFees computedNetworkFees = computeNetworkFees(feeResult);
    assertThat(computedNetworkFees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(1000));
    assertThat(computedNetworkFees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(5008));
    assertThat(computedNetworkFees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(computedNetworkFees.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(5008));
  }

  @Test
  public void computeNetworkFeesForModeratelyFilledQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(220))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(1000));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
  }

  @Test
  public void computeNetworkFeesForLessThanModerateTraffic() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(100))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(1000));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(5008));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(5008));
  }

  @Test
  public void computeNetworkFeesForCompletelyFilledQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(110))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(100))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2657))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(110))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(225));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(2923));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(2923));
  }

  @Test
  public void computeNetworkFeesForEmptyQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(0))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(100))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2657))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(110))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(150));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(2923));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
  }

  @Test
  public void calculateFeeUsingXummTestValuesForLow() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(1))
      .currentQueueSize(UnsignedInteger.valueOf(0))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(5000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(5343))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(10))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(10))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(2000))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(225));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(5877));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
  }

  @Test
  public void calculateFeeUsingXummTestValuesForMedium() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(1))
      .currentQueueSize(UnsignedInteger.valueOf(1924))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(5000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(5343))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(10))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(10))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(2000))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(225));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(5877));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(225));
  }

  @Test
  public void calculateFeeUsingXummTestValuesForHigh() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(1))
      .currentQueueSize(UnsignedInteger.valueOf(2000))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(5000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(5343))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(10))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(10))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(2000))
      .status("success")
      .build();

    ComputedNetworkFees networkFeeResult = computeNetworkFees(feeResult);
    assertThat(networkFeeResult.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
    assertThat(networkFeeResult.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(225));
    assertThat(networkFeeResult.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(5877));
    assertThat(networkFeeResult.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(5877));
  }

  @Test
  void testQueueIsEmpty() {
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.queueIsEmpty(null)
    );

    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(-1))).isTrue();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.ZERO)).isTrue();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(0.1))).isFalse();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(0.4))).isFalse();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(0.5))).isFalse();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(0.6))).isFalse();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(0.999999))).isFalse();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.ONE)).isFalse();
    assertThat(FeeUtils.queueIsEmpty(BigDecimal.valueOf(2))).isFalse();
  }

  @Test
  void testQueueIsNotEmptyAndNotFull() {
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.queueIsNotEmptyAndNotFull(null)
    );

    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(-1))).isFalse();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.ZERO)).isFalse();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(0.1))).isTrue();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(0.4))).isTrue();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(0.5))).isTrue();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(0.6))).isTrue();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(0.999999))).isTrue();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.ONE)).isFalse();
    assertThat(FeeUtils.queueIsNotEmptyAndNotFull(BigDecimal.valueOf(2))).isFalse();
  }

  @Test
  void testToUnsignedLongSafe() {
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.toUnsignedLongSafe(null)
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> FeeUtils.toUnsignedLongSafe(BigInteger.valueOf(-1))
    );

    assertThat(FeeUtils.toUnsignedLongSafe(BigInteger.ZERO)).isEqualTo(UnsignedLong.ZERO);
    assertThat(FeeUtils.toUnsignedLongSafe(BigInteger.ONE)).isEqualTo(UnsignedLong.ONE);
    assertThat(FeeUtils.toUnsignedLongSafe(BigInteger.valueOf(MAX_XRP))).isEqualTo(UnsignedLong.valueOf(MAX_XRP));
    assertThat(FeeUtils.toUnsignedLongSafe(BigInteger.valueOf(MAX_XRP_IN_DROPS)))
      .isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(FeeUtils.toUnsignedLongSafe(UnsignedLong.MAX_VALUE.bigIntegerValue())).isEqualTo(UnsignedLong.MAX_VALUE);
    assertThat(FeeUtils.toUnsignedLongSafe(UnsignedLong.MAX_VALUE.bigIntegerValue().add(BigInteger.ONE)))
      .isEqualTo(UnsignedLong.MAX_VALUE);
  }

  @Test
  void testMin() {
    // Null checks
    assertThrows(NullPointerException.class, () -> FeeUtils.min(null));
    assertThrows(NullPointerException.class, () -> FeeUtils.max(BigInteger.ZERO, (BigInteger[]) null));
    assertThrows(NullPointerException.class, () -> FeeUtils.max(BigInteger.ZERO, (BigInteger) null));

    assertThat(FeeUtils.min(BigInteger.valueOf(-1), BigInteger.ZERO)).isEqualTo(BigInteger.valueOf(-1));
    assertThat(FeeUtils.min(BigInteger.ZERO, BigInteger.ZERO)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.min(BigInteger.ZERO, BigInteger.ONE)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.min(BigInteger.ONE, BigInteger.ZERO)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.min(BigInteger.ONE, BigInteger.ONE)).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.min(BigInteger.ONE)).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.min(BigInteger.ZERO, BigInteger.valueOf(MAX_XRP_IN_DROPS))).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.min(BigInteger.valueOf(MAX_XRP_IN_DROPS), BigInteger.ONE)).isEqualTo(BigInteger.ONE);
  }

  @Test
  void testMax() {
    // Null checks
    assertThrows(NullPointerException.class, () -> FeeUtils.max(null));
    assertThrows(NullPointerException.class, () -> FeeUtils.max(BigInteger.ZERO, (BigInteger[]) null));
    assertThrows(NullPointerException.class, () -> FeeUtils.max(BigInteger.ZERO, (BigInteger) null));

    assertThat(FeeUtils.max(BigInteger.valueOf(-1), BigInteger.ZERO)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.max(BigInteger.ZERO, BigInteger.ZERO)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.max(BigInteger.ZERO, BigInteger.ONE)).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.max(BigInteger.ONE, BigInteger.ZERO)).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.max(BigInteger.ONE, BigInteger.ONE)).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.max(BigInteger.ONE)).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.max(BigInteger.ZERO, BigInteger.valueOf(MAX_XRP_IN_DROPS)))
      .isEqualTo(BigInteger.valueOf(MAX_XRP_IN_DROPS));
    assertThat(FeeUtils.max(BigInteger.valueOf(MAX_XRP_IN_DROPS), BigInteger.ONE))
      .isEqualTo(BigInteger.valueOf(MAX_XRP_IN_DROPS));
  }

  @Test
  void testDivideBigDecimalsToBigInteger() {
    final BigDecimal nullBigDecimal = null;
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.divideToBigInteger(nullBigDecimal, BigDecimal.ONE)
    );
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.divideToBigInteger(BigDecimal.ONE, nullBigDecimal)
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> FeeUtils.divideToBigInteger(BigDecimal.ONE, BigDecimal.valueOf(-1))
    );

    assertThat(FeeUtils.divideToBigInteger(BigDecimal.valueOf(-1), BigDecimal.ONE)).isEqualTo(BigInteger.valueOf(-1));
    assertThat(FeeUtils.divideToBigInteger(BigDecimal.ONE, BigDecimal.valueOf(2))).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.divideToBigInteger(BigDecimal.ONE, BigDecimal.valueOf(4))).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.divideToBigInteger(BigDecimal.ONE, BigDecimal.TEN)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.divideToBigInteger(BigDecimal.TEN, BigDecimal.valueOf(2))).isEqualTo(BigInteger.valueOf(5));
    assertThat(FeeUtils.divideToBigInteger(new BigDecimal(UnsignedLong.MAX_VALUE.bigIntegerValue()), BigDecimal.ONE))
      .isEqualTo(UnsignedLong.MAX_VALUE.bigIntegerValue());
    assertThat(FeeUtils.divideToBigInteger(
      new BigDecimal(UnsignedLong.MAX_VALUE.bigIntegerValue()),
      new BigDecimal(UnsignedLong.MAX_VALUE.bigIntegerValue()))
    ).isEqualTo(BigInteger.ONE);
  }

  @Test
  void testDivideBigIntegersToBigInteger() {
    final BigInteger nullBigInteger = null;
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.divideToBigInteger(nullBigInteger, BigInteger.ONE)
    );
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.divideToBigInteger(BigInteger.ONE, nullBigInteger)
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> FeeUtils.divideToBigInteger(BigInteger.ONE, BigInteger.valueOf(-1))
    );

    assertThat(FeeUtils.divideToBigInteger(BigInteger.valueOf(-1), BigInteger.ONE)).isEqualTo(BigInteger.valueOf(-1));
    assertThat(FeeUtils.divideToBigInteger(BigInteger.ONE, BigInteger.valueOf(2))).isEqualTo(BigInteger.ONE);
    assertThat(FeeUtils.divideToBigInteger(BigInteger.ONE, BigInteger.valueOf(4))).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.divideToBigInteger(BigInteger.ONE, BigInteger.TEN)).isEqualTo(BigInteger.ZERO);
    assertThat(FeeUtils.divideToBigInteger(BigInteger.TEN, BigInteger.valueOf(2))).isEqualTo(BigInteger.valueOf(5));
    assertThat(FeeUtils.divideToBigInteger(UnsignedLong.MAX_VALUE.bigIntegerValue(), BigInteger.ONE))
      .isEqualTo(UnsignedLong.MAX_VALUE.bigIntegerValue());
    assertThat(FeeUtils.divideToBigInteger(
      UnsignedLong.MAX_VALUE.bigIntegerValue(),
      UnsignedLong.MAX_VALUE.bigIntegerValue())
    ).isEqualTo(BigInteger.ONE);
  }

  @Test
  void testMultiplyToBigInteger() {
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.multiplyToBigInteger(null, BigDecimal.ONE)
    );
    assertThrows(
      NullPointerException.class,
      () -> FeeUtils.multiplyToBigInteger(BigInteger.ONE, null)
    );

    assertThat(FeeUtils.multiplyToBigInteger(BigInteger.valueOf(-1), BigDecimal.ONE)).isEqualTo(BigInteger.valueOf(-1));
    assertThat(FeeUtils.multiplyToBigInteger(BigInteger.ONE, BigDecimal.valueOf(2))).isEqualTo(BigInteger.valueOf(2));
    assertThat(FeeUtils.multiplyToBigInteger(BigInteger.ONE, BigDecimal.valueOf(4))).isEqualTo(BigInteger.valueOf(4));
    assertThat(FeeUtils.multiplyToBigInteger(BigInteger.ONE, BigDecimal.TEN)).isEqualTo(BigInteger.TEN);
    assertThat(FeeUtils.multiplyToBigInteger(BigInteger.TEN, BigDecimal.valueOf(2))).isEqualTo(BigInteger.valueOf(20));
    assertThat(FeeUtils.multiplyToBigInteger(UnsignedLong.MAX_VALUE.bigIntegerValue(), BigDecimal.ONE))
      .isEqualTo(UnsignedLong.MAX_VALUE.bigIntegerValue());
    assertThat(FeeUtils.multiplyToBigInteger(
      UnsignedLong.MAX_VALUE.bigIntegerValue(),
      new BigDecimal(UnsignedLong.MAX_VALUE.bigIntegerValue())
    )).isEqualTo(new BigInteger("340282366920938463426481119284349108225"));
  }

  // /////////////////
  // Generic — the types with no special rule
  // /////////////////

  @Test
  void singleSignedPaymentCostsOneBaseFee() {
    assertFeeUnits(paramsFor(payment()), 1);
  }

  @Test
  void multiSignedPaymentChargesEachAdditionalSignature() {
    assertFeeUnits(
      FeeParams.builder()
        .feeResult(feeResultBuilder().build())
        .transaction(payment())
        .signersCount(UnsignedInteger.valueOf(3)),
      4
    );
  }

  @Test
  void singleKeySponsorIsFree() {
    // The sponsor signature lives in SponsorSignature.TxnSignature, which rippled does not charge for.
    assertFeeUnits(paramsFor(sponsoredPayment()), 1);
  }

  @Test
  void multiSignedSponsorChargesEachSponsorSignature() {
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResultBuilder().build()).transaction(sponsoredPayment())
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      4
    );
  }

  @Test
  void ownAndSponsorSignaturesBothCount() {
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResultBuilder().build()).transaction(sponsoredPayment())
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      6
    );
  }

  @Test
  void allThreeFeeLevelsAreScaled() {
    ComputedNetworkFees fees = FeeUtils.computeFee(paramsFor(payment()).signersCount(UnsignedInteger.ONE).build());
    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(2000));
    assertThat(fees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(10016));
    assertThat(fees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(20000));
  }

  // /////////////////
  // EscrowFinish
  // /////////////////

  @Test
  void escrowFinishWithoutFulfillmentCostsOneBaseFee() {
    assertFeeUnits(paramsFor(escrowFinish(0)), 1);
  }

  @Test
  void escrowFinishWithFulfillmentAddsTheSizeSurcharge() {
    // rippled charges by the on-wire fulfillment blob, not the preimage. A 32-byte preimage DER-encodes to a
    // 36-byte blob, so the surcharge is 32 + 36/16 = 34: total 1 + 34 = 35.
    assertFeeUnits(paramsFor(escrowFinish(32)), 35);
  }

  @Test
  void escrowFinishSurchargeUsesTheEncodedBlobSizeNotThePreimageSize() {
    // Boundary case exposing the difference: a 28-byte preimage DER-encodes to a 32-byte blob. Measuring the blob
    // (correct, matches rippled) gives 32 + 32/16 = 34 -> total 35. Measuring the preimage (the old, buggy
    // behavior) would give 32 + 28/16 = 33 -> total 34, underpaying by one base fee.
    assertFeeUnits(paramsFor(escrowFinish(28)), 35);
  }

  @Test
  void escrowFinishSurchargeIsAddedToTheSignatureTerms() {
    // (1 + 2) + (32 + 32/16) = 37 — the signer terms EscrowFinish.computeFee omits.
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResultBuilder().build()).transaction(escrowFinish(32))
        .signersCount(UnsignedInteger.valueOf(2)),
      37
    );
  }

  // /////////////////
  // Lending
  // /////////////////

  @Test
  void loanSetChargesASingleCounterpartySignature() {
    // 1 + 1. Unlike the transaction's own signature, a lone counterparty signature is charged, which is why
    // counterpartySignatureCount defaults to one rather than zero.
    assertFeeUnits(paramsFor(loanSet()), 2);
  }

  @Test
  void loanSetChargesOwnSponsorAndCounterpartySignatures() {
    // 1 + 2 own + 2 sponsor + 3 counterparty
    assertFeeUnits(
      paramsFor(loanSet())
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(2))
        .counterpartySignatureCount(UnsignedInteger.valueOf(3)),
      8
    );
  }

  @Test
  void loanPayMultipliesTheWholeFeeByItsIncrements() {
    // 4 increments x (1 + 0 + 0)
    assertFeeUnits(paramsFor(loanPay()).loanPaymentFeeIncrements(UnsignedInteger.valueOf(4)), 4);
  }

  @Test
  void loanPayIncrementsMultiplyTheSignatureTermsToo() {
    // 2 increments x (1 + 2 own)
    assertFeeUnits(
      paramsFor(loanPay())
        .loanPaymentFeeIncrements(UnsignedInteger.valueOf(2))
        .signersCount(UnsignedInteger.valueOf(2)),
      6
    );
  }

  @Test
  void rejectsLoanPaymentFeeIncrementsOutsideTheAllowedRange() {
    assertThatThrownBy(() -> paramsFor(loanPay()).loanPaymentFeeIncrements(UnsignedInteger.ZERO).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be between 1 and 20");

    assertThatThrownBy(() -> paramsFor(loanPay()).loanPaymentFeeIncrements(UnsignedInteger.valueOf(21)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be between 1 and 20");
  }

  // /////////////////
  // Owner reserve
  // /////////////////

  @Test
  void accountDeleteCostsOneOwnerReserveFlat() {
    ComputedNetworkFees fees = FeeUtils.computeFee(FeeParams.builder()
      .feeResult(feeResultBuilder().build())
      .transaction(accountDelete())
      .ownerReserve(XrpCurrencyAmount.ofDrops(200000))
      .build());

    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
    assertThat(fees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
    assertThat(fees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
  }

  @Test
  void accountDeleteIgnoresSignatureCounts() {
    // calculateOwnerReserveFee returns the increment flat, so a multi-signed AccountDelete costs the same.
    ComputedNetworkFees fees = FeeUtils.computeFee(FeeParams.builder()
      .feeResult(feeResultBuilder().build())
      .transaction(accountDelete())
      .ownerReserve(XrpCurrencyAmount.ofDrops(200000))
      .signersCount(UnsignedInteger.valueOf(5))
      .build());

    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
  }

  // /////////////////
  // Batch
  // /////////////////

  @Test
  void singleAccountBatchRequiresNoBatchSigners() {
    // 2 outer + 0 batchSigners + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(ALICE, 1), innerPayment(ALICE, 2));
    assertThat(batch.requiredSigners()).isEmpty();
    assertFeeUnits(paramsFor(batch), 4);
  }

  @Test
  void multiAccountBatchDerivesItsSignersFromTheInners() {
    // 2 outer + 2 batchSigners + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertThat(batch.requiredSigners()).containsExactlyInAnyOrder(BOB, CAROL);
    assertFeeUnits(paramsFor(batch), 6);
  }

  @Test
  void batchReadsSignatureCountsFromCollectedBatchSigners() {
    // 2 outer + (1 bob + 3 carol) + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    Batch signed = Batch.builder().from(batch)
      .batchSigners(Lists.newArrayList(singleSignature(BOB), multiSignature(CAROL, 3)))
      .build();

    assertFeeUnits(paramsFor(signed), 8);
  }

  @Test
  void batchPricedBeforeSigningCanBeToldAParticipantWillMultiSign() {
    // The same 8, forecast rather than counted.
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertFeeUnits(
      paramsFor(batch).putSignaturesPerBatchSigner(CAROL, UnsignedInteger.valueOf(3)),
      8
    );
  }

  @Test
  void batchRequiredSignerNeedNotBeAnInnerAccount() {
    // 2 outer + 3 batchSigners + (1 + 1 + 1) inners, where the signers are {carol, dave, frank}.
    Batch batch = batch(ALICE,
      innerPayment(CAROL, 1),
      Payment.builder().from(innerPayment(BOB, 1)).delegate(DAVE).build(),
      Payment.builder().from(innerPayment(CAROL, 2))
        .sponsor(FRANK)
        .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
        .sponsorSignature(org.xrpl.xrpl4j.model.transactions.SponsorSignature.builder().build())
        .build()
    );

    assertThat(batch.requiredSigners()).containsExactlyInAnyOrder(CAROL, DAVE, FRANK);
    assertFeeUnits(paramsFor(batch), 8);
  }

  @Test
  void batchPricesConfidentialInnersAtTenBaseFeesEach() {
    // 2 outer + 0 batchSigners + (10 + 10) inners
    Batch batch = batch(ALICE, innerConfidentialSend(ALICE, 1), innerConfidentialSend(ALICE, 2));
    assertFeeUnits(paramsFor(batch), 22);
  }

  @Test
  void batchPricesAMixOfConfidentialAndRegularInners() {
    // 2 outer + 2 batchSigners + 2 outer signers + 3 sponsor signers + (10 + 1) inners
    Batch batch = batch(ALICE, innerConfidentialSend(BOB, 1), innerPayment(CAROL, 1));
    assertFeeUnits(
      paramsFor(batch)
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      20
    );
  }

  @Test
  void standaloneConfidentialSendCostsTenBaseFees() {
    assertFeeUnits(paramsFor(innerConfidentialSend(ALICE, 1)), 10);
  }

  @Test
  void confidentialSendChargesOwnAndSponsorSignaturesOnTopOfTheSurcharge() {
    // 1 + 2 own + 3 sponsor + 9 = 15 — the combination that has no answer today.
    assertFeeUnits(
      paramsFor(innerConfidentialSend(ALICE, 1))
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      15
    );
  }

  @Test
  void batchChargesOuterSignatureAndSponsorTerms() {
    // 2 outer + 2 batchSigners + 2 outer signers + 3 sponsor signers + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertFeeUnits(
      paramsFor(batch)
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      11
    );
  }

  @Test
  void batchAddsAnOwnerReserveForEachOwnerReserveInner() {
    // 2 outer + 0 batchSigners + 1 payment inner = 3 base fees, plus one flat owner reserve.
    Batch batch = batch(ALICE, innerPayment(ALICE, 1), innerAccountDelete(ALICE, 2));
    ComputedNetworkFees fees = FeeUtils.computeFee(FeeParams.builder()
      .feeResult(feeResultBuilder().build())
      .transaction(batch)
      .ownerReserve(XrpCurrencyAmount.ofDrops(200000))
      .build());

    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 1000 + 200000));
    assertThat(fees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 5008 + 200000));
    assertThat(fees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 10000 + 200000));
  }

  // /////////////////
  // FeeParams validation
  // /////////////////

  @Test
  void rejectsASignatureCountForAnAccountThatNeedNotSign() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertThatThrownBy(() -> paramsFor(batch).putSignaturesPerBatchSigner(DAVE, UnsignedInteger.valueOf(3)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("is not required to sign this Batch");
  }

  @Test
  void rejectsAForecastWhenSignaturesHaveAlreadyBeenCollected() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    Batch signed = Batch.builder().from(batch)
      .batchSigners(Lists.newArrayList(singleSignature(BOB), singleSignature(CAROL)))
      .build();

    assertThatThrownBy(() -> paramsFor(signed).putSignaturesPerBatchSigner(BOB, UnsignedInteger.valueOf(3)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("before its signatures exist");
  }

  @Test
  void rejectsFieldsThatDoNotApplyToTheTransaction() {
    assertThatThrownBy(() -> paramsFor(payment()).counterpartySignatureCount(UnsignedInteger.valueOf(2)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a LoanSet");

    assertThatThrownBy(() -> paramsFor(payment()).loanPaymentFeeIncrements(UnsignedInteger.valueOf(2)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a LoanPay");

    assertThatThrownBy(() -> paramsFor(payment()).putSignaturesPerBatchSigner(BOB, UnsignedInteger.ONE).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a Batch");
  }

  @Test
  void rejectsAMissingOrSuperfluousOwnerReserve() {
    assertThatThrownBy(() -> paramsFor(accountDelete()).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ownerReserve must be supplied");

    assertThatThrownBy(() -> paramsFor(payment()).ownerReserve(XrpCurrencyAmount.ofDrops(1)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ownerReserve must be supplied");
  }

  @Test
  void rejectsAPseudoTransaction() {
    Transaction setFee = mock(Transaction.class);
    when(setFee.transactionType()).thenReturn(TransactionType.SET_FEE);
    assertThatThrownBy(() -> paramsFor(setFee).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("pseudo-transaction");
  }

  @Test
  void rejectsAnUnknownTransactionType() {
    Transaction unknown = mock(Transaction.class);
    when(unknown.transactionType()).thenReturn(TransactionType.UNKNOWN);
    assertThatThrownBy(() -> paramsFor(unknown).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("fee rules are not known");
  }

  @Test
  void rejectsSignatureCountsBeyondTheSignerListLimit() {
    assertThatThrownBy(() -> paramsFor(payment()).signersCount(UnsignedInteger.valueOf(33)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must not exceed 32");
  }

  // /////////////////
  // Helpers
  // /////////////////

  private void assertFeeUnits(final ImmutableFeeParams.Builder builder, final long expectedUnits) {
    assertThat(FeeUtils.computeFee(builder.build()).feeLow())
      .isEqualTo(XrpCurrencyAmount.ofDrops(expectedUnits * 1000));
  }

  private ImmutableFeeParams.Builder paramsFor(final Transaction transaction) {
    return FeeParams.builder().feeResult(feeResultBuilder().build()).transaction(transaction);
  }

  private Payment payment() {
    return Payment.builder()
      .account(ALICE)
      .destination(BOB)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private Payment sponsoredPayment() {
    return Payment.builder().from(payment())
      .sponsor(FRANK)
      .sponsorFlags(SponsorFlags.SPONSOR_FEE)
      .build();
  }

  private Payment innerPayment(final Address account, final int sequence) {
    return Payment.builder()
      .account(account)
      .destination(DAVE)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(sequence))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();
  }

  private ConfidentialMptSend innerConfidentialSend(final Address account, final int sequence) {
    return ConfidentialMptSend.builder()
      .account(account)
      .destination(DAVE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(sequence))
      .flags(org.xrpl.xrpl4j.model.flags.TransactionFlags.INNER_BATCH_TXN)
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179" + Strings.repeat("11", 20)))
      .senderEncryptedAmount(EncryptedAmount.of(Strings.repeat("AB", 66)))
      .destinationEncryptedAmount(EncryptedAmount.of(Strings.repeat("CD", 66)))
      .issuerEncryptedAmount(EncryptedAmount.of(Strings.repeat("EF", 66)))
      .zkProof(ConfidentialMptSendProof.fromHex(Strings.repeat("34", 946)))
      .amountCommitment(Commitment.of(Strings.repeat("02", 33)))
      .balanceCommitment(Commitment.of(Strings.repeat("03", 33)))
      .build();
  }

  private AccountDelete innerAccountDelete(final Address account, final int sequence) {
    return AccountDelete.builder()
      .account(account)
      .destination(DAVE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(sequence))
      .flags(org.xrpl.xrpl4j.model.flags.TransactionFlags.INNER_BATCH_TXN)
      .build();
  }

  private LoanSet loanSet() {
    return LoanSet.builder()
      .account(ALICE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .loanBrokerId(Hash256.of("C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"))
      .principalRequested(Amount.of("1000000"))
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private LoanPay loanPay() {
    return LoanPay.builder()
      .account(ALICE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .loanId(Hash256.of("C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"))
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private AccountDelete accountDelete() {
    return AccountDelete.builder()
      .account(ALICE)
      .destination(BOB)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  /**
   * An {@link EscrowFinish} carrying a PREIMAGE-SHA-256 fulfillment built from a {@code preimageBytes}-byte preimage,
   * or no fulfillment when {@code preimageBytes} is 0.
   */
  private EscrowFinish escrowFinish(final int preimageBytes) {
    ImmutableEscrowFinish.Builder builder = EscrowFinish.builder()
      .account(ALICE)
      .owner(BOB)
      .offerSequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY);
    return preimageBytes == 0 ?
      builder.build() : builder.fulfillment(PreimageSha256Fulfillment.from(new byte[preimageBytes])).build();
  }

  private Batch batch(final Address outerAccount, final Transaction... inners) {
    List<RawTransactionWrapper> wrappers = Lists.newArrayList();
    for (Transaction inner : inners) {
      wrappers.add(RawTransactionWrapper.of(inner));
    }
    return Batch.builder()
      .account(outerAccount)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .rawTransactions(wrappers)
      .build();
  }

  private BatchSignerWrapper singleSignature(final Address account) {
    return BatchSignerWrapper.of(BatchSigner.builder()
      .account(account)
      .signingPublicKey(PUBLIC_KEY)
      .transactionSignature(Signature.fromBase16("ABCD"))
      .build());
  }

  private BatchSignerWrapper multiSignature(final Address account, final int signatureCount) {
    List<SignerWrapper> signers = Lists.newArrayList();
    for (int i = 0; i < signatureCount; i++) {
      signers.add(SignerWrapper.of(Signer.builder()
        .signingPublicKey(PUBLIC_KEY)
        .transactionSignature(Signature.fromBase16("ABCD"))
        .build()));
    }
    return BatchSignerWrapper.of(BatchSigner.builder().account(account).signers(signers).build());
  }

  private ImmutableFeeResult.Builder feeResultBuilder() {
    return FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(1))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success");
  }
}
