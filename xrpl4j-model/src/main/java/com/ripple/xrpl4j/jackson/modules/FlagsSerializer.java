package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.Flags;
import com.ripple.xrpl4j.transactions.Hash256;

import java.io.IOException;

public class FlagsSerializer extends StdScalarSerializer<Flags> {

  public FlagsSerializer() {
    super(Flags.class, false);
  }

  @Override
  public void serialize(Flags flags, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(String.valueOf(flags.getValue()));
  }
}
