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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link Marker}s.
 */
public class MarkerDeserializer extends StdDeserializer<Marker> {

  /**
   * No-args constructor.
   */
  protected MarkerDeserializer() {
    super(Marker.class);
  }

  @Override
  public Marker deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException {
    ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = mapper.readTree(jsonParser);

    if (node instanceof TextNode) {
      return Marker.of(node.asText());
    }

    return Marker.of(mapper.writeValueAsString(node));
  }
}
