package org.xrpl.xrpl4j.codec.fixtures.data;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableValueTest.class)
@JsonDeserialize(as = ImmutableValueTest.class)
public interface ValueTest {

  @JsonProperty("test_json")
  JsonNode testJson();

  @Nullable
  @JsonProperty("type_id")
  Integer typeId();

  @Value.Default
  @JsonProperty("is_native")
  default boolean isNative() {
    return false;
  }

  String type();

  @Nullable
  @JsonProperty("expected_hex")
  String expectedHex();

  @Value.Default
  @JsonProperty("is_negative")
  default boolean isNegative() {
    return false;
  }

  @Nullable
  @JsonProperty("type_specialisation_field")
  String typeSpecializationField();

  @Nullable
  String error();

}
