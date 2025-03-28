package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;
import org.xrpl.xrpl4j.model.transactions.AssetScale;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link AssetPrice}s.
 */
public class AssetScaleDeserializer extends StdDeserializer<AssetScale> {

  /**
   * No-args constructor.
   */
  public AssetScaleDeserializer() {
    super(AssetScale.class);
  }

  @Override
  public AssetScale deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return AssetScale.of(UnsignedInteger.valueOf(jsonParser.getLongValue()));
  }
}
