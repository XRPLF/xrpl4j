package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link TransferFee}s.
 */
public class TransferFeeSerializer  extends StdScalarSerializer<TransferFee> {

  /**
   * No-args constructor.
   */
  public TransferFeeSerializer() {
    super(TransferFee.class, false);
  }

  @Override
  public void serialize(TransferFee transferFee, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(transferFee.toString());
  }
}
