package org.xrpl.xrpl4j.model.client.specifiers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;

/**
 * A wrapper around {@link UnsignedLong} representing a specific numerical ledger index on the XRP Ledger.
 */
@Value.Immutable
@Wrapped
@JsonSerialize(as = LedgerIndex.class)
@JsonDeserialize(as = LedgerIndex.class)
public abstract class _LedgerIndex extends Wrapper<UnsignedLong> implements Serializable {

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
    return LedgerIndex.of(this.value().plus(value));
  }

}
