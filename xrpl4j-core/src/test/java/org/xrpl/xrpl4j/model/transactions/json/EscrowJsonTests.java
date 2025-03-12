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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.CryptoConditionReader;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
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
      .networkId(NetworkId.of(1024))
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
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowCreate, json);
  }

  @Test
  public void testEscrowCreateJsonWithUnsetFlags() throws JsonProcessingException, JSONException, DerEncodingException {
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
      .flags(TransactionFlags.UNSET)
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
      "    \"Flags\": 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowCreate, json);
  }

  @Test
  public void testEscrowCreateJsonWithNonZeroFlags()
    throws JsonProcessingException, JSONException, DerEncodingException {
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
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{\n" +
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
      "    \"Flags\": %s,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"12\"\n" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(escrowCreate, json);
  }

  @Test
  public void testEscrowCreateJsonWithUnknownFields()
    throws JsonProcessingException, JSONException, DerEncodingException {
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
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
      "    \"Foo\" : \"Bar\",\n" +
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
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NetworkID\": 1024,\n" +
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
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCancel\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";
    assertCanSerializeAndDeserialize(escrowCancel, json);
  }

  @Test
  public void testEscrowCancelJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCancel\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"12\"\n" +
      "}";
    assertCanSerializeAndDeserialize(escrowCancel, json);
  }

  @Test
  public void testEscrowCancelJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCancel\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": %s,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"12\"\n" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());
    assertCanSerializeAndDeserialize(escrowCancel, json);
  }

  @Test
  public void testEscrowCancelJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
      "    \"Foo\" : \"Bar\",\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCancel\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NetworkID\": 1024,\n" +
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
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"A0028000\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"Fee\": \"330\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowFinish, json);
  }

  @Test
  public void testEscrowFinishJsonWithUnsetFlags() throws JsonProcessingException, JSONException, DerEncodingException {
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
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"A0028000\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 0,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"330\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowFinish, json);
  }

  @Test
  public void testEscrowFinishJsonWithNonZeroFlags()
    throws JsonProcessingException, JSONException, DerEncodingException {
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
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"A0028000\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": %s,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"330\"\n" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(escrowFinish, json);
  }

  @Test
  void testEscrowFinishJsonWithMalformedCondition() throws JsonProcessingException {
    String json = "{\n" +
      "  \"Account\": \"rKWZ2fDqE5B9XorAcEQZD46H6HEdJQVNdb\",\n" +
      "  \"Condition\": \"A02580209423ED2EF4CACA8CA4AFC08D3F5EC60A545FD7A97E66E16EA0E2E2\",\n" +
      "  \"Fee\": \"563\",\n" +
      "  \"Fulfillment\": \"A02280203377850F1B3A4322F1562DF6F75D584596ABE5B9C76EEA8301F56CB942ACC69B\",\n" +
      "  \"LastLedgerSequence\": 40562057,\n" +
      "  \"OfferSequence\": 40403748,\n" +
      "  \"Owner\": \"r3iocgQwoGNCYyvvt8xuWv2XYXx7Z2gQqd\",\n" +
      "  \"Sequence\": 39899485,\n" +
      "  \"SigningPubKey\": \"ED09285829A011D520A1873A0E2F1014F5D6B66A6DDE6953FC02C8185EAFA6A1B0\",\n" +
      "  \"TransactionType\": \"EscrowFinish\",\n" +
      "  \"TxnSignature\": \"A3E64F6B8D1D7C4FBC9663FCD217F86C3529EC2E2F16442DD48D1B66EEE30EAC2CE960A76080F74BC749" +
      "8CA7BBFB822BEE9E8F767114D7B54F7403A7CB672501\"\n" +
      "}";

    EscrowFinish escrowFinish = objectMapper.readValue(json, EscrowFinish.class);
    assertThat(escrowFinish.condition()).isEmpty();
    assertThat(escrowFinish.conditionRawValue()).isNotEmpty().get()
      .isEqualTo("A02580209423ED2EF4CACA8CA4AFC08D3F5EC60A545FD7A97E66E16EA0E2E2");
  }

  @Test
  void testEscrowFinishJsonWithMalformedFulfillment() throws JsonProcessingException {
    String json = String.format("{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"123\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": %s,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Fee\": \"330\"\n" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    EscrowFinish escrowFinish = objectMapper.readValue(json, EscrowFinish.class);
    assertThat(escrowFinish.fulfillment()).isEmpty();
    assertThat(escrowFinish.fulfillmentRawValue()).isNotEmpty().get()
      .isEqualTo("123");
  }

  @Test
  public void testEscrowFinishJsonWithUnknownFields()
    throws JsonProcessingException, JSONException, DerEncodingException {
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
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
      "    \"Foo\" : \"Bar\",\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"A0028000\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"Fee\": \"330\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowFinish, json);
  }
}
