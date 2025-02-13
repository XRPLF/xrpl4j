package org.xrpl.xrpl4j.model.transactions.json;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.model.AddressConstants.ACCOUNT_ZERO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.ImmutableUnlModify;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.UnlModify;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;
import java.util.Map;

public class UnlModifyJsonTests extends AbstractJsonTest {

  @Test
  public void testJsonWithAccountZero() throws JsonProcessingException, JSONException {
    UnlModify unlModify = UnlModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    String json = "{" +
      "\"Account\":\"" + UnlModify.ACCOUNT_ZERO + "\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";

    assertCanSerializeAndDeserialize(unlModify, json);
  }

  @Test
  public void testJsonWithAccountZeroAndUnknownFields() throws JsonProcessingException, JSONException {
    UnlModify unlModify = UnlModify.builder()
      .putUnknownFields("Foo", "Bar")
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    String json = "{" +
      "\"Foo\" : \"Bar\",\n" +
      "\"Account\":\"" + UnlModify.ACCOUNT_ZERO + "\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";

    assertCanSerializeAndDeserialize(unlModify, json);
  }

  @Test
  public void testJsonWithEmptyAccount() throws JsonProcessingException, JSONException {
    UnlModify unlModify = UnlModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    // What we expect the ObjectMapper to Serialize (given the above object)
    String expectedSerializedJson = "{" +
      "\"Account\":\"" + ACCOUNT_ZERO + "\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"" +
      "}";

    String serialized = objectMapper.writeValueAsString(unlModify);
    JSONAssert.assertEquals(expectedSerializedJson, serialized, JSONCompareMode.STRICT);

    // What we provide the ObjectMapper to Deserialize
    String expectedDeserializedJson = "{" +
      "\"Account\":\"\"," + // <-- The crux of the test!
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"" +
      "}";

    Transaction deserialized = objectMapper.readValue(expectedDeserializedJson, Transaction.class);
    assertThat(deserialized).isEqualTo(unlModify);
  }

  @Test
  public void testJsonWithEmptyAccountAndUnknownFields() throws JsonProcessingException, JSONException {
    UnlModify unlModify = UnlModify.builder()
      .putUnknownFields("Foo", "Bar")
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    // What we expect the ObjectMapper to Serialize (given the above object)
    String expectedSerializedJson = "{" +
      "\"Account\":\"" + ACCOUNT_ZERO + "\"," +
      "\"Foo\" : \"Bar\",\n" + // <-- The crux of the test!
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"" +
      "}";

    String serialized = objectMapper.writeValueAsString(unlModify);
    JSONAssert.assertEquals(expectedSerializedJson, serialized, JSONCompareMode.STRICT);

    // What we provide the ObjectMapper to Deserialize
    String expectedDeserializedJson = "{" +
      "\"Foo\" : \"Bar\",\n" + // <-- The crux of the test!
      "\"Account\":\"\"," + // <-- The crux of the test!
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"" +
      "}";

    Transaction deserialized = objectMapper.readValue(expectedDeserializedJson, Transaction.class);
    assertThat(deserialized).isEqualTo(unlModify);
  }

  @Test
  public void testJsonWithMissingAccount() throws JsonProcessingException, JSONException {
    UnlModify unlModify = UnlModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    String expectedJson = "{" +
      "\"Account\":\"" + ACCOUNT_ZERO + "\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";

    String serialized = objectMapper.writeValueAsString(unlModify);
    JSONAssert.assertEquals(expectedJson, serialized, JSONCompareMode.STRICT);

    String jsonForDeserialization = "{" +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";

    Transaction deserialized = objectMapper.readValue(jsonForDeserialization, Transaction.class);
    assertThat(deserialized).isEqualTo(unlModify);
  }

  @Test
  public void testJsonWithMissingAccountAndUnknownFields() throws JsonProcessingException, JSONException {
    UnlModify unlModify = UnlModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator("EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539")
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    // What we expect the ObjectMapper to Serialize (given the above object)
    String expectedSerializedJson = "{" +
      "\"Account\":\"" + ACCOUNT_ZERO + "\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"" +
      "}";

    String serialized = objectMapper.writeValueAsString(unlModify);
    JSONAssert.assertEquals(expectedSerializedJson, serialized, JSONCompareMode.STRICT);

    // What we provide the ObjectMapper to Deserialize
    String expectedDeserializedJson = "{" +
      "\"Account\":\"" + ACCOUNT_ZERO + "\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"" +
      "}";

    Transaction deserialized = objectMapper.readValue(expectedDeserializedJson, Transaction.class);
    assertThat(deserialized).isEqualTo(unlModify);
  }

  // Using Transcation
  // Using Immutable
  // Using UnlModify

//  @Test
//  public void testDeserializeJson() throws JsonProcessingException {
//    String json = "{" +
//      "\"Account\":\"\"," +
//      "\"Fee\":\"0\"," +
//      "\"LedgerSequence\":94084608," +
//      "\"Sequence\":0," +
//      "\"SigningPubKey\":\"\"," +
//      "\"TransactionType\":\"UNLModify\"," +
//      "\"UNLModifyDisabling\":1," +
//      "\"UNLModifyValidator\":\"ED63CF929BE85B266A66584B3FE2EB97FC248203F0271DC9C833563E60418E7818\"}";
//    UnlModify deserialized = objectMapper.readValue(json, UnlModify.class);
//
//    UnlModify expectedUnlModify = UnlModify.builder()
//      .fee(XrpCurrencyAmount.ofDrops(0))
//      .sequence(UnsignedInteger.ZERO)
//      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(94084608)))
//      .unlModifyDisabling(UnsignedInteger.ONE)
//      .unlModifyValidator("ED63CF929BE85B266A66584B3FE2EB97FC248203F0271DC9C833563E60418E7818")
//      .build();
//
//    Assertions.assertThat(deserialized).isEqualTo(expectedUnlModify);
//  }
//
//  @Test
//  public void testDeserializeJsonUsingImmutable() throws JsonProcessingException {
//    String json = "{" +
//      "\"Account\":\"\"," +
//      "\"Fee\":\"0\"," +
//      "\"LedgerSequence\":94084608," +
//      "\"Sequence\":0," +
//      "\"SigningPubKey\":\"\"," +
//      "\"TransactionType\":\"UNLModify\"," +
//      "\"UNLModifyDisabling\":1," +
//      "\"UNLModifyValidator\":\"ED63CF929BE85B266A66584B3FE2EB97FC248203F0271DC9C833563E60418E7818\"}";
//    Transaction deserialized = objectMapper.readValue(json, Transaction.class);
//
//    UnlModify expectedUnlModify = UnlModify.builder()
//      .fee(XrpCurrencyAmount.ofDrops(0))
//      .sequence(UnsignedInteger.ZERO)
//      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(94084608)))
//      .unlModifyDisabling(UnsignedInteger.ONE)
//      .unlModifyValidator("ED63CF929BE85B266A66584B3FE2EB97FC248203F0271DC9C833563E60418E7818")
//      .build();
//
//    Assertions.assertThat(deserialized).isEqualTo(expectedUnlModify);
//  }


}
