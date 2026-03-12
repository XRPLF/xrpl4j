package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.VaultDelete;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link VaultDelete} JSON serialization.
 */
public class VaultDeleteJsonTest extends AbstractJsonTest {

  @Test
  public void testVaultDeleteJson() throws JsonProcessingException, JSONException {
    VaultDelete vaultDelete = VaultDelete.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "\"Account\":\"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "\"VaultID\":\"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "\"Fee\":\"10\"," +
      "\"Sequence\":1," +
      "\"SigningPubKey\":\"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "\"TransactionType\":\"VaultDelete\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultDelete, json);
  }
}

