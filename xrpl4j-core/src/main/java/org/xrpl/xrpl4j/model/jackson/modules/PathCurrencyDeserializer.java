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
import org.xrpl.xrpl4j.model.client.path.PathCurrency;
import org.xrpl.xrpl4j.model.ledger.CurrencyIssue;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link PathCurrency}.
 *
 * <p>This deserializer handles the polymorphic nature of PathCurrency, which can wrap
 * {@link XrpIssue} (for XRP), {@link IouIssue} (for IOUs), or {@link MptIssue} (for MPTokens).</p>
 */
public class PathCurrencyDeserializer extends StdDeserializer<PathCurrency> {

  /**
   * No-args constructor.
   */
  public PathCurrencyDeserializer() {
    super(PathCurrency.class);
  }

  @Override
  public PathCurrency deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    // Check if it's an MPT (has mpt_issuance_id field)
    if (node.has("mpt_issuance_id")) {
      return PathCurrency.of(
        MptIssue.of(MpTokenIssuanceId.of(node.get("mpt_issuance_id").asText()))
      );
    }

    // Otherwise, it's a currency-based issue (has currency field)
    if (node.has("currency")) {
      String currency = node.get("currency").asText();

      // Check if it's XRP
      if ("XRP".equals(currency)) {
        return PathCurrency.of(XrpIssue.builder().build());
      }

      // Otherwise, it's an IOU
      if (node.has("issuer")) {
        return PathCurrency.of(
          IouIssue.builder()
            .currency(currency)
            .issuer(Address.of(node.get("issuer").asText()))
            .build()
        );
      }

      // For backwards compatibility, if there's no issuer, create a deprecated CurrencyIssue
      // This allows deserialization of legacy JSON that doesn't include an issuer
      return PathCurrency.of(
        CurrencyIssue.builder()
          .currency(currency)
          .build()
      );
    }

    throw new IllegalArgumentException(
      "PathCurrency JSON must have either 'mpt_issuance_id' or 'currency' field"
    );
  }
}

