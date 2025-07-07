package org.xrpl.xrpl4j.model.client.ledger.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.ledger.DepositPreAuthCredential;
import org.xrpl.xrpl4j.model.client.ledger.DepositPreAuthLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

import java.util.Collections;
import java.util.List;

public class DepositPreAuthLedgerEntryParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testDepositPreAuthLedgerEntryParams() throws JSONException, JsonProcessingException {
    List<DepositPreAuthCredential> credentials = Collections.singletonList(
      DepositPreAuthCredential
        .builder()
        .credentialType(CredentialType.of("6D795F63726564656E7469616C"))
        .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
        .build()
    );

    DepositPreAuthLedgerEntryParams depositPreAuthParams = DepositPreAuthLedgerEntryParams.builder()
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .authorizedCredentials(credentials)
      .build();

    String json = "{" +
      "  \"owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"authorized_credentials\": [" +
      "    {" +
      "      \"issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"," +
      "      \"credential_type\": \"6D795F63726564656E7469616C\"" +
      "    }" +
      "  ]" +
      "}";

    assertCanSerializeAndDeserialize(depositPreAuthParams, json, DepositPreAuthLedgerEntryParams.class);
  }
}
