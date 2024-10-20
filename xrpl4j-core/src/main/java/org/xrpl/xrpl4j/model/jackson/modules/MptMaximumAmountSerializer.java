package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.MptMaximumAmount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link MptMaximumAmount}s.
 */
public class MptMaximumAmountSerializer extends StdScalarSerializer<MptMaximumAmount> {

  /**
   * No-args constructor.
   */
  public MptMaximumAmountSerializer() {
    super(MptMaximumAmount.class, false);
  }

  @Override
  public void serialize(MptMaximumAmount count, JsonGenerator gen, SerializerProvider provider) throws IOException {
    // sfMaximumAmount is an STUInt64s, which in JSON is represented as a hex-encoded String.
    gen.writeString(count.value().toString(16));
  }
}
