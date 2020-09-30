package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.ripple.xrpl4j.transactions.Address;

import java.io.IOException;

public class AddressSerializer extends StdScalarSerializer<Address> {

  public AddressSerializer() {
    super(Address.class, false);
  }

  @Override
  public void serialize(Address address, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(address.value());
  }
}
