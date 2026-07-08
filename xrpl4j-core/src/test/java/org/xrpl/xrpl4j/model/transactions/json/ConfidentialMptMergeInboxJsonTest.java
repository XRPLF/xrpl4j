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
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptMergeInbox;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * JSON serialization/deserialization tests for {@link ConfidentialMptMergeInbox}.
 */
public class ConfidentialMptMergeInboxJsonTest extends AbstractJsonTest {

  @Test
  public void testJsonWithRequiredFieldsOnly() throws JSONException, JsonProcessingException {
    ConfidentialMptMergeInbox mergeInbox = ConfidentialMptMergeInbox.builder()
      .account(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTMergeInbox\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\"\n" +
      "}";

    assertCanSerializeAndDeserialize(mergeInbox, json);
  }
}
