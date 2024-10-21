package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.MpTokenObjectAmount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link MpTokenObjectAmount}s.
 */
public class MpTokenObjectAmountSerializer extends StdScalarSerializer<MpTokenObjectAmount> {

  /**
   * No-args constructor.
   */
  public MpTokenObjectAmountSerializer() {
    super(MpTokenObjectAmount.class, false);
  }

  @Override
  public void serialize(MpTokenObjectAmount count, JsonGenerator gen, SerializerProvider provider) throws IOException {
    // sfMaximumAmount is an STUInt64s, which in JSON is represented as a hex-encoded String.
    gen.writeString(count.value().toString(16));
  }
}
