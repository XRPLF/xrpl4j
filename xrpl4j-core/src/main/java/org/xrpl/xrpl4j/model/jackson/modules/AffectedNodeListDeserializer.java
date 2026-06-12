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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for {@code List<AffectedNode>} that silently skips null elements produced by
 * {@link AffectedNodeDeserializer} for malformed nodes.
 */
public class AffectedNodeListDeserializer extends StdDeserializer<List<AffectedNode>> {

  private final AffectedNodeDeserializer elementDeserializer = new AffectedNodeDeserializer();

  public AffectedNodeListDeserializer() {
    super(List.class);
  }

  @Override
  public List<AffectedNode> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    List<AffectedNode> result = new ArrayList<>();
    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
      AffectedNode node = elementDeserializer.deserialize(jsonParser, ctxt);
      if (node != null) {
        result.add(node);
      }
    }
    return result;
  }
}
