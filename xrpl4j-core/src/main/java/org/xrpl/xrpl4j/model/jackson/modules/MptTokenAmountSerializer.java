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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link MptTokenAmount}.
 *
 * <p>Writes the wire format {@code {"value":"...","mpt_issuance_id":"..."}}.
 * The {@code value} field is written by delegating to {@link MptAmountSerializer}, so any change to how
 * {@link MptAmount} serializes is automatically reflected here.
 */
public class MptTokenAmountSerializer extends StdSerializer<MptTokenAmount> {

  private static final MptAmountSerializer AMOUNT_SERIALIZER = new MptAmountSerializer();

  /**
   * No-args constructor.
   */
  public MptTokenAmountSerializer() {
    super(MptTokenAmount.class);
  }

  @Override
  public void serialize(
    final MptTokenAmount mptTokenAmount,
    final JsonGenerator gen,
    final SerializerProvider provider
  ) throws IOException {
    gen.writeStartObject();
    gen.writeFieldName("value");
    AMOUNT_SERIALIZER.serialize(mptTokenAmount.amount(), gen, provider);
    gen.writeStringField("mpt_issuance_id", mptTokenAmount.mptIssuanceId().value());
    gen.writeEndObject();
  }
}
