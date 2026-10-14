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
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

import java.io.IOException;
import java.util.Locale;
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
    if (jsonNode == null || !jsonNode.isObject() || jsonNode.size() != 1) {
      throw inputMismatch(
        jsonParser,
        "An AffectedNode must be a JSON object with exactly one field (CreatedNode, ModifiedNode, or DeletedNode), " +
          "but was %s.",
        describe(jsonNode)
      );
    }
    Map.Entry<String, JsonNode> nodeFieldAndValue = jsonNode.fields().next();
    String affectedNodeType = nodeFieldAndValue.getKey();
    JsonNode nodeValue = nodeFieldAndValue.getValue();
    if (!nodeValue.isObject()) {
      throw inputMismatch(jsonParser, "The value of %s must be a JSON object.", affectedNodeType);
    }
    JsonNode ledgerEntryTypeNode = nodeValue.get("LedgerEntryType");
    if (ledgerEntryTypeNode == null) {
      throw inputMismatch(jsonParser, "%s is missing required field LedgerEntryType.", affectedNodeType);
    }
    if (!ledgerEntryTypeNode.isTextual()) {
      throw inputMismatch(
        jsonParser, "LedgerEntryType of %s must be a string, but was %s.",
        affectedNodeType, describe(ledgerEntryTypeNode)
      );
    }

    MetaLedgerEntryType ledgerEntryType = MetaLedgerEntryType.of(ledgerEntryTypeNode.asText());
    Class<? extends MetaLedgerObject> ledgerObjectClass = ledgerEntryType.ledgerObjectType();

    switch (affectedNodeType) {
      case "CreatedNode":
        return codec.treeToValue(
          nodeValue,
          codec.getTypeFactory().constructParametricType(CreatedNode.class, ledgerObjectClass)
        );
      case "ModifiedNode":
        return codec.treeToValue(
          nodeValue,
          codec.getTypeFactory().constructParametricType(ModifiedNode.class, ledgerObjectClass)
        );
      case "DeletedNode":
        return codec.treeToValue(
          nodeValue,
          codec.getTypeFactory().constructParametricType(DeletedNode.class, ledgerObjectClass)
        );
      default:
        throw JsonMappingException.from(
          jsonParser, String.format("Unrecognized AffectedNode type %s.", affectedNodeType)
        );
    }
  }

  /**
   * Builds a {@link MismatchedInputException} for malformed {@link AffectedNode} input. Callers {@code throw} the
   * result directly rather than calling {@code DeserializationContext#reportInputMismatch}, because an exception
   * raised inside that call is invisible to JaCoCo and leaves the whole statement reported as uncovered.
   *
   * @param jsonParser The {@link JsonParser} being read, used for location information.
   * @param message    A {@link String#format} message template.
   * @param args       Arguments for {@code message}.
   *
   * @return A {@link MismatchedInputException} describing the malformed input.
   */
  private static MismatchedInputException inputMismatch(
    final JsonParser jsonParser,
    final String message,
    final Object... args
  ) {
    return MismatchedInputException.from(jsonParser, AffectedNode.class, String.format(message, args));
  }

  /**
   * Describes the shape of a {@link JsonNode} for use in error messages, e.g. {@code "a JSON array"} or
   * {@code "a JSON object with 2 fields"}.
   *
   * @param jsonNode The {@link JsonNode} to describe, which may be {@code null}.
   *
   * @return A human-readable description of the node's shape.
   */
  private static String describe(final JsonNode jsonNode) {
    if (jsonNode == null) {
      return "missing";
    }
    if (jsonNode.isObject()) {
      return String.format("a JSON object with %d field(s)", jsonNode.size());
    }
    return "a JSON " + jsonNode.getNodeType().name().toLowerCase(Locale.ROOT);
  }
}
