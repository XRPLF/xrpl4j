package org.xrpl.xrpl4j.model.jackson.modules;

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
