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
 * Unit tests for {@link LoanBrokerCoverWithdraw} validation logic.
 */
class LoanBrokerCoverWithdrawTest {

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
    assertThatThrownBy(() -> baseBuilder()
      .loanBrokerId(ZERO_HASH)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must not be zero.");
  }

  // //////////////////////
  // Amount checks - XRP
  // //////////////////////

  @Test
  void amountXrpZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(XrpCurrencyAmount.ofDrops(0))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must be greater than zero.");
  }

  @Test
  void amountXrpPositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .build()
    );
  }

  // //////////////////////
  // Amount checks - IOU
  // //////////////////////

  @Test
  void amountIouZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .value("0")
        .build()
      )
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must be greater than zero.");
  }

  @Test
  void amountIouNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .value("-500")
        .build()
      )
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must be greater than zero.");
  }

  @Test
  void amountIouPositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .value("500")
        .build()
      )
      .build()
    );
  }

  // //////////////////////
  // Amount checks - MPT
  // //////////////////////

  @Test
  void amountMptZeroIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("00000001A407AF5856CFF3379A4C6B547331AB3F86DACA08B6"))
        .value("0")
        .build()
      )
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must be greater than zero.");
  }

  @Test
  void amountMptNegativeIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .amount(MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("00000001A407AF5856CFF3379A4C6B547331AB3F86DACA08B6"))
        .value("-500")
        .build()
      )
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Amount must be greater than zero.");
  }

  @Test
  void amountMptPositiveIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(MptCurrencyAmount.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("00000001A407AF5856CFF3379A4C6B547331AB3F86DACA08B6"))
        .value("500")
        .build()
      )
      .build()
    );
  }

  // //////////////////////
  // Destination checks
  // //////////////////////

  @Test
  void destinationZeroAccountIsInvalid() {
    assertThatThrownBy(() -> baseBuilder()
      .destination(AddressConstants.ACCOUNT_ZERO)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Destination must not be the zero account.");
  }

  @Test
  void destinationValidAccountIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .destination(Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE"))
      .build()
    );
  }

  @Test
  void destinationAbsentIsValid() {
    assertDoesNotThrow(() -> baseBuilder().build());
  }

  @Test
  void destinationWithTagIsValid() {
    assertDoesNotThrow(() -> baseBuilder()
      .destination(Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE"))
      .destinationTag(UnsignedInteger.valueOf(12345))
      .build()
    );
  }

  // //////////////////////
  // Happy path
  // //////////////////////

  @Test
  void validWithXrpAmount() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .build()
    );
  }

  @Test
  void validWithIouAmountAndDestination() {
    assertDoesNotThrow(() -> baseBuilder()
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .value("50000")
        .build()
      )
      .destination(Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE"))
      .destinationTag(UnsignedInteger.valueOf(42))
      .build()
    );
  }

  /**
   * Returns a builder pre-populated with required fields for LoanBrokerCoverWithdraw.
   */
  private ImmutableLoanBrokerCoverWithdraw.Builder baseBuilder() {
    return LoanBrokerCoverWithdraw.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .loanBrokerId(VALID_BROKER_ID)
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC"
        )
      );
  }
}
