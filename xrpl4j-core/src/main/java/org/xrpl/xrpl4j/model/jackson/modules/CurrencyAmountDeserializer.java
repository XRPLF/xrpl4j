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
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link CurrencyAmount}s.
 */
public class CurrencyAmountDeserializer extends StdDeserializer<CurrencyAmount> {

  /**
   * No-args constructor.
   */
  protected CurrencyAmountDeserializer() {
    super(CurrencyAmount.class);
  }

  @Override
  public CurrencyAmount deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    if (node.isContainerNode()) {
      if (node.has("mpt_issuance_id")) {
        String mptIssuanceId = node.get("mpt_issuance_id").asText();
        String value = node.get("value").asText();
        return MptCurrencyAmount.builder()
          .mptIssuanceId(MpTokenIssuanceId.of(mptIssuanceId))
          .value(value)
          .build();
      } else {
        String currency = node.get("currency").asText();
        String value = node.get("value").asText();
        String issuer = node.get("issuer").asText();

        return IssuedCurrencyAmount.builder()
          .value(value)
          .issuer(Address.of(issuer))
          .currency(currency)
          .build();
      }
    } else {
      return XrpCurrencyAmount.ofDrops(node.asLong());
    }
  }
}
