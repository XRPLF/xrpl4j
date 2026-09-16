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
import org.xrpl.xrpl4j.model.AddressConstants;

/**
 * Unit tests for {@link LoanBrokerCoverClawback} validation logic.
 */
class LoanBrokerCoverClawbackTest {

  private static final Address ACCOUNT = Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm");
  private static final Address ISSUER = Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd");
  private static final Hash256 VALID_BROKER_ID = Hash256.of(
    "C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"
  );
  private static final Hash256 ZERO_HASH = Hash256.of(
    "0000000000000000000000000000000000000000000000000000000000000000"
  );
  private static final String MPT_ISSUANCE_ID = "00000001A407AF5856CFF3379A4C6B547331AB3F86DACA08B6";

  // //////////////////////
  // At least one of LoanBrokerID or Amount required
  // //////////////////////

  @Test
  void neitherLoanBrokerIdNorAmountIsInvalid() {
    assertThatThrownBy(() -> baseBuilder().build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("At least one of LoanBrokerID or Amount must be specified.");
  }

  @Test
  void onlyLoanBrokerIdIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .build()
    );
  }

  @Test
  void onlyAmountIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(iouAmount(ISSUER, "500"))
      .build()
    );
  }

  // //////////////////////
  // LoanBrokerID checks
  // //////////////////////

  @Test
  void loanBrokerIdMustNotBeZero() {
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(ZERO_HASH)
      .amount(iouAmount(ISSUER, "500"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must not be zero.");
  }

  // //////////////////////
  // Amount negative check
  // //////////////////////

  @Test
  void amountNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(iouAmount(ISSUER, "-500"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must not be negative.");
  }

  @Test
  void amountZeroIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(iouAmount(ISSUER, "0"))
      .build()
    );
  }

  @Test
  void amountPositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(iouAmount(ISSUER, "500"))
      .build()
    );
  }

  // //////////////////////
  // Amount must not be XRP
  // //////////////////////

  @Test
  void amountXrpIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must not be XRP.");
  }

  // //////////////////////
  // LoanBrokerID absent + MPT
  // //////////////////////

  @Test
  void loanBrokerIdAbsentWithMptIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of(MPT_ISSUANCE_ID))
        .value("500")
        .build()
      )
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must be specified when Amount is an MPT.");
  }

  @Test
  void loanBrokerIdPresentWithMptIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of(MPT_ISSUANCE_ID))
        .value("500")
        .build()
      )
      .build()
    );
  }

  // //////////////////////
  // LoanBrokerID absent + IOU issuer checks
  // //////////////////////

  @Test
  void loanBrokerIdAbsentWithIouIssuerIsSubmitterIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(iouAmount(ACCOUNT, "500"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must be specified when Amount issuer is the submitter.");
  }

  @Test
  void loanBrokerIdAbsentWithIouIssuerIsZeroAccountIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(iouAmount(AddressConstants.ACCOUNT_ZERO, "500"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must be specified when Amount issuer is the zero account.");
  }

  @Test
  void loanBrokerIdAbsentWithIouIssuerIsThirdPartyIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(iouAmount(ISSUER, "500"))
      .build()
    );
  }

  // //////////////////////
  // Happy paths
  // //////////////////////

  @Test
  void validWithBrokerIdAndIouAmount() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(iouAmount(ISSUER, "500"))
      .build()
    );
  }

  @Test
  void validWithBrokerIdOnly() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .build()
    );
  }

  @Test
  void validWithBrokerIdAndZeroAmount() {
    assertDoesNotThrow(() -> baseBuilder()
      .loanBrokerId(VALID_BROKER_ID)
      .amount(iouAmount(ISSUER, "0"))
      .build()
    );
  }

  /**
   * Returns a builder pre-populated with required common fields.
   */
  private ImmutableLoanBrokerCoverClawback.Builder baseBuilder() {
    return LoanBrokerCoverClawback.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC"
        )
      );
  }

  private IssuedCurrencyAmount iouAmount(Address issuer, String value) {
    return IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuer)
      .value(value)
      .build();
  }
}
