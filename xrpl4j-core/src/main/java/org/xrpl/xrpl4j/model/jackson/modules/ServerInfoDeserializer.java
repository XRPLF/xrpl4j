package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import org.xrpl.xrpl4j.model.client.serverinfo.ClioServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ReportingModeServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.RippledServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.io.IOException;

/**
 * A {@link StdDeserializer} for deserializing instances of {@link ServerInfo}.
 */
public class ServerInfoDeserializer extends StdDeserializer<ServerInfo> {

  /**
   * No-args constructor.
   */
  protected ServerInfoDeserializer() {
    super(ServerInfo.class);
  }

  @Override
  public ServerInfo deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException {
    ObjectMapper objectMapper = ObjectMapperFactory.create();
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    ServerInfo info;
    if (node.has("clio_version")) {
      info = objectMapper.treeToValue(node, ClioServerInfo.class);
    } else if (node.has("reporting")) {
      info = objectMapper.treeToValue(node, ReportingModeServerInfo.class);
    } else {
      info = objectMapper.treeToValue(node, RippledServerInfo.class);
    }
    return info;
  }
}
