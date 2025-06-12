package org.xrpl.xrpl4j.model.client.ledger.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.ledger.PermissionedDomainLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Address;

public class PermissionedDomainLedgerEntryParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testDepositPreAuthLedgerEntryParams() throws JSONException, JsonProcessingException {
    PermissionedDomainLedgerEntryParams param = PermissionedDomainLedgerEntryParams.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .seq(UnsignedInteger.ONE)
      .build();

    String json = "{" +
      "  \"account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"seq\": 1" +
      "}";

    assertCanSerializeAndDeserialize(param, json, PermissionedDomainLedgerEntryParams.class);
  }
}
