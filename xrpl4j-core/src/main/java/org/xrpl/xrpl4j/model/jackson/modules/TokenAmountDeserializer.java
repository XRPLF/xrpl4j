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
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.amount.IouTokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.TokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.XrpTokenAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link TokenAmount}s.
 *
 * <p>Dispatch rules (identical wire format to {@link CurrencyAmountDeserializer}):
 * <ul>
 *   <li>JSON object with {@code mpt_issuance_id} field → {@link MptTokenAmount}</li>
 *   <li>JSON object with {@code currency} and {@code issuer} fields → {@link IouTokenAmount}</li>
 *   <li>JSON string or number → {@link XrpTokenAmount} (value interpreted as drops)</li>
 * </ul>
 */
public class TokenAmountDeserializer extends StdDeserializer<TokenAmount> {

  /**
   * No-args constructor.
   */
  public TokenAmountDeserializer() {
    super(TokenAmount.class);
  }

  @Override
  public TokenAmount deserialize(
    final JsonParser jsonParser,
    final DeserializationContext deserializationContext
  ) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    if (node.isContainerNode()) {
      if (node.has("mpt_issuance_id")) {
        return MptTokenAmount.builder()
          .amount(MptAmountDeserializer.fromString(node.get("value").asText()))
          .mptIssuanceId(MpTokenIssuanceId.of(node.get("mpt_issuance_id").asText()))
          .build();
      } else {
        return IouTokenAmount.builder()
          .amount(IouAmountDeserializer.fromString(node.get("value").asText()))
          .currency(node.get("currency").asText())
          .issuer(Address.of(node.get("issuer").asText()))
          .build();
      }
    } else {
      return XrpTokenAmount.ofDrops(node.asLong());
    }
  }
}
