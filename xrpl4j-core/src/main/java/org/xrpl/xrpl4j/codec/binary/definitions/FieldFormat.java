package org.xrpl.xrpl4j.codec.binary.definitions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Represents a single field entry in LEDGER_ENTRY_FORMATS or TRANSACTION_FORMATS from definitions.json.
 */
@Immutable
@JsonSerialize(as = ImmutableFieldFormat.class)
@JsonDeserialize(as = ImmutableFieldFormat.class)
public interface FieldFormat {

  /**
   * The name of the field.
   *
   * @return A {@link String} field name.
   */
  @JsonProperty("name")
  String name();

  /**
   * The optionality of this field in the ledger entry or transaction format.
   * 0 = required, 1 = optional, 2 = default.
   *
   * @return An int representing optionality.
   */
  @JsonProperty("optionality")
  int optionality();

}
