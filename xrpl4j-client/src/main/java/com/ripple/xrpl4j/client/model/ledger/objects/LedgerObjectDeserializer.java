package com.ripple.xrpl4j.client.model.ledger.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.client.model.ledger.objects.LedgerObject.LedgerEntryType;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Custom deserializer for {@link LedgerObject}s. Because {@link LedgerObject} is a marker interface, we need
 * this deserializer to deserialize JSON ledger objects to the correct extension of {@link LedgerObject}.
 */
public class LedgerObjectDeserializer extends JsonDeserializer<LedgerObject> {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  Map<LedgerEntryType, Class<? extends LedgerObject>> objectTypeMap = new ImmutableMap.Builder<LedgerEntryType, Class<? extends LedgerObject>>()
    .put(LedgerEntryType.CHECK, CheckObject.class)
    .put(LedgerEntryType.DEPOSIT_PRE_AUTH, DepositPreAuthObject.class)
    .put(LedgerEntryType.ACCOUNT_ROOT, AccountRootObject.class)
    .put(LedgerEntryType.ESCROW, EscrowObject.class)
    .put(LedgerEntryType.RIPPLE_STATE, RippleStateObject.class)
    .put(LedgerEntryType.OFFER, OfferObject.class)
    .put(LedgerEntryType.PAY_CHANNEL, PayChannelObject.class)
    // TODO Add other ledger object types
    .build();

  @Override
  public LedgerObject deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    return objectMapper.readValue(node.toString(), objectTypeMap.get(LedgerEntryType.forValue(node.get("LedgerEntryType").asText())));
  }
}
