package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.NumberAmount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link NumberAmount}s. Serializes the value as a JSON string.
 */
public class NumberAmountSerializer extends StdScalarSerializer<NumberAmount> {

  /**
   * No-args constructor.
   */
  public NumberAmountSerializer() {
    super(NumberAmount.class, false);
  }

  @Override
  public void serialize(NumberAmount value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.value());
  }
}
