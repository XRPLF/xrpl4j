package com.ripple.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableFieldMetadata.class)
@JsonDeserialize(as = ImmutableFieldMetadata.class)
public interface FieldMetadata {

  int nth();

  boolean isSigningField();

  boolean isSerialized();

  String type();
  
}
