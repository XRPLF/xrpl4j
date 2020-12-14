package org.xrpl.xrpl4j.codec.binary.types;

import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInstance;

@Immutable
public interface FieldWithValue<T> extends Comparable<T> {

  static <T> ImmutableFieldWithValue.Builder<T> builder() {
    return ImmutableFieldWithValue.builder();
  }

  FieldInstance field();

  T value();

  @Override
  @SuppressWarnings( {"NullableProblems", "rawtypes"})
  default int compareTo(Object object) {
    if (!(object instanceof FieldWithValue)) {
      throw new IllegalArgumentException("cannot compare to type " + object.getClass());
    }
    FieldWithValue<?> other = (FieldWithValue) object;
    return this.field().ordinal() - other.field().ordinal();
  }
}
