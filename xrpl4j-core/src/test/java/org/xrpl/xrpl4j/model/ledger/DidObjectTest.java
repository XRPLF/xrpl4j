package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DidData;
import org.xrpl.xrpl4j.model.transactions.DidDocument;
import org.xrpl.xrpl4j.model.transactions.DidUri;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class DidObjectTest extends AbstractJsonTest {

  @Test
  void testJsonWithNonEmptyValues() throws JSONException, JsonProcessingException {
    DidObject object = DidObject.builder()
      .account(Address.of("rpfqJrXg5uidNo2ZsRhRY6TiF1cvYmV9Fg"))
      .didDocument(DidDocument.of("646F63"))
      .data(DidData.of("617474657374"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("A4C15DA185E6092DF5954FF62A1446220C61A5F60F0D93B4B09F708778E41120"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(4))
      .uri(DidUri.of("6469645F6578616D706C65"))
      .index(Hash256.of("46813BE38B798B3752CA590D44E7FEADB17485649074403AD1761A2835CE91FF"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rpfqJrXg5uidNo2ZsRhRY6TiF1cvYmV9Fg\",\n" +
      "    \"DIDDocument\": \"646F63\",\n" +
      "    \"Data\": \"617474657374\",\n" +
      "    \"Flags\": 0,\n" +
      "    \"LedgerEntryType\": \"DID\",\n" +
      "    \"OwnerNode\": \"0\",\n" +
      "    \"PreviousTxnID\": \"A4C15DA185E6092DF5954FF62A1446220C61A5F60F0D93B4B09F708778E41120\",\n" +
      "    \"PreviousTxnLgrSeq\": 4,\n" +
      "    \"URI\": \"6469645F6578616D706C65\",\n" +
      "    \"index\": \"46813BE38B798B3752CA590D44E7FEADB17485649074403AD1761A2835CE91FF\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  void testJsonWithEmptyValues() throws JSONException, JsonProcessingException {
    DidObject object = DidObject.builder()
      .account(Address.of("rpfqJrXg5uidNo2ZsRhRY6TiF1cvYmV9Fg"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("A4C15DA185E6092DF5954FF62A1446220C61A5F60F0D93B4B09F708778E41120"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(4))
      .index(Hash256.of("46813BE38B798B3752CA590D44E7FEADB17485649074403AD1761A2835CE91FF"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rpfqJrXg5uidNo2ZsRhRY6TiF1cvYmV9Fg\",\n" +
      "    \"Flags\": 0,\n" +
      "    \"LedgerEntryType\": \"DID\",\n" +
      "    \"OwnerNode\": \"0\",\n" +
      "    \"PreviousTxnID\": \"A4C15DA185E6092DF5954FF62A1446220C61A5F60F0D93B4B09F708778E41120\",\n" +
      "    \"PreviousTxnLgrSeq\": 4,\n" +
      "    \"index\": \"46813BE38B798B3752CA590D44E7FEADB17485649074403AD1761A2835CE91FF\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}