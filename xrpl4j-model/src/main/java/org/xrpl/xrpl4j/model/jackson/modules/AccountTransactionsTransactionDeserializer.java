package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsTransaction;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.io.IOException;

public class AccountTransactionsTransactionDeserializer extends StdDeserializer<AccountTransactionsTransaction<?>> {

  public AccountTransactionsTransactionDeserializer() {
    super(AccountTransactionsTransaction.class);
  }

  @Override
  public AccountTransactionsTransaction<?> deserialize(
    JsonParser jsonParser,
    DeserializationContext ctxt
  ) throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = objectMapper.readTree(jsonParser);

    Transaction transaction = objectMapper.readValue(node.toString(), Transaction.class);
    long ledgerIndex = node.get("ledger_index").asLong(-1L);
    String hash = node.get("hash").asText();
    return AccountTransactionsTransaction.builder()
      .transaction(transaction)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(ledgerIndex)))
      .hash(Hash256.of(hash))
      .build();
  }
}
