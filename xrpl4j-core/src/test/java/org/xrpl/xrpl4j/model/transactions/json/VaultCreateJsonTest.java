package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.VaultCreateFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.VaultCreate;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link VaultCreate} JSON serialization.
 */
public class VaultCreateJsonTest extends AbstractJsonTest {

  @Test
  public void testVaultCreateJsonWithAllFields() throws JsonProcessingException, JSONException {
    VaultCreate vaultCreate = VaultCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .asset(
        IouIssue.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .build()
      )
      .assetsMaximum(Amount.of("1000000"))
      .mpTokenMetadata(MpTokenMetadata.of("AABB"))
      .domainId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .data(VaultData.of("48656C6C6F"))
      .scale(AssetScale.of(UnsignedInteger.valueOf(8)))
      .flags(VaultCreateFlags.of(VaultCreateFlags.VAULT_PRIVATE.getValue() |
        VaultCreateFlags.VAULT_SHARE_NON_TRANSFERABLE.getValue()))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Asset\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"" +
      "  }," +
      "  \"AssetsMaximum\": \"1000000\"," +
      "  \"MPTokenMetadata\": \"AABB\"," +
      "  \"DomainID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"WithdrawalPolicy\": 1," +
      "  \"Data\": \"48656C6C6F\"," +
      "  \"Scale\": 8," +
      "  \"Flags\": 196608," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultCreate\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultCreate, json);
  }

  @Test
  public void testVaultCreateJsonWithRequiredFieldsOnly() throws JsonProcessingException, JSONException {
    VaultCreate vaultCreate = VaultCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .asset(Issue.XRP)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Asset\": {" +
      "    \"currency\": \"XRP\"" +
      "  }," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultCreate\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultCreate, json);
  }

  @Test
  public void testVaultCreateJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    VaultCreate vaultCreate = VaultCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .asset(Issue.XRP)
      .flags(VaultCreateFlags.of(0))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Asset\": {" +
      "    \"currency\": \"XRP\"" +
      "  }," +
      "  \"Flags\": 0," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultCreate\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultCreate, json);
  }

  @Test
  public void testVaultCreateJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    VaultCreate vaultCreate = VaultCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .asset(Issue.XRP)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Foo\": \"Bar\"," +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Asset\": {" +
      "    \"currency\": \"XRP\"" +
      "  }," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultCreate\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultCreate, json);
  }

  @Test
  public void testVaultCreateJsonWithMptAsset() throws JsonProcessingException, JSONException {
    VaultCreate vaultCreate = VaultCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .asset(
        MptIssue.builder()
          .mptIssuanceId(MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E"))
          .build()
      )
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Asset\": {" +
      "    \"mpt_issuance_id\": \"000004C463C52827307480341125DA0577DEFC38405B0E3E\"" +
      "  }," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultCreate\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultCreate, json);
  }
}

