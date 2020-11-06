package com.ripple.xrplj4.client.model.ledger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Custom deserializer for {@link LedgerObject}s. Because {@link LedgerObject} is a marker interface, we need
 * this deserializer to deserialize JSON ledger objects to the correct extension of {@link LedgerObject}.
 */
public class LedgerObjectDeserializer extends JsonDeserializer<LedgerObject> {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  Map<String, Class<? extends LedgerObject>> objectTypeMap = new ImmutableMap.Builder<String, Class<? extends LedgerObject>>()
    .put("Check", Check.class)
    // TODO Add other ledger object types
    .build();

  @Override
  public LedgerObject deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    return objectMapper.readValue(node.toString(), objectTypeMap.get(node.get("LedgerEntryType").asText()));
  }
}
