package org.xrpl.xrpl4j.model.client.common;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;

import java.util.Objects;

/**
 * Represents a ledger index, which can either be an integer or a shortcut {@link String}.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
 */
public class LedgerIndex {

  /**
   * Constant shortcut value to request a rippled server's current working version of the ledger.
   *
   * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
   * @deprecated Ledger index shortcut values should now be specified using {@link LedgerIndexShortcut}.
   */
  @Deprecated
  public static final LedgerIndex CURRENT = LedgerIndex.of("current");

  /**
   * Constant shortcut value to request the most recent ledger that has been validated by consensus.
   *
   * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
   * @deprecated Ledger index shortcut values should now be specified using {@link LedgerIndexShortcut}.
   */
  @Deprecated
  public static final LedgerIndex VALIDATED = LedgerIndex.of("validated");

  /**
   * Constant shortcut value to request a the most recent ledger that has been closed for modifications
   * and proposed for validation.
   *
   * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
   * @deprecated Ledger index shortcut values should now be specified using {@link LedgerIndexShortcut}.
   */
  @Deprecated
  public static final LedgerIndex CLOSED = LedgerIndex.of("closed");

  private final String value;

  /**
   * Public constructor.
   *
   * @param value The ledger index value as a {@link String}.
   *
   * @deprecated Does not check if the given value is a valid index.
   *   This constructor should be made private in the future.
   *   Only the {@link #of(String value)} and {@link #of(UnsignedLong value)}
   *   factory methods should be used to construct {@link LedgerIndex} objects.
   */
  @Deprecated
  public LedgerIndex(String value) {
    this.value = value;
  }

  /**
   * Construct a {@link LedgerIndex} for a {@link String} value.
   *
   * @param value A {@link String} containing either an integer or a shortcut.
   *
   * @return A {@link LedgerIndex} with the given value.
   * @throws NullPointerException  if value is null
   * @throws NumberFormatException if value is an invalid index
   * @deprecated Ledger index shortcuts will not be specified using this class in the future. Instead,
   *   use {@link LedgerIndexShortcut} to specify shortcut values,
   *   and use the {@link UnsignedLong} static constructor for numerical {@link LedgerIndex}es.
   */
  @Deprecated
  public static LedgerIndex of(String value)
    throws NumberFormatException {
    Objects.requireNonNull(value);
    if (isValidShortcut(value)) {
      return new LedgerIndex(value);
    } else {
      UnsignedLong.valueOf(value);
      return new LedgerIndex(value);
    }
  }

  /**
   * Construct a {@link LedgerIndex} from an {@link UnsignedLong}.
   *
   * @param value An {@link UnsignedLong} specifying a ledger index.
   *
   * @return A {@link LedgerIndex} with the given value as a {@link String}.
   * @throws NullPointerException if value is null
   */
  public static LedgerIndex of(UnsignedLong value) {
    Objects.requireNonNull(value);
    return new LedgerIndex(value.toString());
  }

  /**
   * Checks to see if a given value is a valid ledger index shortcut.
   *
   * @param value A {@link String} containing the value to check.
   *
   * @return {@code true} if the value is a valid ledger index shortcut, otherwise {@code false}.
   */
  private static boolean isValidShortcut(String value) {
    if (value.equals("current")) {
      return true;
    }
    if (value.equals("validated")) {
      return true;
    }
    if (value.equals("closed")) {
      return true;
    }
    return false;
  }

  /**
   * Get the value of this {@link LedgerIndex} as a {@link String}.
   *
   * @return The underlying {@code value} of this {@link LedgerIndex}.
   * @deprecated In the future, the underlying value of a {@link LedgerIndex} will be an {@link UnsignedLong}.
   *   The {@code value()} method will therefore return an {@link UnsignedLong} in the future.
   */
  @Deprecated
  public String value() {
    return value;
  }

  /**
   * Get this {@link LedgerIndex} as an {@link UnsignedLong}.
   *
   * @return The {@link UnsignedLong} representation of this {@link LedgerIndex}.
   */
  public UnsignedLong unsignedLongValue() {
    return UnsignedLong.valueOf(value);
  }

  /**
   * Add an {@link UnsignedLong} to this {@link LedgerIndex}.
   *
   * @param other The {@link UnsignedLong} to add to this {@link LedgerIndex}.
   *
   * @return The sum of the {@link UnsignedLong} and this {@link LedgerIndex}'s {@link UnsignedLong} value.
   */
  public LedgerIndex plus(UnsignedLong other) {
    checkAdditionOverflow(other);
    return LedgerIndex.of(unsignedLongValue().plus(other));
  }

  /**
   * Add another {@link LedgerIndex} to this {@link LedgerIndex}.
   *
   * @param other The {@link LedgerIndex} to add to this {@link LedgerIndex}.
   *
   * @return The sum of the {@link LedgerIndex}' and this {@link LedgerIndex}'s {@link UnsignedLong} value.
   */
  public LedgerIndex plus(LedgerIndex other) {
    return plus(other.unsignedLongValue());
  }

  /**
   * Subtract a {@link LedgerIndex} from this {@link LedgerIndex}.
   *
   * @param other Another {@link LedgerIndex} to subtract.
   *
   * @return A {@link LedgerIndex} wrapping the difference of the two wrapped {@link UnsignedLong} values of
   *   this {@link LedgerIndex} and {@code other}.
   */
  public LedgerIndex minus(LedgerIndex other) {
    return minus(other.unsignedLongValue());
  }

  /**
   * Subtract an {@link UnsignedLong} from this {@link LedgerIndex}.
   *
   * @param value An {@link UnsignedLong} to subtract.
   *
   * @return A {@link LedgerIndex} wrapping the difference of this {@link LedgerIndex}'s value and {@code value}.
   */
  public LedgerIndex minus(UnsignedLong value) {
    checkSubtractionOverflow(value);
    return LedgerIndex.of(this.unsignedLongValue().minus(value));
  }

  private void checkAdditionOverflow(UnsignedLong addedValue) {
    Preconditions.checkArgument(
      FluentCompareTo.is(UnsignedLong.MAX_VALUE.minus(addedValue)).greaterThanEqualTo(this.unsignedLongValue()),
      String.format("Value too large. Adding %s would cause an overflow.", addedValue)
    );
  }

  private void checkSubtractionOverflow(UnsignedLong subtractedValue) {
    Preconditions.checkArgument(
      FluentCompareTo.is(subtractedValue).lessThanOrEqualTo(this.unsignedLongValue()),
      String.format("Value too large. Subtracting %s would cause an overflow.", subtractedValue)
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LedgerIndex)) {
      return false;
    }
    LedgerIndex that = (LedgerIndex) obj;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return this.value();
  }
}
