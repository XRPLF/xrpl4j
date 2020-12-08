package org.xrpl.xrpl4j.codec.binary.types;

import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInstance;

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
