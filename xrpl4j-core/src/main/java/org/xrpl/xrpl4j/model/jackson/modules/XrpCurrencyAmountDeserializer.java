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
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Custom Jackson deserializer for {@link XrpCurrencyAmount}s.
 */
public class XrpCurrencyAmountDeserializer extends StdDeserializer<XrpCurrencyAmount> {

  /**
   * XRP drops amounts must be a plain, base-10 integer string (e.g. {@code "1000000"}), optionally prefixed with
   * {@code -}. Anything else (e.g. {@code "1e6"}, {@code "1.0"}, {@code "0x1"}, or a value with leading/trailing
   * whitespace) is rejected here rather than silently coerced, because {@link JsonParser#getValueAsLong()} will
   * otherwise parse non-standard formats into a different drops value than what a caller may have already validated
   * against the raw input.
   */
  private static final Pattern DROPS_PATTERN = Pattern.compile("-?\\d+");

  /**
   * No-args constructor.
   */
  public XrpCurrencyAmountDeserializer() {
    super(XrpCurrencyAmount.class);
  }

  @Override
  public XrpCurrencyAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    String rawValue = jsonParser.getValueAsString();
    if (rawValue == null || !DROPS_PATTERN.matcher(rawValue).matches()) {
      throw MismatchedInputException.from(
        jsonParser,
        XrpCurrencyAmount.class,
        String.format(
          "XRP drops amount must be a plain integer string (e.g. \"1000000\"), but was \"%s\".", rawValue
        )
      );
    }

    final long drops;
    try {
      drops = Long.parseLong(rawValue);
    } catch (NumberFormatException e) {
      throw MismatchedInputException.from(
        jsonParser,
        XrpCurrencyAmount.class,
        String.format("XRP drops amount \"%s\" is out of range for a 64-bit integer.", rawValue)
      );
    }
    return XrpCurrencyAmount.ofDrops(drops);
  }
}
