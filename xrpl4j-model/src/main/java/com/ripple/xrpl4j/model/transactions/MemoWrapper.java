package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Wrapper object for a {@link Memo}, so that the JSON representation of a list of {@link Memo}s is correct, according
 * to the XRPL binary serialization specification.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMemoWrapper.class)
@JsonDeserialize(as = ImmutableMemoWrapper.class)
public interface MemoWrapper {

  static ImmutableMemoWrapper.Builder builder() {
    return ImmutableMemoWrapper.builder();
  }

  /**
   * A {@link Memo} containing arbitrary information.
   */
  @JsonProperty("Memo")
  Memo memo();

}
