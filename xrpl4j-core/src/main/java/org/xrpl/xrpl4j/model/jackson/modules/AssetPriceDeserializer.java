package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link AssetPrice}s.
 */
public class AssetPriceDeserializer extends StdDeserializer<AssetPrice> {

  /**
   * No-args constructor.
   */
  public AssetPriceDeserializer() {
    super(AssetPrice.class);
  }

  @Override
  public AssetPrice deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    // sfAssetPrice is an STUInt64s, which in JSON is represented as a hex-encoded String.
    return AssetPrice.of(UnsignedLong.valueOf(jsonParser.getText(), 16));
  }
}
