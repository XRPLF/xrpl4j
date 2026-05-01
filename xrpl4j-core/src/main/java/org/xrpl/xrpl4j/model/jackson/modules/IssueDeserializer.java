package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link Issue} that dispatches to the correct subtype
 * ({@link XrpIssue}, {@link IouIssue}, or {@link MptIssue}) based on the JSON fields present.
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

    // Otherwise, it's a currency-based issue (has currency field)
    if (node.has("currency")) {
      String currency = node.get("currency").asText();

      // Check if it's XRP (case-insensitive)
      if ("XRP".equalsIgnoreCase(currency)) {
        return Issue.XRP;
      }

      // Otherwise, it's an IOU (must have issuer)
      if (node.has("issuer")) {
        return IouIssue.builder()
          .currency(currency)
          .issuer(Address.of(node.get("issuer").asText()))
          .build();
      }

      throw new IllegalArgumentException(
        "Invalid Issue JSON: IOU currency '" + currency + "' must have an 'issuer' field"
      );
    }

    throw new IOException("Cannot deserialize Issue: must contain 'currency' or 'mpt_issuance_id' field");
  }
}
