package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Collections;
import java.util.List;

class MetaDepositPreAuthObjectTest extends AbstractJsonTest {

  @Test
  public void testMetaDepositPreAuthObjectTest() throws JsonProcessingException, JSONException {
    List<MetaCredentialWrapper> credentials = Collections.singletonList(
      ImmutableMetaCredentialWrapper
        .builder()
        .credential(
          ImmutableMetaCredential.builder()
            .credentialType(CredentialType.of("6D795F63726564656E7469616C"))
            .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
            .build())
        .build()
    );

    MetaDepositPreAuthObject object = ImmutableMetaDepositPreAuthObject.builder()
      .account(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
      .authorizeCredentials(credentials)
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3D4665AE6874D7E4E34B45E906FF970CD820EB42B1DBD238588E845466D1CE61"))
      .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(3113101)))
      .build();

    String json = "{" +
      "  \"Account\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"," +
      "  \"AuthorizeCredentials\": [" +
      "    {" +
      "      \"Credential\": {" +
      "        \"CredentialType\": \"6D795F63726564656E7469616C\"," +
      "        \"Issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"" +
      "      }" +
      "    }" +
      "  ]," +
      "  \"Flags\": 0," +
      "  \"OwnerNode\": \"0\"," +
      "  \"PreviousTxnID\": \"3D4665AE6874D7E4E34B45E906FF970CD820EB42B1DBD238588E845466D1CE61\"," +
      "  \"PreviousTxnLgrSeq\": 3113101" +
      "}";

    assertCanSerializeAndDeserialize(object, json, MetaDepositPreAuthObject.class);
  }

}