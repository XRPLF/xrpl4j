package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom deserializer for {@link AccountTransactionsRequestParams}. This is needed because Jackson cannot
 * deserialize {@link com.fasterxml.jackson.annotation.JsonUnwrapped} values if they are wrapped in an {@link Optional},
 * as is the case with {@link AccountTransactionsRequestParams#ledgerSpecifier()}.
 */
public class AccountTransactionsRequestParamsDeserializer extends StdDeserializer<AccountTransactionsRequestParams> {

  public AccountTransactionsRequestParamsDeserializer() {
    super(AccountTransactionsRequestParams.class);
  }

  @Override
  public AccountTransactionsRequestParams deserialize(
    JsonParser jsonParser,
    DeserializationContext ctxt
  ) throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = objectMapper.readTree(jsonParser);

    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of(node.get("account").asText()))
      .ledgerIndexMin(
        node.has("ledger_index_min") ?
          LedgerIndexBound.of(node.get("ledger_index_min").asLong()) :
          null
      )
      .ledgerIndexMax(
        node.has("ledger_index_max") ?
          LedgerIndexBound.of(node.get("ledger_index_max").asLong()) :
          null
      )
      .forward(node.get("forward").asBoolean())
      .limit(
        Optional.ofNullable(node.get("limit"))
          .map(JsonNode::asLong)
          .map(UnsignedInteger::valueOf)
      )
      .marker(
        Optional.ofNullable(node.get("marker"))
          .map(JsonNode::toString)
          .map(markerString -> {
            try {
              return objectMapper.readValue(markerString, Marker.class);
            } catch (JsonProcessingException e) {
              return null;
            }
          })
      )
      .build();

    LedgerSpecifier ledgerSpecifier = null;

    final JsonNode ledgerHash = node.get("ledger_hash");
    if (ledgerHash != null) {
      ledgerSpecifier = LedgerSpecifier.ledgerHash(Hash256.of(ledgerHash.asText()));
    } else if (node.has("ledger_index")) {
      final JsonNode ledgerIndex = node.get("ledger_index");
      if (ledgerIndex.isNumber()) {
        ledgerSpecifier = LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(ledgerIndex.asLong())));
      } else {
        ledgerSpecifier = LedgerSpecifier.ledgerIndexShortcut(
          objectMapper.readValue(ledgerIndex.toString(), LedgerIndexShortcut.class)
        );
      }
    }

    return AccountTransactionsRequestParams.builder().from(params)
      .ledgerSpecifier(Optional.ofNullable(ledgerSpecifier))
      .build();
  }
}
