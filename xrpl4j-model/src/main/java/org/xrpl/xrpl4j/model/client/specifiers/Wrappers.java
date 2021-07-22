package org.xrpl.xrpl4j.model.client.specifiers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;

public class Wrappers {

  /**
   * Represents a numerical XRPL ledger version by wrapping an {@link UnsignedLong}.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = LedgerIndex.class)
  @JsonDeserialize(as = LedgerIndex.class)
  abstract static class _LedgerIndex extends Wrapper<UnsignedLong> implements Serializable {

    @Override
    public String toString() {
      return value().toString();
    }

    /**
     * Add a {@link LedgerIndex} to this {@link LedgerIndex}.
     *
     * @param other Another {@link LedgerIndex} to add.
     *
     * @return A {@link LedgerIndex} wrapping the sum of the two wrapped {@link UnsignedLong} values of
     *   this {@link LedgerIndex} and {@code other}.
     */
    public LedgerIndex plus(LedgerIndex other) {
      checkAdditionOverflow(other.value());
      return LedgerIndex.of(this.value().plus(other.value()));
    }

    /**
     * Add an {@link UnsignedLong} to this {@link LedgerIndex}.
     *
     * @param value An {@link UnsignedLong} to add.
     *
     * @return A {@link LedgerIndex} wrapping the sum of this {@link LedgerIndex}'s value and {@code value}.
     */
    public LedgerIndex plus(UnsignedLong value) {
      checkAdditionOverflow(value);
      return LedgerIndex.of(this.value().plus(value));
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
      checkSubtractionOverflow(other.value());
      return LedgerIndex.of(this.value().minus(other.value()));
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
      return LedgerIndex.of(this.value().minus(value));
    }

    @Value.Auxiliary
    private void checkAdditionOverflow(UnsignedLong addedValue) {
      Preconditions.checkArgument(
        FluentCompareTo.is(UnsignedLong.MAX_VALUE.minus(addedValue)).greaterThanEqualTo(this.value()),
        String.format("Value too large. Adding %s would cause an overflow.", addedValue)
      );
    }

    @Value.Auxiliary
    private void checkSubtractionOverflow(UnsignedLong subtractedValue) {
      Preconditions.checkArgument(
        FluentCompareTo.is(subtractedValue).lessThanOrEqualTo(this.value()),
        String.format("Value too large. Subtracting %s would cause an overflow.", subtractedValue)
      );
    }
  }

  /**
   * This class is similar to {@link LedgerIndex} in that it represents a numerical ledger version on the XRP Ledger.
   * However, {@link LedgerIndexBound} can be used to specify ranges of ledgers in some rippled API request objects,
   * such as {@link org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams}. Unlike {@link LedgerIndex}
   * which wraps an unsigned long value, this class supports {@code -1} and does not allow {@code 0}.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = LedgerIndexBound.class)
  @JsonDeserialize(as = LedgerIndexBound.class)
  abstract static class _LedgerIndexBound extends Wrapper<Long> implements Serializable {

    public static LedgerIndexBound of(int ledgerIndexBound) {
      return LedgerIndexBound.of(Integer.valueOf(ledgerIndexBound).longValue());
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
     * @return A {@link LedgerIndexBound} wrapping the sum of the two wrapped {@link Long} values of
     *   this {@link LedgerIndexBound} and {@code other}.
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
      checkAdditionOverflow(ledgerIndex.value().longValue());
      return LedgerIndexBound.of(this.value() + ledgerIndex.value().longValue());
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
     * @return A {@link LedgerIndexBound} wrapping the difference of the two wrapped {@link Long} values of
     *   this {@link LedgerIndexBound} and {@code other}.
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
      checkSubtractionInBounds(ledgerIndex.value().longValue());
      return LedgerIndexBound.of(this.value() - ledgerIndex.value().longValue());
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
      Preconditions.checkArgument(value() != 0, "LedgerIndexBounds cannot be 0.");
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
