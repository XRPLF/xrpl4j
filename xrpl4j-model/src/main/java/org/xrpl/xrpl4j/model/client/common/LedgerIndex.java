package org.xrpl.xrpl4j.model.client.common;

import com.google.common.primitives.UnsignedLong;

import java.util.Objects;

/**
 * Represents a ledger index, which can either be an integer or a shortcut {@link String}.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
 */
public class LedgerIndex {

  /**
   * Constant shortcut values for specifying a ledger index.
   */
  public static final LedgerIndex CURRENT = LedgerIndex.of("current");
  public static final LedgerIndex VALIDATED = LedgerIndex.of("validated");
  public static final LedgerIndex CLOSED = LedgerIndex.of("closed");
  private final String value;

  /**
   * Public constructor.
   *
   * @param value The ledger index value as a {@link String}.
   *
   * @deprecated Does not check if the given value is a valid index.
   *    This constructor should be made private in the future.
   *    Only the {@link #of(String value)} and {@link #of(UnsignedLong value)} 
   *    factory methods should be used to construct {@link LedgerIndex} objects.
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
   *
   * @throws NullPointerException if value is null
   *
   * @throws NumberFormatException if value is an invalid index
   */
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
   *
   * @throws NullPointerException if value is null
   */
  public static LedgerIndex of(UnsignedLong value) {
    Objects.requireNonNull(value);
    return new LedgerIndex(value.toString());
  }

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

  public static boolean isValidShortcut(String value) {
    if (value.equals("current")) return true;
    if (value.equals("validated")) return true;
    if (value.equals("closed")) return true;
    return false;
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
