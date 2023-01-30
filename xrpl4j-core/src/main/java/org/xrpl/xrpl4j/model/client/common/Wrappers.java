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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;
import org.xrpl.xrpl4j.model.jackson.modules.LedgerIndexBoundDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.LedgerIndexBoundSerializer;

import java.io.Serializable;

/**
 * Definitions for immutable wrappers.
 */
@SuppressWarnings("TypeName")
public class Wrappers {

  /**
   * This class is similar to {@link LedgerIndex} in that it represents a numerical ledger version on the XRP Ledger.
   * However, {@link LedgerIndexBound} can be used to specify ranges of ledgers in some rippled API request objects,
   * such as {@link org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams}. Unlike {@link LedgerIndex}
   * which wraps an unsigned long value, this class supports {@code -1} and does not allow {@code 0}.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = LedgerIndexBound.class, using = LedgerIndexBoundSerializer.class)
  @JsonDeserialize(as = LedgerIndexBound.class, using = LedgerIndexBoundDeserializer.class)
  abstract static class _LedgerIndexBound extends Wrapper<Long> implements Serializable {

    /**
     * Static constructor.
     *
     * @param ledgerIndexBound An integer representing the ledger-index bound.
     *
     * @return A {@link LedgerIndexBound}.
     */
    public static LedgerIndexBound of(int ledgerIndexBound) {
      return LedgerIndexBound.of(Integer.valueOf(ledgerIndexBound).longValue());
    }

    public static LedgerIndexBound unbounded() {
      return LedgerIndexBound.of(-1);
    }
    
    @Override
    public String toString() {
      return value().toString();
    }

    /**
     * Add a {@link LedgerIndexBound} to this {@link LedgerIndexBound}.
     *
     * @param other Another {@link LedgerIndexBound} to add.
     *
     * @return A {@link LedgerIndexBound} wrapping the sum of the two wrapped {@link Long} values of this
     *   {@link LedgerIndexBound} and {@code other}.
     */
    public LedgerIndexBound plus(LedgerIndexBound other) {
      checkAdditionOverflow(other.value());
      return LedgerIndexBound.of(this.value() + other.value());
    }

    /**
     * Add a {@link LedgerIndex} to this {@link LedgerIndexBound}.
     *
     * @param ledgerIndex A {@link LedgerIndex} to add to this {@link LedgerIndexBound}.
     *
     * @return A {@link LedgerIndexBound} wrapping the sum of the two values of this {@link LedgerIndexBound} and
     *   {@code ledgerIndex}.
     */
    public LedgerIndexBound plus(LedgerIndex ledgerIndex) {
      checkAdditionOverflow(ledgerIndex.unsignedIntegerValue().longValue());
      return LedgerIndexBound.of(this.value() + ledgerIndex.unsignedIntegerValue().longValue());
    }

    /**
     * Add a {@link Long} to this {@link LedgerIndexBound}.
     *
     * @param value A {@link Long} to add.
     *
     * @return A {@link LedgerIndexBound} wrapping the sum of this {@link LedgerIndexBound}'s value and {@code value}.
     */
    public LedgerIndexBound plus(Long value) {
      checkAdditionOverflow(value);
      return LedgerIndexBound.of(this.value() + value);
    }

    /**
     * Add an {@link Integer} to this {@link LedgerIndexBound}.
     *
     * @param value An {@link Integer} to add.
     *
     * @return A {@link LedgerIndexBound} wrapping the sum of this {@link LedgerIndexBound}'s value and {@code value}.
     */
    public LedgerIndexBound plus(Integer value) {
      return plus(value.longValue());
    }

    /**
     * Subtract a {@link LedgerIndexBound} from this {@link LedgerIndexBound}.
     *
     * @param other Another {@link LedgerIndexBound} to subtract.
     *
     * @return A {@link LedgerIndexBound} wrapping the difference of the two wrapped {@link Long} values of this
     *   {@link LedgerIndexBound} and {@code other}.
     */
    public LedgerIndexBound minus(LedgerIndexBound other) {
      checkSubtractionInBounds(other.value());
      return LedgerIndexBound.of(this.value() - other.value());
    }

    /**
     * Subtract a {@link LedgerIndex} from this {@link LedgerIndexBound}.
     *
     * @param ledgerIndex A {@link LedgerIndex} to subtract.
     *
     * @return A {@link LedgerIndexBound} wrapping the difference of the two values of this {@link LedgerIndexBound} and
     *   {@code ledgerIndex}.
     */
    public LedgerIndexBound minus(LedgerIndex ledgerIndex) {
      checkSubtractionInBounds(ledgerIndex.unsignedIntegerValue().longValue());
      return LedgerIndexBound.of(this.value() - ledgerIndex.unsignedIntegerValue().longValue());
    }

    /**
     * Subtract a {@link Long} from this {@link LedgerIndexBound}.
     *
     * @param value A {@link Long} to subtract.
     *
     * @return A {@link LedgerIndexBound} wrapping the difference of this {@link LedgerIndexBound}'s value and
     *   {@code value}.
     */
    public LedgerIndexBound minus(Long value) {
      checkSubtractionInBounds(value);
      return LedgerIndexBound.of(this.value() - value);
    }

    /**
     * Subtract an {@link Integer} from this {@link LedgerIndexBound}.
     *
     * @param value An {@link Integer} to subtract.
     *
     * @return A {@link LedgerIndexBound} wrapping the difference of this {@link LedgerIndexBound}'s value and
     *   {@code value}.
     */
    public LedgerIndexBound minus(Integer value) {
      return minus(value.longValue());
    }

    /**
     * Ensures that this {@link LedgerIndexBound} is not less than {@code -1} and not equal to {@code 0}.
     */
    @Value.Check
    public void checkBounds() {
      Preconditions.checkArgument(value() >= -1, "LedgerIndexBounds must be greater than or equal to -1.");
      Preconditions.checkArgument(
        value() <= UnsignedInteger.MAX_VALUE.longValue(),
        "LedgerIndexBounds cannot be larger than max unsigned integer value " + UnsignedInteger.MAX_VALUE.toString()
      );
    }

    @Value.Auxiliary
    private void checkAdditionOverflow(Long addedValue) {
      Preconditions.checkArgument(
        FluentCompareTo.is(Long.MAX_VALUE - addedValue).greaterThanEqualTo(this.value()),
        String.format("Value too large. Adding %s would cause an overflow.", addedValue)
      );
    }

    @Value.Auxiliary
    private void checkSubtractionInBounds(Long subtractedValue) {
      Preconditions.checkArgument(
        FluentCompareTo.is(subtractedValue).lessThanOrEqualTo(this.value() - 1),
        String.format("Value too large. Subtracting %s would result in a LedgerIndexBound below 1.", subtractedValue)
      );
    }
  }


}
