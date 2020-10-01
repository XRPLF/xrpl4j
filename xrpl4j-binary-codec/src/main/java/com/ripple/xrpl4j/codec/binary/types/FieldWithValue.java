package com.ripple.xrpl4j.codec.binary.types;

import com.ripple.xrpl4j.codec.binary.enums.FieldInstance;
import org.immutables.value.Value.Immutable;

@Immutable
public interface FieldWithValue<T> extends Comparable {

  static ImmutableFieldWithValue.Builder builder() {
    return ImmutableFieldWithValue.builder();
  }

  FieldInstance field();

  T value();

  @Override
  default int compareTo(Object o) {
    if (!(o instanceof FieldWithValue)) {
      throw new IllegalArgumentException("cannot compare to type " + o.getClass());
    }
    FieldWithValue other = (FieldWithValue) o;
    return this.field().ordinal() - other.field().ordinal();
  }
}
