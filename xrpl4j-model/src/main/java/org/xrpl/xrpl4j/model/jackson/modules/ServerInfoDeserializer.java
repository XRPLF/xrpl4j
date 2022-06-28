package org.xrpl.xrpl4j.model.jackson.modules;

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
