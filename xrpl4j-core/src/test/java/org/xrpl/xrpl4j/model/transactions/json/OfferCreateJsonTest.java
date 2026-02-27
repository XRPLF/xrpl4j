package org.xrpl.xrpl4j.model.transactions.json;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class OfferCreateJsonTest extends AbstractJsonTest {

  @Test
  public void testOfferCreateJson() throws JsonProcessingException, JSONException {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .expiration(UnsignedInteger.valueOf(16))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{" +
      "  \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\"," +
      "  \"TransactionType\": \"OfferCreate\"," +
      "  \"Sequence\": 1," +
      "  \"OfferSequence\": 13," +
      "  \"TakerPays\": \"14\"," +
      "  \"TakerGets\": \"15\"," +
      "  \"Fee\": \"12\"," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"DomainID\": \"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"Expiration\": 16" +
      "}";

    assertCanSerializeAndDeserialize(offerCreate, json);
  }

  @Test
  public void testOfferCreateJsonWithFlags() throws JsonProcessingException, JSONException {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .expiration(UnsignedInteger.valueOf(16))
      .flags(OfferCreateFlags.builder().tfSell(true).tfHybrid(true).build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\"," +
      "  \"TransactionType\": \"OfferCreate\"," +
      "  \"Sequence\": 1," +
      "  \"OfferSequence\": 13," +
      "  \"TakerPays\": \"14\"," +
      "  \"TakerGets\": \"15\"," +
      "  \"Fee\": \"12\"," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"DomainID\": \"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"Expiration\": 16" +
      "}", 2149056512L);

    assertCanSerializeAndDeserialize(offerCreate, json);
  }

  @Test
  public void testOfferCreateJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .expiration(UnsignedInteger.valueOf(16))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Foo\" : \"Bar\"," +
      "  \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\"," +
      "  \"TransactionType\": \"OfferCreate\"," +
      "  \"Sequence\": 1," +
      "  \"OfferSequence\": 13," +
      "  \"TakerPays\": \"14\"," +
      "  \"TakerGets\": \"15\"," +
      "  \"Fee\": \"12\"," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"DomainID\": \"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"Expiration\": 16" +
      "}";

    assertCanSerializeAndDeserialize(offerCreate, json);
  }

}