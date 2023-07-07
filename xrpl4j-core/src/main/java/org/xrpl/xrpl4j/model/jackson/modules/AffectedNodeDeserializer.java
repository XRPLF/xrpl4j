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
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaCheckObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaDepositPreAuthObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaEscrowObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaNfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaNfTokenPageObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaOfferObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaPayChannelObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaRippleStateObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaSignerListObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaTicketObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaUnknownObject;
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

    String ledgerEntryType = nodeFieldAndValue.getValue().get("LedgerEntryType").asText();
    Class<? extends MetaLedgerObject> ledgerObjectClass = determineLedgerObjectType(ledgerEntryType);

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

  private Class<? extends MetaLedgerObject> determineLedgerObjectType(String ledgerEntryType) {
    switch (ledgerEntryType) {
      case "AccountRoot":
        return MetaAccountRootObject.class;
      case "Check":
        return MetaCheckObject.class;
      case "DepositPreauth":
        return MetaDepositPreAuthObject.class;
      case "Escrow":
        return MetaEscrowObject.class;
      case "NFTokenOffer":
        return MetaNfTokenOfferObject.class;
      case "Offer":
        return MetaOfferObject.class;
      case "PayChannel":
        return MetaPayChannelObject.class;
      case "RippleState":
        return MetaRippleStateObject.class;
      case "SignerList":
        return MetaSignerListObject.class;
      case "Ticket":
        return MetaTicketObject.class;
      case "NFTokenPage":
        return MetaNfTokenPageObject.class;
      default:
        return MetaUnknownObject.class;
    }
  }
}
