package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.AmmWithdrawFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;

class AmmWithdrawTest extends AbstractJsonTest {

  @Test
  void constructLpTokenWithdrawAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.LP_TOKEN)
      .lpTokensIn(lpTokensIn())
      
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"LPTokensIn\" : " + objectMapper.writeValueAsString(withdraw.lpTokensIn()) + "," +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.LP_TOKEN + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructWithdrawAllAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.WITHDRAW_ALL)
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.WITHDRAW_ALL + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructTwoAssetAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.TWO_ASSET)
      .amount(amount())
      .amount2(XrpCurrencyAmount.ofDrops(50000000))
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"5\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"50000000\"," +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.TWO_ASSET + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructSingleAssetAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.SINGLE_ASSET)
      .amount(amount())
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"5\"\n" +
      "    },\n" +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.SINGLE_ASSET + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructOneAssetWithdrawAllAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL)
      .amount(amount())
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"5\"\n" +
      "    },\n" +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructOneAssetLpTokenAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.ONE_ASSET_LP_TOKEN)
      .amount(amount())
      .lpTokensIn(lpTokensIn())
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"5\"\n" +
      "    },\n" +
      "    \"LPTokensIn\" : " + objectMapper.writeValueAsString(withdraw.lpTokensIn()) + "," +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.ONE_ASSET_LP_TOKEN + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructLimitLpTokenAndTestJson() throws JSONException, JsonProcessingException {
    AmmWithdraw withdraw = baseBuilder()
      .flags(AmmWithdrawFlags.LIMIT_LP_TOKEN)
      .amount(amount())
      .effectivePrice(amount())
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"5\"\n" +
      "    },\n" +
      "    \"EPrice\" : " + objectMapper.writeValueAsString(withdraw.effectivePrice()) + "," +
      "    \"Asset\" : " + objectMapper.writeValueAsString(withdraw.asset()) + "," +
      "    \"Asset2\" : " + objectMapper.writeValueAsString(withdraw.asset2()) + "," +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmWithdrawFlags.LIMIT_LP_TOKEN + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMWithdraw\"\n" +
      "}";

    assertCanSerializeAndDeserialize(withdraw, json);
  }

  @Test
  void constructLpTokenWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LP_TOKEN)
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, amount, amount2, and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .amount(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, amount, amount2, and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, amount, amount2, and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, amount, amount2, and effectivePrice cannot be present.");
  }

  @Test
  void constructWithdrawAllWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.WITHDRAW_ALL)
        .lpTokensIn(lpTokensIn())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, lpTokensIn, amount, amount2, and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.WITHDRAW_ALL)
        .amount(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, lpTokensIn, amount, amount2, and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.WITHDRAW_ALL)
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, lpTokensIn, amount, amount2, and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.WITHDRAW_ALL)
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLPToken flag is set, lpTokensIn, amount, amount2, and effectivePrice cannot be present.");
  }

  @Test
  void constructTwoAssetWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.TWO_ASSET)
        .lpTokensIn(lpTokensIn())
        .amount(amount())
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfTwoAsset flag is set, lpTokensIn and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.TWO_ASSET)
        .amount(amount())
        .amount2(amount())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfTwoAsset flag is set, lpTokensIn and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.TWO_ASSET)
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfTwoAsset flag is set, lpTokensIn and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.TWO_ASSET)
        .amount(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfTwoAsset flag is set, lpTokensIn and effectivePrice cannot be present.");
  }

  @Test
  void constructSingleAssetWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.SINGLE_ASSET)
        .amount(amount())
        .lpTokensIn(lpTokensIn())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.SINGLE_ASSET)
        .amount(amount())
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.SINGLE_ASSET)
        .amount(amount())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.SINGLE_ASSET)
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");
  }

  @Test
  void constructOneAssetWithdrawAllWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL)
        .amount(amount())
        .lpTokensIn(lpTokensIn())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL)
        .amount(amount())
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL)
        .amount(amount())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL)
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfSingleAsset or tfOneAssetWithdrawAll flag is set, lpTokensIn, amount2, and effectivePrice" +
        " cannot be present.");
  }

  @Test
  void constructOneAssetLpTokenWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .amount(amount())
        .amount2(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfOneAssetLPToken flag is set, amount2 and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .amount(amount())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfOneAssetLPToken flag is set, amount2 and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_LP_TOKEN)
        .amount(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfOneAssetLPToken flag is set, amount2 and effectivePrice cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.ONE_ASSET_LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfOneAssetLPToken flag is set, amount2 and effectivePrice cannot be present.");
  }

  @Test
  void constructLimitLpTokenWithWrongFieldsPresent() {
    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LIMIT_LP_TOKEN)
        .lpTokensIn(lpTokensIn())
        .amount(amount())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLimitLPToken flag is set, lpTokensIn and amount2 cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LIMIT_LP_TOKEN)
        .amount(amount())
        .amount2(amount())
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLimitLPToken flag is set, lpTokensIn and amount2 cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LIMIT_LP_TOKEN)
        .amount(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLimitLPToken flag is set, lpTokensIn and amount2 cannot be present.");

    assertThatThrownBy(
      () -> baseBuilder()
        .flags(AmmWithdrawFlags.LIMIT_LP_TOKEN)
        .effectivePrice(amount())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("If the tfLimitLPToken flag is set, lpTokensIn and amount2 cannot be present.");
  }

  private ImmutableIssuedCurrencyAmount amount() {
    return IssuedCurrencyAmount.builder()
      .currency("TST")
      .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
      .value("5")
      .build();
  }

  private ImmutableIssuedCurrencyAmount lpTokensIn() {
    return IssuedCurrencyAmount.builder()
      .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
      .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
      .value("100")
      .build();
  }

  private ImmutableAmmWithdraw.Builder baseBuilder() {
    return AmmWithdraw.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(
        Issue.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      ).asset2(Issue.XRP);
  }
}