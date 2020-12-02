package com.ripple.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonParser;
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

  Map<LedgerObject.LedgerEntryType, Class<? extends LedgerObject>> objectTypeMap =
      new ImmutableMap.Builder<LedgerObject.LedgerEntryType, Class<? extends LedgerObject>>()
          .put(LedgerObject.LedgerEntryType.CHECK, CheckObject.class)
          .put(LedgerObject.LedgerEntryType.DEPOSIT_PRE_AUTH, DepositPreAuthObject.class)
          .put(LedgerObject.LedgerEntryType.ACCOUNT_ROOT, AccountRootObject.class)
          .put(LedgerObject.LedgerEntryType.ESCROW, EscrowObject.class)
          .put(LedgerObject.LedgerEntryType.RIPPLE_STATE, RippleStateObject.class)
          .put(LedgerObject.LedgerEntryType.OFFER, OfferObject.class)
          .put(LedgerObject.LedgerEntryType.PAY_CHANNEL, PayChannelObject.class)
          // TODO Add other ledger object types
          .build();

  @Override
  public LedgerObject deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    return objectMapper.readValue(
        node.toString(),
        objectTypeMap.get(LedgerObject.LedgerEntryType.forValue(node.get("LedgerEntryType").asText()))
    );
  }
}
