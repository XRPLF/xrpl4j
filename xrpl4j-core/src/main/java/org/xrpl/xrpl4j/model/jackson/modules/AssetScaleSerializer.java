package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;
import org.xrpl.xrpl4j.model.transactions.AssetScale;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link AssetPrice}s.
 */
public class AssetScaleSerializer extends StdScalarSerializer<AssetScale> {

  /**
   * No-args constructor.
   */
  public AssetScaleSerializer() {
    super(AssetScale.class, false);
  }

  @Override
  public void serialize(AssetScale value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeNumber(value.value().longValue());
  }
}
