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
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link MptTokenAmount}.
 *
 * <p>Reads the wire format {@code {"value":"...","mpt_issuance_id":"..."}} and constructs
 * an {@link MptTokenAmount}. The {@code value} field is parsed via {@link MptAmountDeserializer#fromString(String)}, so
 * any change to how {@link org.xrpl.xrpl4j.model.transactions.amount.MptAmount} deserializes is automatically reflected
 * here.
 */
public class MptTokenAmountDeserializer extends StdDeserializer<MptTokenAmount> {

  /**
   * No-args constructor.
   */
  public MptTokenAmountDeserializer() {
    super(MptTokenAmount.class);
  }

  @Override
  public MptTokenAmount deserialize(
    final JsonParser jsonParser,
    final DeserializationContext ctxt
  ) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    return MptTokenAmount.builder()
      .amount(MptAmountDeserializer.fromString(node.get("value").asText()))
      .mptIssuanceId(MpTokenIssuanceId.of(node.get("mpt_issuance_id").asText()))
      .build();
  }
}
