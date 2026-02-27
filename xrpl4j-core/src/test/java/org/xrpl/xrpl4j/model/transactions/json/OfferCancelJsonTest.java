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
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class OfferCancelJsonTest extends AbstractJsonTest {

  @Test
  public void testOfferCancelJson() throws JsonProcessingException, JSONException {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .sequence(UnsignedInteger.valueOf(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .fee(XrpCurrencyAmount.ofDrops(14))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{" +
      "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\"," +
      "  \"TransactionType\": \"OfferCancel\"," +
      "  \"Sequence\": 12," +
      "  \"OfferSequence\": 13," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"Fee\": \"14\"" +
      "}";

    assertCanSerializeAndDeserialize(offerCancel, json);
  }

  @Test
  public void testOfferCancelJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .sequence(UnsignedInteger.valueOf(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .fee(XrpCurrencyAmount.ofDrops(14))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\"," +
      "  \"TransactionType\": \"OfferCancel\"," +
      "  \"Sequence\": 12," +
      "  \"OfferSequence\": 13," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Fee\": \"14\"" +
      "}";

    assertCanSerializeAndDeserialize(offerCancel, json);
  }

  @Test
  public void testOfferCancelJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .sequence(UnsignedInteger.valueOf(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .fee(XrpCurrencyAmount.ofDrops(14))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\"," +
      "  \"TransactionType\": \"OfferCancel\"," +
      "  \"Sequence\": 12," +
      "  \"OfferSequence\": 13," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Fee\": \"14\"" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(offerCancel, json);
  }

  @Test
  public void testOfferCancelJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .sequence(UnsignedInteger.valueOf(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .fee(XrpCurrencyAmount.ofDrops(14))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Foo\" : \"Bar\"," +
      "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\"," +
      "  \"TransactionType\": \"OfferCancel\"," +
      "  \"Sequence\": 12," +
      "  \"OfferSequence\": 13," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"Fee\": \"14\"" +
      "}";

    assertCanSerializeAndDeserialize(offerCancel, json);
  }
}