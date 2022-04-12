package org.xrpl.xrpl4j.model.immutables;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
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

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

/**
 * Custom immutables annotation which can be used to generate immutable wrapper classes.
 *
 * @see "http://immutables.github.io/immutable.html#wrapper-types"
 */
@Value.Style(
  // Detect names starting with underscore
  typeAbstract = "_*",
  // Generate without any suffix, just raw detected name
  typeImmutable = "*",
  // Make generated public, leave underscored as package private
  visibility = ImplementationVisibility.PUBLIC,
  // Seems unnecessary to have builder or superfluous copy method
  defaults = @Value.Immutable(builder = false, copy = false))
public @interface Wrapped {

}
