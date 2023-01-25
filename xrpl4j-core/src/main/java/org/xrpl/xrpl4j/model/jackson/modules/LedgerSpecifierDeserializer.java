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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link LedgerSpecifier}s.
 */
public class LedgerSpecifierDeserializer extends StdDeserializer<LedgerSpecifier> {

  /**
   * No-args constructor.
   */
  protected LedgerSpecifierDeserializer() {
    super(LedgerSpecifier.class);
  }

  @Override
  public JsonDeserializer<LedgerSpecifier> unwrappingDeserializer(NameTransformer unwrapper) {
    return new LedgerSpecifierDeserializer();
  }

  @Override
  public LedgerSpecifier deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    final ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    final ObjectNode node = objectMapper.readTree(jsonParser);

    final JsonNode ledgerHash = node.get("ledger_hash");
    if (ledgerHash != null) {
      return LedgerSpecifier.of(Hash256.of(ledgerHash.asText()));
    } else {
      final JsonNode ledgerIndex = node.get("ledger_index");
      if (ledgerIndex.isNumber()) {
        return LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.valueOf(ledgerIndex.asInt())));
      } else {
        switch (ledgerIndex.asText()) {
          case "validated":
            return LedgerSpecifier.VALIDATED;
          case "current":
            return LedgerSpecifier.CURRENT;
          case "closed":
            return LedgerSpecifier.CLOSED;
          default:
            throw new JsonParseException(jsonParser, "Unrecognized LedgerIndex shortcut " + ledgerIndex.toString());
        }
      }
    }
  }
}
