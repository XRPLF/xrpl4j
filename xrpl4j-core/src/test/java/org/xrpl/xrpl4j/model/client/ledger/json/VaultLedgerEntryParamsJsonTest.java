package org.xrpl.xrpl4j.model.client.ledger.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.ledger.VaultLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Address;

public class VaultLedgerEntryParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testVaultLedgerEntryParams() throws JSONException, JsonProcessingException {
    VaultLedgerEntryParams vaultLedgerEntryParams = VaultLedgerEntryParams.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j"))
      .seq(UnsignedInteger.valueOf(123))
      .build();

    String json = "{" +
      "  \"owner\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j\"," +
      "  \"seq\": 123" +
      "}";

    assertCanSerializeAndDeserialize(vaultLedgerEntryParams, json, VaultLedgerEntryParams.class);
  }
}

