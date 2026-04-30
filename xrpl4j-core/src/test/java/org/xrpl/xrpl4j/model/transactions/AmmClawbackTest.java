package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.AmmClawbackFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;

/**
 * Unit tests for {@link AmmClawback}.
 **/
public class AmmClawbackTest extends AbstractJsonTest {

  String usd = "USD";

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AmmClawback ammClawback = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(usd)
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .asset(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .asset2(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .holder(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json =
      "{\n" +
        "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Amount\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
        "        \"value\" : \"25\"\n" +
        "    },\n" +
        "    \"Asset\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
        "    },\n" +
        "    \"Asset2\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
        "    },\n" +
        "    \"Holder\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Fee\" : \"10\",\n" +
        "    \"Sequence\" : 6,\n" +
        "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "    \"TransactionType\" : \"AMMClawback\"\n" +
        "}";

    assertCanSerializeAndDeserialize(ammClawback, json);
  }

  @Test
  void testJsonWithUnsetFlags() throws JSONException, JsonProcessingException {
    AmmClawback ammClawback = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(usd)
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .holder(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .asset(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .asset2(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(AmmClawbackFlags.UNSET)
      .build();

    String json =
      "{\n" +
        "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Amount\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
        "        \"value\" : \"25\"\n" +
        "    },\n" +
        "    \"Asset\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
        "    },\n" +
        "    \"Asset2\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
        "    },\n" +
        "    \"Holder\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Flags\" : 0,\n" +
        "    \"Fee\" : \"10\",\n" +
        "    \"Sequence\" : 6,\n" +
        "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "    \"TransactionType\" : \"AMMClawback\"\n" +
        "}";

    assertCanSerializeAndDeserialize(ammClawback, json);
  }

  @Test
  void testJsonWithNonZeroFlags() throws JSONException, JsonProcessingException {
    AmmClawback ammClawback = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(usd)
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .holder(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .asset(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .asset2(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(AmmClawbackFlags.CLAW_TWO_ASSETS)
      .build();

    String json =
      "{\n" +
        "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Amount\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
        "        \"value\" : \"25\"\n" +
        "    },\n" +
        "    \"Asset\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
        "    },\n" +
        "    \"Asset2\" : {\n" +
        "        \"currency\" : \"USD\",\n" +
        "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
        "    },\n" +
        "    \"Holder\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Flags\" : 1,\n" +
        "    \"Fee\" : \"10\",\n" +
        "    \"Sequence\" : 6,\n" +
        "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "    \"TransactionType\" : \"AMMClawback\"\n" +
        "}";

    assertCanSerializeAndDeserialize(ammClawback, json);
  }

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    AmmClawback ammClawback = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(usd)
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .asset(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .asset2(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .holder(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    assertThat(ammClawback.transactionFlags()).isEqualTo(ammClawback.flags());
    assertThat(ammClawback.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void transactionFlagsReturnsCorrectFlagsWhenFlagsSet() {
    AmmClawback ammClawback = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(usd)
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .asset(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .asset2(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .holder(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(AmmClawbackFlags.CLAW_TWO_ASSETS)
      .build();

    assertThat(ammClawback.transactionFlags()).isEqualTo(ammClawback.flags());
    assertThat(((AmmClawbackFlags) ammClawback.transactionFlags()).tfClawTwoAssets()).isTrue();
  }

  @Test
  void builderFromCopiesFlagsCorrectly() {
    AmmClawback original = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(usd)
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .asset(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .asset2(IouIssue.builder().currency(usd).issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd")).build())
      .holder(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(AmmClawbackFlags.CLAW_TWO_ASSETS)
      .build();

    AmmClawback copied = AmmClawback.builder().from(original).build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
    assertThat(((AmmClawbackFlags) copied.transactionFlags()).tfClawTwoAssets()).isTrue();
  }

  @Test
  void testJsonWithMptAssets() throws JSONException, JsonProcessingException {
    MpTokenIssuanceId mptIssuanceId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");

    AmmClawback ammClawback = AmmClawback.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mptIssuanceId)
          .value("25")
          .build()
      )
      .asset(MptIssue.of(mptIssuanceId))
      .asset2(XrpIssue.builder().build())
      .holder(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json =
      "{\n" +
        "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
        "    \"Amount\" : {\n" +
        "        \"mpt_issuance_id\" : \"00000002430427B80BD2D09D36B70B969E12801065F22308\",\n" +
        "        \"value\" : \"25\"\n" +
        "    },\n" +
        "    \"Asset\" : {\n" +
        "        \"mpt_issuance_id\" : \"00000002430427B80BD2D09D36B70B969E12801065F22308\"\n" +
        "    },\n" +
        "    \"Asset2\" : {\n" +
        "        \"currency\" : \"XRP\"\n" +
        "    },\n" +
        "    \"Holder\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
        "    \"Fee\" : \"10\",\n" +
        "    \"Sequence\" : 6,\n" +
        "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "    \"TransactionType\" : \"AMMClawback\"\n" +
        "}";

    assertCanSerializeAndDeserialize(ammClawback, json);
  }
}
