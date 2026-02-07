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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class PaymentChannelFundTest extends AbstractJsonTest {

  @Test
  public void testPaymentChannelFundJson() throws JsonProcessingException, JSONException {
    PaymentChannelFund fund = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .expiration(UnsignedLong.valueOf(543171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelFund\"," +
      "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\"," +
      "  \"Amount\": \"200000\"," +
      "  \"NetworkID\": 1024," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Expiration\": 543171558" +
      "}";

    assertCanSerializeAndDeserialize(fund, json);
  }

  @Test
  public void testPaymentChannelFundJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    PaymentChannelFund fund = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .expiration(UnsignedLong.valueOf(543171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelFund\"," +
      "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\"," +
      "  \"Amount\": \"200000\"," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Expiration\": 543171558" +
      "}";

    assertCanSerializeAndDeserialize(fund, json);
  }

  @Test
  public void testPaymentChannelFundJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    PaymentChannelFund fund = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .expiration(UnsignedLong.valueOf(543171558))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelFund\"," +
      "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\"," +
      "  \"Amount\": \"200000\"," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Expiration\": 543171558" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(fund, json);
  }

  @Test
  public void testPaymentChannelFundJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    PaymentChannelFund fund = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .expiration(UnsignedLong.valueOf(543171558))
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
      "  \"TransactionType\": \"PaymentChannelFund\"," +
      "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\"," +
      "  \"Amount\": \"200000\"," +
      "  \"NetworkID\": 1024," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Expiration\": 543171558" +
      "}";

    assertCanSerializeAndDeserialize(fund, json);
  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    PaymentChannelFund fund = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .build();

    assertThat(fund.transactionFlags()).isEqualTo(fund.flags());
    assertThat(fund.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    PaymentChannelFund original = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .build();

    PaymentChannelFund copied = PaymentChannelFund.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}
