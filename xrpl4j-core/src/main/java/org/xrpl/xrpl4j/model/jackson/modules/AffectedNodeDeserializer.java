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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Custom Jackson deserializer for {@link AffectedNode}s.
 */
public class AffectedNodeDeserializer extends StdDeserializer<AffectedNode> {

  private static final Logger logger = LoggerFactory.getLogger(AffectedNodeDeserializer.class);

  /**
   * No-args constructor.
   */
  public AffectedNodeDeserializer() {
    super(AffectedNode.class);
  }

  @Override
  public AffectedNode deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    ObjectMapper codec = (ObjectMapper) jsonParser.getCodec();
    JsonNode jsonNode = jsonParser.readValueAsTree();
    try {
      return jsonNode.properties().stream()
        .findFirst()
        .map(entry -> deserializeNode(codec, jsonParser, entry))
        .orElseGet(() -> {
          logger.warn("AffectedNode JSON object has no fields; skipping. affectedNode={}", jsonNode);
          return null;
        });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private AffectedNode deserializeNode(
    ObjectMapper codec,
    JsonParser jsonParser,
    Map.Entry<String, JsonNode> nodeFieldAndValue
  ) {
    try {
      String affectedNodeType = nodeFieldAndValue.getKey();
      JsonNode nodeValue = nodeFieldAndValue.getValue();
      if (nodeValue == null || nodeValue.isNull()) {
        logger.warn("AffectedNode {} has no value; skipping. affectedNode={}", affectedNodeType, nodeFieldAndValue);
        return null; // caller's stream filters this via AffectedNodeListDeserializer
      }
      JsonNode ledgerEntryTypeNode = nodeValue.get("LedgerEntryType");
      if (ledgerEntryTypeNode == null || ledgerEntryTypeNode.isNull()) {
        logger.warn("AffectedNode {} is missing LedgerEntryType; skipping. affectedNode={}", affectedNodeType,
          nodeValue);
        return null; // caller's stream filters this via AffectedNodeListDeserializer
      }
      MetaLedgerEntryType ledgerEntryType = MetaLedgerEntryType.of(ledgerEntryTypeNode.asText());
      Class<? extends MetaLedgerObject> ledgerObjectClass = ledgerEntryType.ledgerObjectType();

      switch (affectedNodeType) {
        case "CreatedNode":
          return codec.treeToValue(
            nodeFieldAndValue.getValue(),
            codec.getTypeFactory().constructParametricType(CreatedNode.class, ledgerObjectClass)
          );
        case "ModifiedNode":
          return codec.treeToValue(
            nodeFieldAndValue.getValue(),
            codec.getTypeFactory().constructParametricType(ModifiedNode.class, ledgerObjectClass)
          );
        case "DeletedNode":
          return codec.treeToValue(
            nodeFieldAndValue.getValue(),
            codec.getTypeFactory().constructParametricType(DeletedNode.class, ledgerObjectClass)
          );
        default:
          throw JsonMappingException.from(
            jsonParser, String.format("Unrecognized AffectedNode type %s.", affectedNodeType)
          );
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
