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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Serializes a {@link BigDecimal} as a JSON number in plain notation (e.g., {@code 0.0000001} rather than
 * {@code 1E-7}), matching {@link JsonGenerator.Feature#WRITE_BIGDECIMAL_AS_PLAIN} without requiring that feature to
 * be enabled mapper-wide. Supplied for {@link BigDecimal} properties of xrpl4j classes by
 * {@link Xrpl4jAnnotationIntrospector}.
 */
public class BigDecimalPlainSerializer extends StdScalarSerializer<BigDecimal> {

  /**
   * No-args constructor.
   */
  public BigDecimalPlainSerializer() {
    super(BigDecimal.class);
  }

  @Override
  public void serialize(
    final BigDecimal value,
    final JsonGenerator gen,
    final SerializerProvider provider
  ) throws IOException {
    // Mirrors Jackson's NumberSerializer: a TokenBuffer must keep the typed value so that tree conversions preserve
    // a DecimalNode rather than an encoded string.
    if (gen instanceof TokenBuffer) {
      gen.writeNumber(value);
    } else {
      gen.writeNumber(value.toPlainString());
    }
  }
}
