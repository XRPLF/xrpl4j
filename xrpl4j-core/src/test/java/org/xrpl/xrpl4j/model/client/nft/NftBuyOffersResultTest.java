package org.xrpl.xrpl4j.model.client.nft;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;

public class NftBuyOffersResultTest extends AbstractJsonTest {

  @Test
  public void testWithXrpCurrencyAmount() throws JsonProcessingException, JSONException {

    BuyOffer buyOffer = BuyOffer.builder()
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .owner(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .flags(NfTokenOfferFlags.BUY_TOKEN)
      .nftOfferIndex(Hash256.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .build();

    List<BuyOffer> list = new ArrayList<>();
    list.add(buyOffer);

    NftBuyOffersResult params = NftBuyOffersResult.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .offers(list)
      .build();

    String offer = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": \"1000\",\n" +
      "    \"owner\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"nft_offer_index\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\"\n" +
      "}";

    String json = "{\n" +
      "        \"nft_id\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "        \"offers\": [" + offer + "]\n" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithIssuedCurrencyAmount() throws JsonProcessingException, JSONException {

    BuyOffer buyOffer = BuyOffer.builder()
      .amount(IssuedCurrencyAmount.builder()
        .issuer(Address.of("rsjYGpMWQeNBXbUTkVz4ZKzHefgZSr6rys"))
        .currency("USD")
        .value("100")
        .build()
      )
      .owner(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .flags(NfTokenOfferFlags.BUY_TOKEN)
      .nftOfferIndex(Hash256.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .build();

    List<BuyOffer> list = new ArrayList<>();
    list.add(buyOffer);

    NftBuyOffersResult params = NftBuyOffersResult.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .offers(list)
      .build();

    String offer = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": {\n" +
      "      \"issuer\": \"rsjYGpMWQeNBXbUTkVz4ZKzHefgZSr6rys\",\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"owner\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"nft_offer_index\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\"\n" +
      "}";

    String json = "{\n" +
      "        \"nft_id\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "        \"offers\": [" + offer + "]\n" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }
}
