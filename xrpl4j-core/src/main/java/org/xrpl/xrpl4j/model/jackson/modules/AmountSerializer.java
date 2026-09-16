package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.Amount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Amount}s. Serializes the value as a JSON string.
 */
public class AmountSerializer extends StdScalarSerializer<Amount> {

  /**
   * No-args constructor.
   */
  public AmountSerializer() {
    super(Amount.class, false);
  }

  @Override
  public void serialize(Amount value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.value());
  }
}
