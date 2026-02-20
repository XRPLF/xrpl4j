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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeBatchFee;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeMultisigNetworkFees;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeNetworkFees;
import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP;
import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP_IN_DROPS;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.SignerListFlags;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Unit tests for {@link FeeUtils}.
 */
public class FeeUtilsTest {

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

  @Test
  void testComputeBatchFeeNullBaseFee() {
    assertThrows(NullPointerException.class,
      () -> computeBatchFee(null, UnsignedInteger.ZERO, XrpCurrencyAmount.ofDrops(0)));
    assertThrows(NullPointerException.class,
      () -> computeBatchFee(XrpCurrencyAmount.ofDrops(10), null, XrpCurrencyAmount.ofDrops(0)));
    assertThrows(NullPointerException.class,
      () -> computeBatchFee(XrpCurrencyAmount.ofDrops(10), UnsignedInteger.ZERO, null));
  }

  @Test
  void testComputeBatchFeeNullInnerFeeSum() {
    assertThrows(NullPointerException.class, () ->
      computeBatchFee(XrpCurrencyAmount.ofDrops(10), UnsignedInteger.ZERO, null)
    );
  }

  @Test
  void testComputeBatchFeeSingleAccountNoInnerFees() {
    // (0+2)*10 + 0 = 20
    XrpCurrencyAmount result = computeBatchFee(
      XrpCurrencyAmount.ofDrops(10), UnsignedInteger.ZERO, XrpCurrencyAmount.ofDrops(0)
    );
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(20));
  }

  @Test
  void testComputeBatchFeeOneBatchSigner() {
    // (1+2)*10 + 0 = 30
    XrpCurrencyAmount result = computeBatchFee(
      XrpCurrencyAmount.ofDrops(10), UnsignedInteger.ONE, XrpCurrencyAmount.ofDrops(0)
    );
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(30));
  }

  @Test
  void testComputeBatchFeeWithInnerFees() {
    // (0+2)*10 + 20 = 40
    XrpCurrencyAmount result = computeBatchFee(
      XrpCurrencyAmount.ofDrops(10), UnsignedInteger.ZERO, XrpCurrencyAmount.ofDrops(20)
    );
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(40));
  }

  @Test
  void testComputeBatchFeeCombined() {
    // (2+2)*10 + 50 = 90
    XrpCurrencyAmount result = computeBatchFee(
      XrpCurrencyAmount.ofDrops(10), UnsignedInteger.valueOf(2), XrpCurrencyAmount.ofDrops(50)
    );
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(90));
  }

  // /////////////////
  // computeBatchFee(FeeResult, UnsignedInteger) - Public method tests
  // /////////////////

  @Test
  void testComputeBatchFeeWithFeeResultNullFeeResult() {
    assertThrows(NullPointerException.class,
      () -> FeeUtils.computeBatchFee(null, UnsignedInteger.ZERO));
  }

  @Test
  void testComputeBatchFeeWithFeeResultNullNumBatchSigners() {
    FeeResult feeResult = feeResultBuilder().build();
    assertThrows(NullPointerException.class,
      () -> FeeUtils.computeBatchFee(feeResult, null));
  }

  @Test
  void testComputeBatchFeeWithFeeResultZeroBatchSigners() {
    // Test with empty queue (recommendedFee = feeLow = 1000 drops)
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

    // recommendedFee = 15 drops (from computeNetworkFeesForEmptyQueue test)
    // numBatchSigners = 0
    // Formula: (0 + 2) * 15 + 0 = 30 drops
    XrpCurrencyAmount result = FeeUtils.computeBatchFee(feeResult, UnsignedInteger.ZERO);
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(30));
  }

  @Test
  void testComputeBatchFeeWithFeeResultOneBatchSigner() {
    // Test with empty queue
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

    // recommendedFee = 15 drops
    // numBatchSigners = 1
    // Formula: (1 + 2) * 15 + 15 = 60 drops
    XrpCurrencyAmount result = FeeUtils.computeBatchFee(feeResult, UnsignedInteger.ONE);
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(60));
  }

  @Test
  void testComputeBatchFeeWithFeeResultMultipleBatchSigners() {
    // Test with moderately filled queue
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

    // recommendedFee = 10000 drops (from computeNetworkFeesForModeratelyFilledQueue test)
    // numBatchSigners = 3
    // Formula: (3 + 2) * 10000 + (3 * 10000) = 50000 + 30000 = 80000 drops
    XrpCurrencyAmount result = FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(3));
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(80000));
  }

  @Test
  void testComputeBatchFeeWithFeeResultHighTraffic() {
    // Test with completely filled queue
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

    // recommendedFee = 2923 drops (from computeNetworkFeesForCompletelyFilledQueue test)
    // numBatchSigners = 5
    // Formula: (5 + 2) * 2923 + (5 * 2923) = 20461 + 14615 = 35076 drops
    XrpCurrencyAmount result = FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(5));
    assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(35076));
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
