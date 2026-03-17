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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.GranularPermissionValue;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TransactionTypePermission;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom Jackson deserializer for {@link Permission}s.
 */
public class PermissionDeserializer extends StdDeserializer<Permission> {

  /**
   * No-args constructor.
   */
  public PermissionDeserializer() {
    super(Permission.class);
  }

  @Override
  public Permission deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    String value = jsonParser.getValueAsString();

    // Try to parse as TransactionType first
    TransactionType transactionType = TransactionType.forValue(value);
    if (transactionType != TransactionType.UNKNOWN) {
      return TransactionTypePermission.of(transactionType);
    }

    // Try to parse as GranularPermission
    Optional<GranularPermission> granularPermission = GranularPermission.forValue(value);
    if (granularPermission.isPresent()) {
      return GranularPermissionValue.of(granularPermission.get());
    }

    // If neither, throw an exception
    throw new IllegalArgumentException(
      String.format("Unknown permission value: %s", value)
    );
  }
}

