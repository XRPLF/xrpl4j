package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link TransferFee}s.
 */
public class TransferFeeDeserializer  extends StdDeserializer<TransferFee> {

  /**
   * No-args constructor.
   */
  public TransferFeeDeserializer() {
    super(TransferFee.class);
  }

  @Override
  public TransferFee deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return TransferFee.of(UnsignedInteger.valueOf(jsonParser.getText()));
  }
}
