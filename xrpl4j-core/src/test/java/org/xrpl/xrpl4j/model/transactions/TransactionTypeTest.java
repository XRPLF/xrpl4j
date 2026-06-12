package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransactionType}.
 */
public class TransactionTypeTest {

  @Test
  void forValueExactCase() {
    assertThat(TransactionType.forValue("Payment")).isEqualTo(TransactionType.PAYMENT);
    assertThat(TransactionType.forValue("AccountSet")).isEqualTo(TransactionType.ACCOUNT_SET);
    assertThat(TransactionType.forValue("TrustSet")).isEqualTo(TransactionType.TRUST_SET);
    assertThat(TransactionType.forValue("DelegateSet")).isEqualTo(TransactionType.DELEGATE_SET);
  }

  @Test
  void forValueWrongCase() {
    assertThat(TransactionType.forValue("payment")).isEqualTo(TransactionType.PAYMENT);
    assertThat(TransactionType.forValue("PAYMENT")).isEqualTo(TransactionType.PAYMENT);
    assertThat(TransactionType.forValue("accountset")).isEqualTo(TransactionType.ACCOUNT_SET);
    assertThat(TransactionType.forValue("TRUSTSET")).isEqualTo(TransactionType.TRUST_SET);
    assertThat(TransactionType.forValue("delegateset")).isEqualTo(TransactionType.DELEGATE_SET);
  }

  @Test
  void forValueUnknown() {
    assertThat(TransactionType.forValue("NotARealTransaction")).isEqualTo(TransactionType.UNKNOWN);
    assertThat(TransactionType.forValue("")).isEqualTo(TransactionType.UNKNOWN);
    assertThat(TransactionType.forValue("abc")).isEqualTo(TransactionType.UNKNOWN);
  }
}
