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
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

import java.io.IOException;
import java.util.Map;

/**
 * Custom Jackson deserializer for {@link AffectedNode}s.
 */
public class AffectedNodeDeserializer extends StdDeserializer<AffectedNode> {

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
    Map.Entry<String, JsonNode> nodeFieldAndValue = jsonNode.fields().next();
    String affectedNodeType = nodeFieldAndValue.getKey();

    MetaLedgerEntryType ledgerEntryType = MetaLedgerEntryType.of(
      nodeFieldAndValue.getValue().get("LedgerEntryType").asText()
    );
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
  }
}
