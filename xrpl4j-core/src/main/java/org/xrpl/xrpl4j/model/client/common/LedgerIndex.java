package org.xrpl.xrpl4j.model.client.common;

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

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;

import java.util.Objects;

/**
 * Represents a ledger index, which can either be an integer or a shortcut {@link String}.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
 */
public class LedgerIndex {

  private final UnsignedInteger value;

  /**
   * Public constructor.
   *
   * @param value The ledger index value as a {@link String}.
   */
  private LedgerIndex(UnsignedInteger value) {
    this.value = value;
  }

  /**
   * Construct a {@link LedgerIndex} from an {@link UnsignedInteger}.
   *
   * @param value An {@link UnsignedInteger} specifying a ledger index.
   *
   * @return A {@link LedgerIndex} with the given value.
   *
   * @throws NullPointerException if value is null
   */
  public static LedgerIndex of(UnsignedInteger value) {
    Objects.requireNonNull(value);
    return new LedgerIndex(value);
  }

  /**
   * Accessor for an unsigned integer value.
   *
   * @return A {@link UnsignedInteger}.
   */
  public UnsignedInteger unsignedIntegerValue() {
    return value;
  }

  /**
   * Add an {@link UnsignedInteger} to this {@link LedgerIndex}.
   *
   * @param other The {@link UnsignedInteger} to add to this {@link LedgerIndex}.
   *
   * @return The sum of the {@link UnsignedInteger} and this {@link LedgerIndex}'s {@link UnsignedInteger} value.
   */
  public LedgerIndex plus(UnsignedInteger other) {
    checkAdditionOverflow(other);
    return LedgerIndex.of(unsignedIntegerValue().plus(other));
  }

  /**
   * Add another {@link LedgerIndex} to this {@link LedgerIndex}.
   *
   * @param other The {@link LedgerIndex} to add to this {@link LedgerIndex}.
   *
   * @return The sum of the {@link LedgerIndex}' and this {@link LedgerIndex}'s {@link UnsignedLong} value.
   */
  public LedgerIndex plus(LedgerIndex other) {
    return plus(other.unsignedIntegerValue());
  }

  /**
   * Subtract a {@link LedgerIndex} from this {@link LedgerIndex}.
   *
   * @param other Another {@link LedgerIndex} to subtract.
   *
   * @return A {@link LedgerIndex} wrapping the difference of the two wrapped {@link UnsignedLong} values of this {@link
   *   LedgerIndex} and {@code other}.
   */
  public LedgerIndex minus(LedgerIndex other) {
    return minus(other.unsignedIntegerValue());
  }

  /**
   * Subtract an {@link UnsignedInteger} from this {@link LedgerIndex}.
   *
   * @param value An {@link UnsignedInteger} to subtract.
   *
   * @return A {@link LedgerIndex} wrapping the difference of this {@link LedgerIndex}'s value and {@code value}.
   */
  public LedgerIndex minus(UnsignedInteger value) {
    checkSubtractionOverflow(value);
    return LedgerIndex.of(this.unsignedIntegerValue().minus(value));
  }

  private void checkAdditionOverflow(UnsignedInteger addedValue) {
    Preconditions.checkArgument(
      FluentCompareTo.is(UnsignedInteger.MAX_VALUE.minus(addedValue)).greaterThanEqualTo(this.unsignedIntegerValue()),
      String.format("Value too large. Adding %s would cause an overflow.", addedValue)
    );
  }

  private void checkSubtractionOverflow(UnsignedInteger subtractedValue) {
    Preconditions.checkArgument(
      FluentCompareTo.is(subtractedValue).lessThanOrEqualTo(this.unsignedIntegerValue()),
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
    return this.value.toString();
  }
}
