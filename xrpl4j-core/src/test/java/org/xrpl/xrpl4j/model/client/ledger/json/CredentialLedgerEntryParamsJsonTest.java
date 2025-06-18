package org.xrpl.xrpl4j.model.client.ledger.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.ledger.CredentialLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

public class CredentialLedgerEntryParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testCredentialLedgerEntryParams() throws JSONException, JsonProcessingException {
    CredentialLedgerEntryParams credentialLedgerEntryParams = CredentialLedgerEntryParams.builder()
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
      .subject(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1oa"))
      .build();

    String json = "{" +
      "  \"subject\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1oa\"," +
      "  \"issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"," +
      "  \"credential_type\": \"647269766572206C6963656E6365\"" +
      "}";

    assertCanSerializeAndDeserialize(credentialLedgerEntryParams, json, CredentialLedgerEntryParams.class);
  }
}
