package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.VaultWithdraw;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link VaultWithdraw} JSON serialization.
 */
public class VaultWithdrawJsonTest extends AbstractJsonTest {

  @Test
  public void testVaultWithdrawJsonWithAllFields() throws JsonProcessingException, JSONException {
    VaultWithdraw vaultWithdraw = VaultWithdraw.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .amount(XrpCurrencyAmount.ofDrops(500000))
      .destination(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .destinationTag(UnsignedInteger.valueOf(42))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"VaultID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"Amount\": \"500000\"," +
      "  \"Destination\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "  \"DestinationTag\": 42," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultWithdraw\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultWithdraw, json);
  }

  @Test
  public void testVaultWithdrawJsonWithXrpAmount() throws JsonProcessingException, JSONException {
    VaultWithdraw vaultWithdraw = VaultWithdraw.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .amount(XrpCurrencyAmount.ofDrops(500000))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"VaultID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"Amount\": \"500000\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultWithdraw\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultWithdraw, json);
  }

  @Test
  public void testVaultWithdrawJsonWithIssuedCurrencyAmount() throws JsonProcessingException, JSONException {
    VaultWithdraw vaultWithdraw = VaultWithdraw.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("50.25")
          .build()
      )
      .destination(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"VaultID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"Amount\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "    \"value\": \"50.25\"" +
      "  }," +
      "  \"Destination\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultWithdraw\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultWithdraw, json);
  }

  @Test
  public void testVaultWithdrawJsonWithMptAmount() throws JsonProcessingException, JSONException {
    VaultWithdraw vaultWithdraw = VaultWithdraw.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E"))
          .value("2500")
          .build()
      )
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"VaultID\": \"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "  \"Amount\": {" +
      "    \"mpt_issuance_id\": \"000004C463C52827307480341125DA0577DEFC38405B0E3E\"," +
      "    \"value\": \"2500\"" +
      "  }," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"TransactionType\": \"VaultWithdraw\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultWithdraw, json);
  }
}

