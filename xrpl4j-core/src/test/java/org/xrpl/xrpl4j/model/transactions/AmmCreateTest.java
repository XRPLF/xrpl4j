package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

class AmmCreateTest extends AbstractJsonTest {

  static final MpTokenIssuanceId MPT_ISSUANCE_ID =
    MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"250000000\",\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }

  @Test
  void testJsonWithUnsetFlags() throws JSONException, JsonProcessingException {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"250000000\",\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"Flags\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }

  @Test
  void testJsonWithNonZeroFlags() throws JSONException, JsonProcessingException {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"250000000\",\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }

  @Test
  void testJsonWithUnknownFields() throws JSONException, JsonProcessingException {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
      "    \"Foo\" : \"Bar\",\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"250000000\",\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    assertThat(ammCreate.transactionFlags()).isEqualTo(ammCreate.flags());
    assertThat(ammCreate.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void builderFromCopiesFlagsCorrectly() {
    AmmCreate original = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    AmmCreate copied = AmmCreate.builder().from(original).build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }

  @Test
  void testJsonWithMptAmounts() throws JSONException, JsonProcessingException {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(MPT_ISSUANCE_ID)
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"mpt_issuance_id\" : \"00000002430427B80BD2D09D36B70B969E12801065F22308\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"250000000\",\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }

  @Test
  void testJsonWithBothMptAmounts() throws JSONException, JsonProcessingException {
    MpTokenIssuanceId mptIssuanceId2 = MpTokenIssuanceId.of("00000003430427B80BD2D09D36B70B969E12801065F22309");

    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(MPT_ISSUANCE_ID)
          .value("25")
          .build()
      )
      .amount2(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mptIssuanceId2)
          .value("50")
          .build()
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"mpt_issuance_id\" : \"00000002430427B80BD2D09D36B70B969E12801065F22308\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : {\n" +
      "        \"mpt_issuance_id\" : \"00000003430427B80BD2D09D36B70B969E12801065F22309\",\n" +
      "        \"value\" : \"50\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }
}
