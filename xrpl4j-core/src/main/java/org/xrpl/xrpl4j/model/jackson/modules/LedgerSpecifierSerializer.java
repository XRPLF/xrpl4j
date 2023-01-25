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
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link LedgerSpecifier}s.
 */
public class LedgerSpecifierSerializer extends StdSerializer<LedgerSpecifier> {

  /**
   * No-args constructor.
   */
  public LedgerSpecifierSerializer() {
    super(LedgerSpecifier.class, false);
  }

  @Override
  public boolean isUnwrappingSerializer() {
    return true;
  }

  @Override
  public void serialize(
    LedgerSpecifier ledgerSpecifier,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  ) throws IOException {
    if (ledgerSpecifier.ledgerHash().isPresent()) {
      jsonGenerator.writeStringField("ledger_hash", ledgerSpecifier.ledgerHash().get().value());
    } else if (ledgerSpecifier.ledgerIndex().isPresent()) {
      jsonGenerator.writeNumberField(
        "ledger_index",
        ledgerSpecifier.ledgerIndex().get().unsignedIntegerValue().intValue()
      );
    } else if (ledgerSpecifier.ledgerIndexShortcut().isPresent()) {
      jsonGenerator.writeStringField("ledger_index", ledgerSpecifier.ledgerIndexShortcut().get().toString());
    }
  }
}
