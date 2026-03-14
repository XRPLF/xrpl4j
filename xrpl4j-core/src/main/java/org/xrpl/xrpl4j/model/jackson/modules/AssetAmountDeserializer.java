package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link AssetAmount}s. Deserializes from a JSON string.
 */
public class AssetAmountDeserializer extends StdDeserializer<AssetAmount> {

  /**
   * No-args constructor.
   */
  public AssetAmountDeserializer() {
    super(AssetAmount.class);
  }

  @Override
  public AssetAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return AssetAmount.of(jsonParser.getText());
  }
}
