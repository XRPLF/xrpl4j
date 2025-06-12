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
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class EscrowCreateJsonTest extends AbstractJsonTest {

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

    String json = "{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"TransactionType\": \"EscrowCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"CancelAfter\": 533257958," +
      "  \"FinishAfter\": 533171558," +
      "  \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\"," +
      "  \"DestinationTag\": 23480," +
      "  \"SourceTag\": 11747," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"Fee\": \"12\"" +
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

    String json = "{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"TransactionType\": \"EscrowCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"CancelAfter\": 533257958," +
      "  \"FinishAfter\": 533171558," +
      "  \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\"," +
      "  \"DestinationTag\": 23480," +
      "  \"SourceTag\": 11747," +
      "  \"Sequence\": 1," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Fee\": \"12\"" +
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

    String json = String.format("{" +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"TransactionType\": \"EscrowCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"CancelAfter\": 533257958," +
      "  \"FinishAfter\": 533171558," +
      "  \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\"," +
      "  \"DestinationTag\": 23480," +
      "  \"SourceTag\": 11747," +
      "  \"Sequence\": 1," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Fee\": \"12\"" +
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

    String json = "{" +
      "  \"Foo\" : \"Bar\"," +
      "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "  \"TransactionType\": \"EscrowCreate\"," +
      "  \"Amount\": \"10000\"," +
      "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"CancelAfter\": 533257958," +
      "  \"FinishAfter\": 533171558," +
      "  \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\"," +
      "  \"DestinationTag\": 23480," +
      "  \"SourceTag\": 11747," +
      "  \"Sequence\": 1," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"NetworkID\": 1024," +
      "  \"Fee\": \"12\"" +
      "}";

    assertCanSerializeAndDeserialize(escrowCreate, json);
  }
}
