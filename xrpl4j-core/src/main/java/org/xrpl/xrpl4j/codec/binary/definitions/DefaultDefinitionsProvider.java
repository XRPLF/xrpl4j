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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.Objects;

public class DefaultDefinitionsProvider implements DefinitionsProvider {

  private final Supplier<Definitions> supplier;

  /**
   * Required-args Constructor.
   *
   * @param objectMapper An {@link ObjectMapper}.
   */
  @SuppressWarnings("UnstableApiUsage")
  public DefaultDefinitionsProvider(final ObjectMapper objectMapper) {
    Objects.requireNonNull(objectMapper);

    this.supplier = Suppliers.memoize(() -> {
      try {
        return objectMapper.readerFor(Definitions.class)
          .readValue(Resources.getResource(DefaultDefinitionsProvider.class, "/definitions.json"));
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read definition.json file", e);
      }
    });
  }

  @Override
  public Definitions get() {
    return supplier.get();
  }

}
