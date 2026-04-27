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
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link MptAmount}.
 *
 * <p>Writes the MPT value as a bare JSON string (e.g. {@code "1000"} or {@code "-500"}),
 * matching the scalar representation used when an {@link MptAmount} appears as a standalone field.
 */
public class MptAmountSerializer extends StdScalarSerializer<MptAmount> {

  /**
   * No-args constructor.
   */
  public MptAmountSerializer() {
    super(MptAmount.class, false);
  }

  @Override
  public void serialize(
    final MptAmount mptAmount,
    final JsonGenerator gen,
    final SerializerProvider provider
  ) throws IOException {
    gen.writeString(mptAmount.value());
  }
}
