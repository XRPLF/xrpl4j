package org.xrpl.xrpl4j.model.transactions;

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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.BatchFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link Batch}.
 */
public class BatchTest {

  private static final Address ACCOUNT = Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH");
  private static final Address DESTINATION = Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy");

  @Test
  void testBatchWithAllOrNothingMode() {
    Batch batch = createValidBatch(BatchFlags.ALL_OR_NOTHING);
    assertThat(batch.flags().tfAllOrNothing()).isTrue();
    assertThat(batch.rawTransactions()).hasSize(2);
  }

  @Test
  void testBatchWithOnlyOneMode() {
    Batch batch = createValidBatch(BatchFlags.ONLY_ONE);
    assertThat(batch.flags().tfOnlyOne()).isTrue();
  }

  @Test
  void testBatchWithUntilFailureMode() {
    Batch batch = createValidBatch(BatchFlags.UNTIL_FAILURE);
    assertThat(batch.flags().tfUntilFailure()).isTrue();
  }

  @Test
  void testBatchWithIndependentMode() {
    Batch batch = createValidBatch(BatchFlags.INDEPENDENT);
    assertThat(batch.flags().tfIndependent()).isTrue();
  }

  @Test
  void testBatchWithTooFewTransactions() {
    List<RawTransactionWrapper> oneTransaction = createInnerTransactions(1);

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(oneTransaction)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RawTransactions must contain between 2 and 8 transactions");
  }

  @Test
  void testBatchWithTooManyTransactions() {
    List<RawTransactionWrapper> nineTransactions = createInnerTransactions(9);

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(nineTransactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RawTransactions must contain between 2 and 8 transactions");
  }

  @Test
  void testBatchWithNoModeFlag() {
    List<RawTransactionWrapper> transactions = createInnerTransactions(2);

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.of(0L))
      .rawTransactions(transactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Exactly one batch mode flag must be set");
  }

  @Test
  void testBatchWithMultipleModeFlags() {
    List<RawTransactionWrapper> transactions = createInnerTransactions(2);
    // Combine ALL_OR_NOTHING and ONLY_ONE flags
    long combinedFlags = BatchFlags.ALL_OR_NOTHING.getValue() | BatchFlags.ONLY_ONE.getValue();

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.of(combinedFlags))
      .rawTransactions(transactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Exactly one batch mode flag must be set");
  }

  @Test
  void testBatchWithNestedBatch() {
    // Create an inner Batch transaction (which should be rejected)
    Batch innerBatch = createValidBatch(BatchFlags.ALL_OR_NOTHING);
    RawTransactionWrapper nestedBatchWrapper = RawTransactionWrapper.of(innerBatch);

    List<RawTransactionWrapper> transactions = new ArrayList<>();
    transactions.add(nestedBatchWrapper);
    transactions.add(createInnerTransactions(1).get(0));

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Batch transactions cannot be nested");
  }

  @Test
  void testBatchWithMaxTransactions() {
    List<RawTransactionWrapper> eightTransactions = createInnerTransactions(8);
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(eightTransactions)
      .build();

    assertThat(batch.rawTransactions()).hasSize(8);
  }

  private Batch createValidBatch(BatchFlags flags) {
    return Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(flags)
      .rawTransactions(createInnerTransactions(2))
      .build();
  }

  private List<RawTransactionWrapper> createInnerTransactions(int count) {
    return IntStream.range(0, count)
      .mapToObj(i -> RawTransactionWrapper.of(
        Payment.builder()
          .account(ACCOUNT)
          .destination(DESTINATION)
          .fee(XrpCurrencyAmount.ofDrops(0))
          .sequence(UnsignedInteger.valueOf(i + 1))
          .amount(XrpCurrencyAmount.ofDrops(1000))
          .build()
      ))
      .collect(Collectors.toList());
  }
}

