package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.Uri;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Uri}s.
 */
public class UriDeserializer extends StdDeserializer<Uri> {
  /**
   * No-args constructor.
   */
  public UriDeserializer() {
    super(Uri.class);
  }

  @Override
  public Uri deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return Uri.of(jsonParser.getText());
  }
}
