package org.xrpl.xrpl4j.model.jackson.modules;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.amount.IouAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link IouAmount}.
 *
 * <p>Reads a bare JSON string containing the decimal or scientific-notation value
 * (e.g. {@code "100.50"} or {@code "1.23e10"}) and constructs an {@link IouAmount} via {@link IouAmount#of(String)}.
 */
public class IouAmountDeserializer extends StdDeserializer<IouAmount> {

  /**
   * No-args constructor.
   */
  public IouAmountDeserializer() {
    super(IouAmount.class);
  }

  @Override
  public IouAmount deserialize(
    final JsonParser jsonParser,
    final DeserializationContext ctxt
  ) throws IOException {
    return fromString(jsonParser.getValueAsString());
  }

  /**
   * Construct an {@link IouAmount} from a raw decimal or scientific-notation string
   * (e.g. {@code "100.50"} or {@code "1.23e10"}).
   *
   * <p>Extracted as a package-private static so that {@link IouTokenAmountDeserializer} can
   * reuse the same parsing logic when reading the {@code "value"} field of a JSON object
   * without needing to synthesize a sub-{@link JsonParser}.
   *
   * @param raw The raw string value. Must not be null.
   *
   * @return An {@link IouAmount}.
   */
  static IouAmount fromString(final String raw) {
    return IouAmount.of(raw);
  }
}
