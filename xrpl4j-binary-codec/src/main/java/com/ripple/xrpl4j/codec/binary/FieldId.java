package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Represents a Field ID used to encode an XRPL field name to its binary form.
 */
@Immutable
@JsonSerialize(as = ImmutableFieldId.class)
@JsonDeserialize(as = ImmutableFieldId.class)
public interface FieldId {

  static ImmutableFieldId.Builder builder() {
    return ImmutableFieldId.builder();
  }

  int fieldCode();

  int typeCode();

}
