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
 * Unit tests for {@link LoanBrokerSet} validation logic.
 */
class LoanBrokerSetTest {

  private static final Hash256 VALID_VAULT_ID = Hash256.of(
    "D70384C6A81A5375B1DF840FAD6E7B5672780BC1583CEAB7B2247B8D456B28CB"
  );
  private static final Hash256 ZERO_HASH = Hash256.of(
    "0000000000000000000000000000000000000000000000000000000000000000"
  );
  private static final Hash256 VALID_BROKER_ID = Hash256.of(
    "C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"
  );

  // //////////////////////
  // VaultID checks
  // //////////////////////

  @Test
  void vaultIdMustNotBeZero() {
    assertThatThrownBy(() -> baseBuilder().vaultId(ZERO_HASH).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultID must not be zero.");
  }

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
  // ManagementFeeRate checks
  // //////////////////////

  @Test
  void managementFeeRateAtMaxIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .managementFeeRate(UnsignedInteger.valueOf(10000))
      .build()
    );
  }

  @Test
  void managementFeeRateAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .managementFeeRate(UnsignedInteger.valueOf(10001))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("ManagementFeeRate must be between 0 and 10000 inclusive.");
  }

  @Test
  void managementFeeRateAtZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .managementFeeRate(UnsignedInteger.ZERO)
      .build()
    );
  }

  // //////////////////////
  // DebtMaximum checks
  // //////////////////////

  @Test
  void debtMaximumMustNotBeNegative() {
    assertThatThrownBy(() -> baseBuilder()
      .debtMaximum(Amount.of("-1"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("DebtMaximum must not be negative.");
  }

  @Test
  void debtMaximumAtZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .debtMaximum(Amount.of("0"))
      .build()
    );
  }

  @Test
  void debtMaximumPositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .debtMaximum(Amount.of("5000000000"))
      .build()
    );
  }

  // //////////////////////
  // CoverRateMinimum checks
  // //////////////////////

  @Test
  void coverRateMinimumAtMaxIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(100000))
      .coverRateLiquidation(UnsignedInteger.valueOf(1))
      .build()
    );
  }

  @Test
  void coverRateMinimumAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(100001))
      .coverRateLiquidation(UnsignedInteger.valueOf(1))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateMinimum must be between 0 and 100000 inclusive.");
  }

  // //////////////////////
  // CoverRateLiquidation checks
  // //////////////////////

  @Test
  void coverRateLiquidationAtMaxIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(1))
      .coverRateLiquidation(UnsignedInteger.valueOf(100000))
      .build()
    );
  }

  @Test
  void coverRateLiquidationAboveMaxIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(1))
      .coverRateLiquidation(UnsignedInteger.valueOf(100001))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateLiquidation must be between 0 and 100000 inclusive.");
  }

  // //////////////////////
  // CoverRate consistency checks
  // //////////////////////

  @Test
  void coverRateMinimumNonZeroWithLiquidationZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(5000))
      .coverRateLiquidation(UnsignedInteger.ZERO)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateMinimum and CoverRateLiquidation must both be zero or both be non-zero.");
  }

  @Test
  void coverRateLiquidationNonZeroWithMinimumZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.ZERO)
      .coverRateLiquidation(UnsignedInteger.valueOf(5000))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateMinimum and CoverRateLiquidation must both be zero or both be non-zero.");
  }

  @Test
  void coverRateMinimumNonZeroWithLiquidationAbsentIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(5000))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateMinimum and CoverRateLiquidation must both be zero or both be non-zero.");
  }

  @Test
  void coverRateLiquidationNonZeroWithMinimumAbsentIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .coverRateLiquidation(UnsignedInteger.valueOf(5000))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateMinimum and CoverRateLiquidation must both be zero or both be non-zero.");
  }

  @Test
  void bothCoverRatesNonZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .coverRateMinimum(UnsignedInteger.valueOf(5000))
      .coverRateLiquidation(UnsignedInteger.valueOf(3000))
      .build()
    );
  }

  @Test
  void bothCoverRatesAbsentIsValid() {
    assertDoesNotThrow(() -> baseBuilder().build());
  }

  // //////////////////////
  // Fixed field modification checks (update case)
  // //////////////////////

  @Test
  void updateCannotModifyManagementFeeRate() {
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .managementFeeRate(UnsignedInteger.valueOf(100))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("ManagementFeeRate cannot be modified when updating an existing LoanBroker.");
  }

  @Test
  void updateCannotModifyCoverRateMinimum() {
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .coverRateMinimum(UnsignedInteger.valueOf(100))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateMinimum cannot be modified when updating an existing LoanBroker.");
  }

  @Test
  void updateCannotModifyCoverRateLiquidation() {
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .coverRateLiquidation(UnsignedInteger.valueOf(100))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CoverRateLiquidation cannot be modified when updating an existing LoanBroker.");
  }

  @Test
  void updateWithOnlyDataAndDebtMaximumIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .data(LoanBrokerData.of("AABBCC"))
      .debtMaximum(Amount.of("1000000"))
      .build()
    );
  }

  // //////////////////////
  // Happy path
  // //////////////////////

  @Test
  void validCreateWithAllFields() {
    assertDoesNotThrow(() -> baseBuilder()
      .managementFeeRate(UnsignedInteger.valueOf(5000))
      .debtMaximum(Amount.of("1000000000"))
      .coverRateMinimum(UnsignedInteger.valueOf(50000))
      .coverRateLiquidation(UnsignedInteger.valueOf(25000))
      .data(LoanBrokerData.of("AABBCC"))
      .build()
    );
  }

  @Test
  void validMinimalCreate() {
    assertDoesNotThrow(() -> baseBuilder().build());
  }

  /**
   * Returns a builder pre-populated with required fields for LoanBrokerSet.
   */
  private ImmutableLoanBrokerSet.Builder baseBuilder() {
    return LoanBrokerSet.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(VALID_VAULT_ID)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC"
        )
      );
  }
}
