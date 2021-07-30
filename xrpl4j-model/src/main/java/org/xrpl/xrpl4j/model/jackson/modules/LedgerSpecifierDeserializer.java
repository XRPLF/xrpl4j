package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link LedgerSpecifier}s.
 */
public class LedgerSpecifierDeserializer extends StdDeserializer<LedgerSpecifier> {

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
      return LedgerSpecifier.ledgerHash(Hash256.of(ledgerHash.asText()));
    } else {
      final JsonNode ledgerIndex = node.get("ledger_index");
      if (ledgerIndex.isNumber()) {
        return LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(ledgerIndex.asLong())));
      } else {
        return LedgerSpecifier.ledgerIndexShortcut(
          objectMapper.readValue(ledgerIndex.toString(), LedgerIndexShortcut.class)
        );
      }
    }
  }
}
