package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link AssetAmount}s. Serializes the value as a JSON string.
 */
public class AssetAmountSerializer extends StdScalarSerializer<AssetAmount> {

  /**
   * No-args constructor.
   */
  public AssetAmountSerializer() {
    super(AssetAmount.class, false);
  }

  @Override
  public void serialize(AssetAmount value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.value());
  }
}
