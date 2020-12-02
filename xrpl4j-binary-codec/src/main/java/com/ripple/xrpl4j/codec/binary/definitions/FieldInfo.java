package com.ripple.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Model object for Field info medata from the "fields" section of defintions.json.
 */
@Immutable
@JsonSerialize(as = ImmutableFieldInfo.class)
@JsonDeserialize(as = ImmutableFieldInfo.class)
public interface FieldInfo {

  /**
   * Sort order position for fields of the same type.
   * For example, "Fee" has a type "Amount" and has a sort order of 8th.
   *
   * @return
   */
  int nth();

  /**
   * If field is included in signed transactions.
   *
   * @return
   */
  boolean isSigningField();

  /**
   * If fiels is included in binary serialized representation.
   *
   * @return
   */
  boolean isSerialized();

  /**
   * XRPL type (e.g. UInt32, AccountID, etc.)
   *
   * @return
   */
  String type();

  @JsonProperty("isVLEncoded")
  boolean isVariableLengthEncoded();

}
