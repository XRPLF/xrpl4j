package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2025 XRPL Foundation and its contributors
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link UnknownPermission}.
 */
class UnknownPermissionTest {

  @Test
  void acceptsGenuinelyUnknownValue() {
    UnknownPermission permission = UnknownPermission.of("SomeFutureTransactionType");
    assertThat(permission.value()).isEqualTo("SomeFutureTransactionType");
  }

  @Test
  void acceptsEmptyValue() {
    assertThat(UnknownPermission.of("").value()).isEmpty();
  }

  @Test
  void acceptsTransactionTypeUnknownSentinelValue() {
    // "Unknown" is the string value of TransactionType.UNKNOWN, which is a sentinel rather than a real type.
    assertThat(UnknownPermission.of(TransactionType.UNKNOWN.value()).value()).isEqualTo("Unknown");
  }

  @ParameterizedTest
  @ValueSource(strings = {"AccountSet", "accountset", "ACCOUNTSET", "Payment", "payment"})
  void rejectsKnownTransactionTypeName(String value) {
    assertThatThrownBy(() -> UnknownPermission.of(value))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(
        "UnknownPermission: value %s is a known TransactionType; use TransactionTypePermission instead", value
      );
  }

  @ParameterizedTest
  @ValueSource(strings = {"TrustlineAuthorize", "trustlineauthorize", "PaymentMint", "PAYMENTMINT"})
  void rejectsKnownGranularPermissionName(String value) {
    assertThatThrownBy(() -> UnknownPermission.of(value))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(
        "UnknownPermission: value %s is a known GranularPermission; use GranularPermissionValue instead", value
      );
  }
}
