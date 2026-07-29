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
 * Unit tests for {@link LoanBrokerDelete} validation logic.
 */
class LoanBrokerDeleteTest {

  private static final Hash256 VALID_BROKER_ID = Hash256.of(
    "C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"
  );
  private static final Hash256 ZERO_HASH = Hash256.of(
    "0000000000000000000000000000000000000000000000000000000000000000"
  );

  @Test
  void loanBrokerIdMustNotBeZero() {
    assertThatThrownBy(() -> baseBuilder().loanBrokerId(ZERO_HASH).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerID must not be zero.");
  }

  @Test
  void validLoanBrokerDelete() {
    assertDoesNotThrow(() -> baseBuilder().build());
  }

  private ImmutableLoanBrokerDelete.Builder baseBuilder() {
    return LoanBrokerDelete.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .loanBrokerId(VALID_BROKER_ID)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC"
        )
      );
  }
}
