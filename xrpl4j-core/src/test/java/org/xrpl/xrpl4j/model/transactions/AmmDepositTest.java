package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.AmmDepositFlags;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.Asset;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class AmmDepositTest extends AbstractJsonTest {

  @Test
  void constructLpTokenDepositAndTestJson() throws JSONException, JsonProcessingException {
    AmmDeposit deposit = AmmDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .lpTokenOut(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      ).build();

    assertThat(deposit.flags()).isEqualTo(AmmDepositFlags.LP_TOKEN);

    String json = "{\n" +
      "    \"Account\" : \"" + deposit.account() + "\",\n" +
      "    \"LPTokenOut\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"Asset2\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmDepositFlags.LP_TOKEN + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"TransactionType\" : \"AMMDeposit\"\n" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  void constructTwoAssetDepositAndTestJson() throws JSONException, JsonProcessingException {
    AmmDeposit deposit = AmmDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(10))
      .build();

    assertThat(deposit.flags()).isEqualTo(AmmDepositFlags.TWO_ASSET);

    String json = "{\n" +
      "    \"Account\" : \"" + deposit.account() + "\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"10\"," +
      "    \"Asset2\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmDepositFlags.TWO_ASSET + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"TransactionType\" : \"AMMDeposit\"\n" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  void constructSingleAssetDepositAndTestJson() throws JSONException, JsonProcessingException {
    AmmDeposit deposit = AmmDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .build();

    assertThat(deposit.flags()).isEqualTo(AmmDepositFlags.SINGLE_ASSET);

    String json = "{\n" +
      "    \"Account\" : \"" + deposit.account() + "\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"Asset2\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmDepositFlags.SINGLE_ASSET + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"TransactionType\" : \"AMMDeposit\"\n" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  void constructOneAssetLpTokenDepositAndTestJson() throws JSONException, JsonProcessingException {
    AmmDeposit deposit = AmmDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .lpTokenOut(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .build();

    assertThat(deposit.flags()).isEqualTo(AmmDepositFlags.ONE_ASSET_LP_TOKEN);

    String json = "{\n" +
      "    \"Account\" : \"" + deposit.account() + "\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"LPTokenOut\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"Asset2\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmDepositFlags.ONE_ASSET_LP_TOKEN + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"TransactionType\" : \"AMMDeposit\"\n" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  void constructLimitLpTokenDepositAndTestJson() throws JSONException, JsonProcessingException {
    AmmDeposit deposit = AmmDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .effectivePrice(XrpCurrencyAmount.ofDrops(10))
      .build();

    assertThat(deposit.flags()).isEqualTo(AmmDepositFlags.LIMIT_LP_TOKEN);

    String json = "{\n" +
      "    \"Account\" : \"" + deposit.account() + "\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"EPrice\" : \"10\",\n" +
      "    \"Asset2\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : " + AmmDepositFlags.LIMIT_LP_TOKEN + ",\n" +
      "    \"Sequence\" : 0,\n" +
      "    \"TransactionType\" : \"AMMDeposit\"\n" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @ParameterizedTest
  @MethodSource("getBooleanCombinations")
  void testInvalidFieldPresence(
    boolean lpTokenPresent,
    boolean amountPresent,
    boolean amount2Present,
    boolean effectivePricePresent
  ) {

    ImmutableAmmDeposit.Builder builder = AmmDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      );

    if (lpTokenPresent) {
      builder.lpTokenOut(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      );
    }
    if (amountPresent) {
      builder.amount(XrpCurrencyAmount.ofDrops(10));
    }
    if (amount2Present) {
      builder.amount2(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      );
    }
    if (effectivePricePresent) {
      builder.effectivePrice(XrpCurrencyAmount.ofDrops(10));
    }

    assertThatThrownBy(builder::build)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Correct AmmDepositFlag could not be determined based on set fields.");
  }

  private static Stream<Arguments> getBooleanCombinations() {
    // Every combination of 4 booleans
    List<Object[]> params = new ArrayList<>();
    for (int i = 0; i < Math.pow(2, 4); i++) {
      String bin = Integer.toBinaryString(i);
      while (bin.length() < 4) {
        bin = "0" + bin;
      }

      char[] chars = bin.toCharArray();
      Boolean[] booleans = new Boolean[4];
      for (int j = 0; j < chars.length; j++) {
        booleans[j] = chars[j] == '0';
      }

      if (booleans[0] && !booleans[1] && !booleans[2] && !booleans[3]) {
        continue;
      }
      if (!booleans[0] && booleans[1] && booleans[2] && !booleans[3]) {
        continue;
      }
      if (!booleans[0] && booleans[1] && !booleans[2] && !booleans[3]) {
        continue;
      }
      if (booleans[0] && booleans[1] && !booleans[2] && !booleans[3]) {
        continue;
      }
      if (!booleans[0] && booleans[1] && !booleans[2]) {
        continue;
      }
      params.add(booleans);
    }

    return params.stream().map(Arguments::of);
  }

}