package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

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
