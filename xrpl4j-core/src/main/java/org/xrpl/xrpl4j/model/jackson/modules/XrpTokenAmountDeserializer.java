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
import org.xrpl.xrpl4j.model.transactions.amount.XrpAmount;
import org.xrpl.xrpl4j.model.transactions.amount.XrpTokenAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link XrpTokenAmount}.
 *
 * <p>Delegates to {@link XrpAmountDeserializer} to read the bare JSON string or number, then
 * wraps the resulting {@link XrpAmount} via {@link XrpTokenAmount#of(XrpAmount)}.
 */
public class XrpTokenAmountDeserializer extends StdDeserializer<XrpTokenAmount> {

  private static final XrpAmountDeserializer AMOUNT_DESERIALIZER = new XrpAmountDeserializer();

  /**
   * No-args constructor.
   */
  public XrpTokenAmountDeserializer() {
    super(XrpTokenAmount.class);
  }

  @Override
  public XrpTokenAmount deserialize(
    final JsonParser jsonParser,
    final DeserializationContext ctxt
  ) throws IOException {
    return XrpTokenAmount.of(AMOUNT_DESERIALIZER.deserialize(jsonParser, ctxt));
  }
}
