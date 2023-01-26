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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.CryptoConditionReader;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class EscrowJsonTests extends AbstractJsonTest {

  @Test
  public void testEscrowCreateJson() throws JsonProcessingException, JSONException, DerEncodingException {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .finishAfter(UnsignedLong.valueOf(533171558))
      .condition(CryptoConditionReader.readCondition(
        BaseEncoding.base16()
          .decode("A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"))
      )
      .sourceTag(UnsignedInteger.valueOf(11747))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCreate\",\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"CancelAfter\": 533257958,\n" +
      "    \"FinishAfter\": 533171558,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"DestinationTag\": 23480,\n" +
      "    \"SourceTag\": 11747,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowCreate, json);
  }

  @Test
  public void testEscrowCancelJson() throws JsonProcessingException, JSONException {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCancel\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"12\"\n" +
      "}";
    assertCanSerializeAndDeserialize(escrowCancel, json);
  }

  @Test
  public void testEscrowFinishJson() throws JsonProcessingException, JSONException, DerEncodingException {
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(330))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .condition(CryptoConditionReader.readCondition(BaseEncoding.base16().decode(
        "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"))
      )
      .fulfillment(CryptoConditionReader.readFulfillment(BaseEncoding.base16().decode("A0028000")))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"A0028000\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"330\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowFinish, json);
  }

  @Test
  public void testEscrowFinishJsonWithFeeTooLow() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowFinish.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(3))
        .sequence(UnsignedInteger.ONE)
        .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .offerSequence(UnsignedInteger.valueOf(7))
        .condition(PreimageSha256Fulfillment.from(new byte[48]).getDerivedCondition())
        .fulfillment(PreimageSha256Fulfillment.from(new byte[60]))
        .build(),
      "If a fulfillment is specified, the fee must be set to 330 or greater."
    );
  }
}
