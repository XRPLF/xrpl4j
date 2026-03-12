package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NumberAmount;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.VaultSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link VaultSet} JSON serialization.
 */
public class VaultSetJsonTest extends AbstractJsonTest {

  @Test
  public void testVaultSetJsonWithAllFields() throws JsonProcessingException, JSONException {
    VaultSet vaultSet = VaultSet.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .assetsMaximum(NumberAmount.of("5000000"))
      .domainId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000002"))
      .data(VaultData.of("48656C6C6F"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"VaultID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"AssetsMaximum\": \"5000000\"," +
      "  \"DomainID\": \"0000000000000000000000000000000000000000000000000000000000000002\"," +
      "  \"Data\": \"48656C6C6F\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultSet\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultSet, json);
  }

  @Test
  public void testVaultSetJsonWithRequiredFieldsOnly() throws JsonProcessingException, JSONException {
    VaultSet vaultSet = VaultSet.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"VaultID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultSet\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultSet, json);
  }
}

