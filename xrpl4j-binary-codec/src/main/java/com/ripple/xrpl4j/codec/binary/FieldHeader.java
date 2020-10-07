package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Represents a Field ID used to encode an XRPL field name to its binary form.
 */
@Immutable
@JsonSerialize(as = ImmutableFieldHeader.class)
@JsonDeserialize(as = ImmutableFieldHeader.class)
public interface FieldHeader {

  static ImmutableFieldHeader.Builder builder() {
    return ImmutableFieldHeader.builder();
  }

  int fieldCode();

  int typeCode();

}
