package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.model.ledger.UnknownLedgerObject;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link UnknownLedgerObject}s. {@link UnknownLedgerObject} holds a {@link JsonNode}
 * of all the fields contained in the JSON being deserialized. However, we need this custom deserializer because the
 * {@link JsonNode} field is one level deeper than it is in the JSON.
 */
public class UnknownLedgerObjectDeserializer extends StdDeserializer<UnknownLedgerObject> {

  static Logger logger = LoggerFactory.getLogger(UnknownLedgerObjectDeserializer.class);

  protected UnknownLedgerObjectDeserializer() {
    super(UnknownLedgerObject.class);
  }

  @Override
  public UnknownLedgerObject deserialize(
    JsonParser jsonParser,
    DeserializationContext ctxt
  ) throws IOException, JacksonException {
    JsonNode treeNode = jsonParser.readValueAsTree();

    UnknownLedgerObject ledgerObject = UnknownLedgerObject.builder()
      .properties(treeNode)
      .build();

    logger.warn(
      "Encountered an unknown LedgerObject of type {}. xrpl4j either does not yet support this LedgerObject type," +
        " or the version you are using does not support this LedgerObject type.",
      ledgerObject.ledgerEntryType()
    );
    return ledgerObject;
  }
}
