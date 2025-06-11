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
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class PaymentChannelCreateJsonTest extends AbstractJsonTest {

  @Test
  public void testPaymentChannelCreateJson() throws JsonProcessingException, JSONException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.valueOf(11747))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .settleDelay(UnsignedInteger.valueOf(86400))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"SettleDelay\": 86400," +
      "  \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\"," +
      "  \"CancelAfter\": 533171558," +
      "  \"DestinationTag\": 23480," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"SourceTag\": 11747" +
      "}";

    assertCanSerializeAndDeserialize(create, json);
  }

  @Test
  public void testPaymentChannelCreateJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.valueOf(11747))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .settleDelay(UnsignedInteger.valueOf(86400))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"SettleDelay\": 86400," +
      "  \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\"," +
      "  \"CancelAfter\": 533171558," +
      "  \"DestinationTag\": 23480," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"SourceTag\": 11747" +
      "}";

    assertCanSerializeAndDeserialize(create, json);
  }

  @Test
  public void testPaymentChannelCreateJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.valueOf(11747))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .settleDelay(UnsignedInteger.valueOf(86400))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"SettleDelay\": 86400," +
      "  \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\"," +
      "  \"CancelAfter\": 533171558," +
      "  \"DestinationTag\": 23480," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"SourceTag\": 11747" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(create, json);
  }

  @Test
  public void testPaymentChannelCreateJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.valueOf(11747))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .settleDelay(UnsignedInteger.valueOf(86400))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Foo\" : \"Bar\"," +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"SettleDelay\": 86400," +
      "  \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\"," +
      "  \"CancelAfter\": 533171558," +
      "  \"DestinationTag\": 23480," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"SourceTag\": 11747" +
      "}";

    assertCanSerializeAndDeserialize(create, json);
  }
}
