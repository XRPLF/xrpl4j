package org.xrpl.xrpl4j.model.transactions;

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