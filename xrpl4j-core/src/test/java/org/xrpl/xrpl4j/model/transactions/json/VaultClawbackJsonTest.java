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
import org.xrpl.xrpl4j.model.transactions.VaultClawback;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link VaultClawback} JSON serialization.
 */
public class VaultClawbackJsonTest extends AbstractJsonTest {

  @Test
  public void testVaultClawbackJsonWithIssuedCurrencyAmount() throws JsonProcessingException, JSONException {
    VaultClawback vaultClawback = VaultClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
          .value("50")
          .build()
      )
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "\"Account\":\"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "\"VaultID\":\"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "\"Holder\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"Amount\":{" +
      "\"currency\":\"USD\"," +
      "\"issuer\":\"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "\"value\":\"50\"" +
      "}," +
      "\"Fee\":\"10\"," +
      "\"Sequence\":1," +
      "\"SigningPubKey\":\"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "\"TransactionType\":\"VaultClawback\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultClawback, json);
  }

  @Test
  public void testVaultClawbackJsonWithRequiredFieldsOnly() throws JsonProcessingException, JSONException {
    VaultClawback vaultClawback = VaultClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "\"Account\":\"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "\"VaultID\":\"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "\"Holder\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"Fee\":\"10\"," +
      "\"Sequence\":1," +
      "\"SigningPubKey\":\"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "\"TransactionType\":\"VaultClawback\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultClawback, json);
  }

  @Test
  public void testVaultClawbackJsonWithXrpAmount() throws JsonProcessingException, JSONException {
    VaultClawback vaultClawback = VaultClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .amount(XrpCurrencyAmount.ofDrops(100000))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "\"Account\":\"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "\"VaultID\":\"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "\"Holder\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"Amount\":\"100000\"," +
      "\"Fee\":\"10\"," +
      "\"Sequence\":1," +
      "\"SigningPubKey\":\"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "\"TransactionType\":\"VaultClawback\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultClawback, json);
  }

  @Test
  public void testVaultClawbackJsonWithMptAmount() throws JsonProcessingException, JSONException {
    VaultClawback vaultClawback = VaultClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E"))
          .value("1000")
          .build()
      )
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "\"Account\":\"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "\"VaultID\":\"0000000000000000000000000000000000000000000000000000000000000001\"," +
      "\"Holder\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"Amount\":{" +
      "\"mpt_issuance_id\":\"000004C463C52827307480341125DA0577DEFC38405B0E3E\"," +
      "\"value\":\"1000\"" +
      "}," +
      "\"Fee\":\"10\"," +
      "\"Sequence\":1," +
      "\"SigningPubKey\":\"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "\"TransactionType\":\"VaultClawback\"" +
      "}";

    assertCanSerializeAndDeserialize(vaultClawback, json);
  }
}

