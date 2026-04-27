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
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link MptAmount}.
 *
 * <p>Reads a bare JSON string containing the integer value, optionally prefixed with {@code -}
 * (e.g. {@code "1000"} or {@code "-500"}), and constructs an {@link MptAmount} via
 * {@link MptAmount#of(UnsignedLong, boolean)}.
 */
public class MptAmountDeserializer extends StdDeserializer<MptAmount> {

  /**
   * No-args constructor.
   */
  public MptAmountDeserializer() {
    super(MptAmount.class);
  }

  @Override
  public MptAmount deserialize(
    final JsonParser jsonParser,
    final DeserializationContext ctxt
  ) throws IOException {
    return fromString(jsonParser.getValueAsString());
  }

  /**
   * Construct an {@link MptAmount} from a raw decimal-integer string, optionally prefixed with
   * {@code -} (e.g. {@code "1000"} or {@code "-500"}).
   *
   * <p>Extracted as a package-private static so that {@link MptTokenAmountDeserializer} can
   * reuse the same parsing logic when reading the {@code "value"} field of a JSON object
   * without needing to synthesize a sub-{@link JsonParser}.
   *
   * @param raw The raw string value. Must not be null.
   *
   * @return An {@link MptAmount}.
   */
  static MptAmount fromString(final String raw) {
    final boolean isNegative = raw.startsWith("-");
    final UnsignedLong magnitude = UnsignedLong.valueOf(isNegative ? raw.substring(1) : raw);
    return MptAmount.of(magnitude, isNegative);
  }
}
