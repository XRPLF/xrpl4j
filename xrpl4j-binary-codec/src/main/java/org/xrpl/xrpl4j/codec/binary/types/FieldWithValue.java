package org.xrpl.xrpl4j.codec.binary.types;

import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInstance;

@Immutable
public interface FieldWithValue<T> extends Comparable<FieldWithValue<T>> {

  static <T> ImmutableFieldWithValue.Builder<T> builder() {
    return ImmutableFieldWithValue.builder();
  }

  FieldInstance field();

  T value();

  @Override
  @SuppressWarnings( {"NullableProblems", "rawtypes"})
  default int compareTo(FieldWithValue<T> other) {
    return this.field().compareTo(other.field());
  }
}
