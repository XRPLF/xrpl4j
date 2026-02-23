package org.xrpl.xrpl4j.model.jackson.modules;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.ledger.CurrencyIssue;
import org.xrpl.xrpl4j.model.ledger.ImmutableCurrencyIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link Issue}s.
 *
 * <p>This deserializer determines which concrete type of Issue to create based on the JSON fields:
 * <ul>
 *   <li>If the JSON has a "mpt_issuance_id" field, creates an {@link MptIssue}</li>
 *   <li>If the JSON has a "currency" field, creates a {@link CurrencyIssue}</li>
 * </ul>
 */
public class IssueDeserializer extends StdDeserializer<Issue> {

  /**
   * No-args constructor.
   */
  public IssueDeserializer() {
    super(Issue.class);
  }

  @Override
  public Issue deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    // Check if it's an MPT issue (has mpt_issuance_id field)
    if (node.has("mpt_issuance_id")) {
      return MptIssue.of(MpTokenIssuanceId.of(node.get("mpt_issuance_id").asText()));
    }

    // Otherwise, it's a currency issue (has currency field, optionally issuer)
    if (node.has("currency")) {
      ImmutableCurrencyIssue.Builder builder = CurrencyIssue.builder()
        .currency(node.get("currency").asText());

      if (node.has("issuer")) {
        builder.issuer(Address.of(node.get("issuer").asText()));
      }

      return builder.build();
    }

    throw new IllegalArgumentException(
      "Invalid Issue JSON: must have either 'mpt_issuance_id' or 'currency' field"
    );
  }
}

