package org.xrpl.xrpl4j.model.client.amm;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class AmmInfoResultTest extends AbstractJsonTest {

  @Test
  void testJsonForCurrentLedger() throws JSONException, JsonProcessingException {
    AmmInfoResult result = AmmInfoResult.builder()
      .amm(
        AmmInfo.builder()
          .account(Address.of("rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze"))
          .amount(XrpCurrencyAmount.ofDrops(11080000720L))
          .amount2(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rELH2VCCkjDzvygtB4nKiqGav7h53RhDiP"))
              .value("11080.00072727936")
              .build()
          )
          .auctionSlot(
            AmmInfoAuctionSlot.builder()
              .account(Address.of("rM7xXGzMUALmEhQ2y9FW5XG69WXwQ6xtDC"))
              .addAuthAccounts(
                AmmInfoAuthAccount.of(Address.of("rHq1eC9TEyEPVhRvdTPLKr3z8D5BUzcHqi")),
                AmmInfoAuthAccount.of(Address.of("rNzgpEGUyEmQ1YGDMAiGGBvwtzbk78tcCG"))
              )
              .discountedFee(TradingFee.of(UnsignedInteger.ZERO))
              .expiration(
                ZonedDateTime.parse(
                  "2023-07-20T15:17:31+0000",
                  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                )
              )
              .price(
                IssuedCurrencyAmount.builder()
                  .currency("03930D02208264E2E40EC1B0C09E4DB96EE197B1")
                  .issuer(Address.of("rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze"))
                  .value("100")
                  .build()
              )
              .timeInterval(UnsignedInteger.ZERO)
              .build()
          )
          .lpToken(
            IssuedCurrencyAmount.builder()
              .currency("03930D02208264E2E40EC1B0C09E4DB96EE197B1")
              .issuer(Address.of("rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze"))
              .value("11079900")
              .build()
          )
          .tradingFee(TradingFee.of(UnsignedInteger.valueOf(225)))
          .addVoteSlots(
            AmmInfoVoteEntry.builder()
              .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(90)))
              .tradingFee(TradingFee.of(UnsignedInteger.valueOf(50)))
              .account(Address.of("rs6HZNabrZzBBjDWCwkWcSGdDH7Xsi4Z99"))
              .build(),
            AmmInfoVoteEntry.builder()
              .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(90)))
              .tradingFee(TradingFee.of(UnsignedInteger.valueOf(100)))
              .account(Address.of("rJd7rhLSaqLHEfeqAW2vYzYYkhvyE9XfBE"))
              .build()
          )
          .build()
      )
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(102)))
      .status("success")
      .build();

    String json = "{\"amm\": {\"account\": \"rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze\",\n" +
      "      \"amount\": \"11080000720\",\n" +
      "      \"amount2\": {\"currency\": \"USD\",\n" +
      "      \"issuer\": \"rELH2VCCkjDzvygtB4nKiqGav7h53RhDiP\",\n" +
      "      \"value\": \"11080.00072727936\"},\n" +
      "      \"asset2_frozen\": false,\n" +
      "      \"asset_frozen\": false,\n" +
      "        \"auction_slot\": {\"account\": \"rM7xXGzMUALmEhQ2y9FW5XG69WXwQ6xtDC\",\n" +
      "        \"auth_accounts\": [{\"account\": \"rHq1eC9TEyEPVhRvdTPLKr3z8D5BUzcHqi\"},\n" +
      "        {\"account\": \"rNzgpEGUyEmQ1YGDMAiGGBvwtzbk78tcCG\"}],\n" +
      "        \"discounted_fee\": 0,\n" +
      "          \"expiration\": \"2023-07-20T15:17:31+0000\",\n" +
      "          \"price\": {\"currency\": \"03930D02208264E2E40EC1B0C09E4DB96EE197B1\",\n" +
      "          \"issuer\": \"rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze\",\n" +
      "          \"value\": \"100\"},\n" +
      "        \"time_interval\": 0},\n" +
      "      \"lp_token\": {\"currency\": \"03930D02208264E2E40EC1B0C09E4DB96EE197B1\",\n" +
      "        \"issuer\": \"rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze\",\n" +
      "        \"value\": \"11079900\"},\n" +
      "      \"trading_fee\": 225,\n" +
      "        \"vote_slots\": [{\"account\": \"rs6HZNabrZzBBjDWCwkWcSGdDH7Xsi4Z99\",\n" +
      "        \"trading_fee\": 50,\n" +
      "        \"vote_weight\": 90},\n" +
      "      {\"account\": \"rJd7rhLSaqLHEfeqAW2vYzYYkhvyE9XfBE\",\n" +
      "        \"trading_fee\": 100,\n" +
      "        \"vote_weight\": 90}]},\n" +
      "      \"ledger_current_index\": 102,\n" +
      "      \"status\": \"success\",\n" +
      "      \"validated\": false}";

    assertCanSerializeAndDeserialize(result, json);

    assertThat(result.ledgerCurrentIndexSafe()).isEqualTo(result.ledgerCurrentIndex().get());
    assertThatThrownBy(result::ledgerIndexSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerHashSafe).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testJsonForValidatedLedger() throws JSONException, JsonProcessingException {
    AmmInfoResult result = AmmInfoResult.builder()
      .amm(
        AmmInfo.builder()
          .account(Address.of("rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze"))
          .amount(XrpCurrencyAmount.ofDrops(11080000720L))
          .amount2(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rELH2VCCkjDzvygtB4nKiqGav7h53RhDiP"))
              .value("11080.00072727936")
              .build()
          )
          .asset2Frozen(false)
          .auctionSlot(
            AmmInfoAuctionSlot.builder()
              .account(Address.of("rM7xXGzMUALmEhQ2y9FW5XG69WXwQ6xtDC"))
              .addAuthAccounts(
                AmmInfoAuthAccount.of(Address.of("rHq1eC9TEyEPVhRvdTPLKr3z8D5BUzcHqi")),
                AmmInfoAuthAccount.of(Address.of("rNzgpEGUyEmQ1YGDMAiGGBvwtzbk78tcCG"))
              )
              .discountedFee(TradingFee.of(UnsignedInteger.ZERO))
              .expiration(
                ZonedDateTime.parse(
                  "2023-07-20T15:17:31+0000",
                  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                )
              )
              .price(
                IssuedCurrencyAmount.builder()
                  .currency("03930D02208264E2E40EC1B0C09E4DB96EE197B1")
                  .issuer(Address.of("rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze"))
                  .value("100")
                  .build()
              )
              .timeInterval(UnsignedInteger.ZERO)
              .build()
          )
          .lpToken(
            IssuedCurrencyAmount.builder()
              .currency("03930D02208264E2E40EC1B0C09E4DB96EE197B1")
              .issuer(Address.of("rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze"))
              .value("11079900")
              .build()
          )
          .tradingFee(TradingFee.of(UnsignedInteger.valueOf(225)))
          .addVoteSlots(
            AmmInfoVoteEntry.builder()
              .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(90)))
              .tradingFee(TradingFee.of(UnsignedInteger.valueOf(50)))
              .account(Address.of("rs6HZNabrZzBBjDWCwkWcSGdDH7Xsi4Z99"))
              .build(),
            AmmInfoVoteEntry.builder()
              .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(90)))
              .tradingFee(TradingFee.of(UnsignedInteger.valueOf(100)))
              .account(Address.of("rJd7rhLSaqLHEfeqAW2vYzYYkhvyE9XfBE"))
              .build()
          )
          .build()
      )
      .ledgerHash(Hash256.of("93586177048F82080AB79B8D0FA76F9D93AF458551A7358D9F0EC6D790AF5CBA"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(102)))
      .status("success")
      .validated(true)
      .build();

    String json = "{\"amm\": {\"account\": \"rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze\",\n" +
      "      \"amount\": \"11080000720\",\n" +
      "      \"amount2\": {\"currency\": \"USD\",\n" +
      "      \"issuer\": \"rELH2VCCkjDzvygtB4nKiqGav7h53RhDiP\",\n" +
      "      \"value\": \"11080.00072727936\"},\n" +
      "      \"asset2_frozen\": false,\n" +
      "      \"asset_frozen\": false,\n" +
      "        \"auction_slot\": {\"account\": \"rM7xXGzMUALmEhQ2y9FW5XG69WXwQ6xtDC\",\n" +
      "        \"auth_accounts\": [{\"account\": \"rHq1eC9TEyEPVhRvdTPLKr3z8D5BUzcHqi\"},\n" +
      "        {\"account\": \"rNzgpEGUyEmQ1YGDMAiGGBvwtzbk78tcCG\"}],\n" +
      "        \"discounted_fee\": 0,\n" +
      "          \"expiration\": \"2023-07-20T15:17:31+0000\",\n" +
      "          \"price\": {\"currency\": \"03930D02208264E2E40EC1B0C09E4DB96EE197B1\",\n" +
      "          \"issuer\": \"rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze\",\n" +
      "          \"value\": \"100\"},\n" +
      "        \"time_interval\": 0},\n" +
      "      \"lp_token\": {\"currency\": \"03930D02208264E2E40EC1B0C09E4DB96EE197B1\",\n" +
      "        \"issuer\": \"rU3auoTuhaPwiiod3wEXNnYogxMnYsBhze\",\n" +
      "        \"value\": \"11079900\"},\n" +
      "      \"trading_fee\": 225,\n" +
      "        \"vote_slots\": [{\"account\": \"rs6HZNabrZzBBjDWCwkWcSGdDH7Xsi4Z99\",\n" +
      "        \"trading_fee\": 50,\n" +
      "        \"vote_weight\": 90},\n" +
      "      {\"account\": \"rJd7rhLSaqLHEfeqAW2vYzYYkhvyE9XfBE\",\n" +
      "        \"trading_fee\": 100,\n" +
      "        \"vote_weight\": 90}]},\n" +
      "      \"ledger_hash\": \"93586177048F82080AB79B8D0FA76F9D93AF458551A7358D9F0EC6D790AF5CBA\",\n" +
      "      \"ledger_index\": 102,\n" +
      "      \"status\": \"success\",\n" +
      "      \"validated\": true}";

    assertCanSerializeAndDeserialize(result, json);

    assertThat(result.ledgerIndexSafe()).isEqualTo(result.ledgerIndex().get());
    assertThat(result.ledgerHashSafe()).isEqualTo(result.ledgerHash().get());
    assertThatThrownBy(result::ledgerCurrentIndexSafe).isInstanceOf(IllegalStateException.class);
  }
}
