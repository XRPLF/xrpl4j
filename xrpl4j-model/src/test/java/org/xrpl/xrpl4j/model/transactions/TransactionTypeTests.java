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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

/**
 * Unit tests for {@link TransactionType}.
 */
public class TransactionTypeTests {

  @ParameterizedTest
  @ArgumentsSource(value = TransactionTypeValidArgumentProvider.class)
  public void shouldReturnTransactionTypeForValidValues(String value) {
    TransactionType transactionType = TransactionType.forValue(value);
    assertNotNull(transactionType);
    assertTrue(transactionType instanceof TransactionType);
  }

  @EmptySource
  @NullSource
  @ParameterizedTest
  @ArgumentsSource(value = TransactionTypeInvalidArgumentProvider.class)
  public void shouldThrowIllegalArgumentExceptionForInvalidValues(String value) {
    assertThrows(IllegalArgumentException.class, () -> TransactionType.forValue(value),
      "No matching TransactionType enum value for String value " + value);
  }

  public static class TransactionTypeValidArgumentProvider implements ArgumentsProvider {

    @Override
    public java.util.stream.Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return java.util.stream.Stream.of(TransactionType.values()).map(TransactionType::value).map(Arguments::of);
    }

  }

  public static class TransactionTypeInvalidArgumentProvider implements ArgumentsProvider {

    @Override
    public java.util.stream.Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return java.util.stream.Stream.of("bla", "blaaa", "123").map(Arguments::of);
    }

  }
}
