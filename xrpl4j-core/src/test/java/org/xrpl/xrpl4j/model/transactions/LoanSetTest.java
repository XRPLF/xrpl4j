package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for {@link LoanSet} validation logic.
 */
class LoanSetTest {

  private static final Hash256 VALID_BROKER_ID = Hash256.of(
    "C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"
  );
  private static final Hash256 ZERO_HASH = Hash256.of(
    "0000000000000000000000000000000000000000000000000000000000000000"
  );

  // //////////////////////
  // LoanBrokerID checks
  // //////////////////////

  @Test
  void loanBrokerIdMustNotBeZero() {
    assertThatThrownBy(() -> baseBuilder().loanBrokerId(ZERO_HASH).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must not be zero.");
  }

  // //////////////////////
  // PrincipalRequested checks
  // //////////////////////

  @Test
  void principalRequestedZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().principalRequested(Amount.of("0")).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("PrincipalRequested must be greater than zero.");
  }

  @Test
  void principalRequestedNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().principalRequested(Amount.of("-1000")).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("PrincipalRequested must be greater than zero.");
  }

  @Test
  void principalRequestedPositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder().principalRequested(Amount.of("1000000")).build());
  }

  // //////////////////////
  // Fee field checks (LoanServiceFee, LatePaymentFee, ClosePaymentFee)
  // //////////////////////

  @Test
  void loanServiceFeeNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().loanServiceFee(Amount.of("-1")).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanServiceFee must not be negative.");
  }

  @Test
  void loanServiceFeeZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder().loanServiceFee(Amount.of("0")).build());
  }

  @Test
  void latePaymentFeeNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().latePaymentFee(Amount.of("-1")).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LatePaymentFee must not be negative.");
  }

  @Test
  void latePaymentFeeZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder().latePaymentFee(Amount.of("0")).build());
  }

  @Test
  void closePaymentFeeNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().closePaymentFee(Amount.of("-1")).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("ClosePaymentFee must not be negative.");
  }

  @Test
  void closePaymentFeeZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder().closePaymentFee(Amount.of("0")).build());
  }

  // //////////////////////
  // LoanOriginationFee checks
  // //////////////////////

  @Test
  void loanOriginationFeeNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().loanOriginationFee(Amount.of("-1")).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanOriginationFee must not be negative.");
  }

  @Test
  void loanOriginationFeeZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder().loanOriginationFee(Amount.of("0")).build());
  }

  @Test
  void loanOriginationFeePositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder().loanOriginationFee(Amount.of("500")).build());
  }

  @Test
  void loanOriginationFeeExceedsPrincipalIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .principalRequested(Amount.of("1000"))
      .loanOriginationFee(Amount.of("1001"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanOriginationFee must not exceed PrincipalRequested.");
  }

  @Test
  void loanOriginationFeeEqualsPrincipalIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .principalRequested(Amount.of("1000"))
      .loanOriginationFee(Amount.of("1000"))
      .build()
    );
  }

  // //////////////////////
  // Rate field checks (all use max 100000)
  // //////////////////////

  @Test
  void interestRateAtMaxIsValid() {
    assertDoesNotThrow(() -> baseBuilder().interestRate(UnsignedInteger.valueOf(100000)).build());
  }

  @Test
  void interestRateAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().interestRate(UnsignedInteger.valueOf(100001)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("InterestRate must be between 0 and 100000 inclusive.");
  }

  @Test
  void overpaymentFeeAtMaxIsValid() {
    assertDoesNotThrow(() -> baseBuilder().overpaymentFee(UnsignedInteger.valueOf(100000)).build());
  }

  @Test
  void overpaymentFeeAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().overpaymentFee(UnsignedInteger.valueOf(100001)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("OverpaymentFee must be between 0 and 100000 inclusive.");
  }

  @Test
  void lateInterestRateAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().lateInterestRate(UnsignedInteger.valueOf(100001)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LateInterestRate must be between 0 and 100000 inclusive.");
  }

  @Test
  void closeInterestRateAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().closeInterestRate(UnsignedInteger.valueOf(100001)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CloseInterestRate must be between 0 and 100000 inclusive.");
  }

  @Test
  void overpaymentInterestRateAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().overpaymentInterestRate(UnsignedInteger.valueOf(100001)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("OverpaymentInterestRate must be between 0 and 100000 inclusive.");
  }

  // //////////////////////
  // PaymentTotal checks
  // //////////////////////

  @Test
  void paymentTotalZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().paymentTotal(UnsignedInteger.ZERO).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("PaymentTotal must be greater than zero.");
  }

  @Test
  void paymentTotalOneIsValid() {
    assertDoesNotThrow(() -> baseBuilder().paymentTotal(UnsignedInteger.ONE).build());
  }

  // //////////////////////
  // PaymentInterval checks
  // //////////////////////

  @Test
  void paymentIntervalBelow60IsInvalid() {
    assertThatThrownBy(() -> baseBuilder().paymentInterval(UnsignedInteger.valueOf(59)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("PaymentInterval must be at least 60 seconds.");
  }

  @Test
  void paymentIntervalAt60IsValid() {
    assertDoesNotThrow(() -> baseBuilder().paymentInterval(UnsignedInteger.valueOf(60)).build());
  }

  @Test
  void paymentIntervalAbove60IsValid() {
    assertDoesNotThrow(() -> baseBuilder().paymentInterval(UnsignedInteger.valueOf(3600)).build());
  }

  // //////////////////////
  // GracePeriod checks
  // //////////////////////

  @Test
  void gracePeriodBelow60IsInvalid() {
    assertThatThrownBy(() -> baseBuilder().gracePeriod(UnsignedInteger.valueOf(59)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("GracePeriod must be at least 60 seconds.");
  }

  @Test
  void gracePeriodAt60IsValid() {
    assertDoesNotThrow(() -> baseBuilder().gracePeriod(UnsignedInteger.valueOf(60)).build());
  }

  @Test
  void gracePeriodExceedsPaymentIntervalIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .paymentInterval(UnsignedInteger.valueOf(120))
      .gracePeriod(UnsignedInteger.valueOf(121))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("GracePeriod must not exceed PaymentInterval.");
  }

  @Test
  void gracePeriodEqualsPaymentIntervalIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .paymentInterval(UnsignedInteger.valueOf(120))
      .gracePeriod(UnsignedInteger.valueOf(120))
      .build()
    );
  }

  @Test
  void gracePeriodWithoutPaymentIntervalIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .gracePeriod(UnsignedInteger.valueOf(60))
      .build()
    );
  }

  // //////////////////////
  // Happy path
  // //////////////////////

  @Test
  void validMinimalLoanSet() {
    assertDoesNotThrow(() -> baseBuilder().build());
  }

  @Test
  void validFullLoanSet() {
    assertDoesNotThrow(() -> baseBuilder()
      .counterparty(Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE"))
      .data(LoanData.of("AABBCC"))
      .loanOriginationFee(Amount.of("100"))
      .loanServiceFee(Amount.of("10"))
      .latePaymentFee(Amount.of("50"))
      .closePaymentFee(Amount.of("25"))
      .overpaymentFee(UnsignedInteger.valueOf(5000))
      .interestRate(UnsignedInteger.valueOf(10000))
      .lateInterestRate(UnsignedInteger.valueOf(5000))
      .closeInterestRate(UnsignedInteger.valueOf(2000))
      .overpaymentInterestRate(UnsignedInteger.valueOf(1000))
      .paymentTotal(UnsignedInteger.valueOf(12))
      .paymentInterval(UnsignedInteger.valueOf(2592000))
      .gracePeriod(UnsignedInteger.valueOf(86400))
      .build()
    );
  }

  /**
   * Returns a builder pre-populated with required fields for LoanSet.
   */
  private ImmutableLoanSet.Builder baseBuilder() {
    return LoanSet.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .loanBrokerId(VALID_BROKER_ID)
      .principalRequested(Amount.of("1000000"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC"
        )
      );
  }
}
