package org.xrpl.xrpl4j.codec.binary.definitions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.codec.binary.FieldHeader;

/**
 * Holder of {@link FieldHeader} and {@link FieldInfo} data. Provided by {@link DefinitionsService} for looking up
 * fields and their related type data.
 */
@Immutable
public interface FieldInstance extends Comparable<FieldInstance> {

  static ImmutableFieldInstance.Builder builder() {
    return ImmutableFieldInstance.builder();
  }

  /**
   * Sort order position for fields of the same type. For example, "Fee" has a type "Amount" and has a sort order of
   * 8th.
   *
   * @return An int with the nth value.
   */
  int nth();

  /**
   * If field is included in signed transactions.
   *
   * @return {@code true} if this is a signing field; {@code false} otherwise.
   */
  boolean isSigningField();

  /**
   * If field is included in binary serialized representation.
   *
   * @return {@code true} if this FieldInof is serialized; {@code false} otherwise.
   */
  boolean isSerialized();

  /**
   * Field name.
   *
   * @return A {@link String} representing the name of this FieldInstance.
   */
  String name();

  /**
   * XRPL type (e.g. UInt32, AccountID, etc).
   *
   * @return A {@link String} representing the type of this FieldInstance.
   */
  String type();

  /**
   * Whether or not this FieldInstance is variable-length encoded.
   *
   * @return {@code true} this FieldInstance is variable-length encoded; {@code false} otherwise.
   */
  boolean isVariableLengthEncoded();

  /**
   * Globally unique ordinal position based on type code and field code.
   *
   * @return An int representing the ordinal of this FieldInstance.
   */
  default int ordinal() {
    return (header().typeCode() << 16) | nth();
  }

  /**
   * The {@link FieldHeader} for this {@link FieldInstance}.
   *
   * @return A {@link FieldHeader} for this {@link FieldInstance}.
   */
  FieldHeader header();

  @Override
  default int compareTo(FieldInstance other) {
    return Integer.compare(this.ordinal(), other.ordinal());
  }
}
