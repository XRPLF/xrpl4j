package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountChannelsResultJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {
    AccountChannelsResult result = AccountChannelsResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addChannels(
        PaymentChannelResultObject.builder()
          .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
          .amount(XrpCurrencyAmount.ofDrops(100000000))
          .balance(XrpCurrencyAmount.ofDrops(0))
          .channelId(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
          .destinationAccount(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .destinationTag(UnsignedInteger.valueOf(20170428))
          .publicKey("aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3")
          .publicKeyHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6")
          .expiration(UnsignedLong.valueOf(10000))
          .cancelAfter(UnsignedLong.valueOf(10000))
          .sourceTag(UnsignedInteger.valueOf(10000))
          .settleDelay(UnsignedInteger.valueOf(86400))
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"channels\": [{\n" +
      "            \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "            \"amount\": \"100000000\",\n" +
      "            \"balance\": \"0\",\n" +
      "            \"channel_id\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
      "            \"destination_account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "            \"destination_tag\": 20170428,\n" +
      "            \"public_key\": \"aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3\",\n" +
      "            \"public_key_hex\": \"023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6\",\n" +
      "            \"expiration\": 10000,\n" +
      "            \"cancel_after\": 10000,\n" +
      "            \"source_tag\": 10000,\n" +
      "            \"settle_delay\": 86400\n" +
      "        }],\n" +
      "        \"ledger_hash\": \"B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D\",\n" +
      "        \"ledger_index\": 37230600,\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {
    AccountChannelsResult result = AccountChannelsResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .limit(UnsignedInteger.valueOf(10))
      .marker(Marker.of("marker"))
      .validated(true)
      .addChannels(
        PaymentChannelResultObject.builder()
          .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
          .amount(XrpCurrencyAmount.ofDrops(100000000))
          .balance(XrpCurrencyAmount.ofDrops(0))
          .channelId(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
          .destinationAccount(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .destinationTag(UnsignedInteger.valueOf(20170428))
          .publicKey("aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3")
          .publicKeyHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6")
          .expiration(UnsignedLong.valueOf(10000))
          .cancelAfter(UnsignedLong.valueOf(10000))
          .sourceTag(UnsignedInteger.valueOf(10000))
          .settleDelay(UnsignedInteger.valueOf(86400))
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"channels\": [{\n" +
      "            \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "            \"amount\": \"100000000\",\n" +
      "            \"balance\": \"0\",\n" +
      "            \"channel_id\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
      "            \"destination_account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "            \"destination_tag\": 20170428,\n" +
      "            \"public_key\": \"aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3\",\n" +
      "            \"public_key_hex\": \"023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6\",\n" +
      "            \"expiration\": 10000,\n" +
      "            \"cancel_after\": 10000,\n" +
      "            \"source_tag\": 10000,\n" +
      "            \"settle_delay\": 86400\n" +
      "        }],\n" +
      "        \"ledger_hash\": \"B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D\",\n" +
      "        \"ledger_index\": 37230600,\n" +
      "        \"status\": \"success\",\n" +
      "        \"limit\": 10,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
