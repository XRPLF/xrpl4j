package org.xrpl.xrpl4j.model.flags;

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

import org.junit.jupiter.api.Test;

class TransactionFlagsTest {

  @Test
  void testFlags() {
    TransactionFlags flags = new TransactionFlags.Builder().build();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.isEmpty()).isTrue();
  }

  @Test
  void testOfWithValue() {
    TransactionFlags flags = TransactionFlags.of(0x80000000L);
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x80000000L);
  }

  @Test
  void testOfWithInnerBatchTxnValue() {
    TransactionFlags flags = TransactionFlags.of(0x40000000L);
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x40000000L);
  }

  @Test
  void testOfWithCombinedValue() {
    TransactionFlags flags = TransactionFlags.of(0x80000000L | 0x40000000L);
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0xC0000000L);
  }

  @Test
  void testOfWithZeroValue() {
    TransactionFlags flags = TransactionFlags.of(0L);
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testTfInnerBatchTxn() {
    TransactionFlags flags = TransactionFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x40000000L);
  }
}
