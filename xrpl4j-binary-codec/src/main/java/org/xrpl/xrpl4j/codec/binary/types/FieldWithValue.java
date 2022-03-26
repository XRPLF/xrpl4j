package org.xrpl.xrpl4j.codec.binary.types;

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
