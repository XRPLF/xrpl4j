package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Address}es.
 */
public class AddressSerializer extends StdScalarSerializer<Address> {

  public AddressSerializer() {
    super(Address.class, false);
  }

  @Override
  public void serialize(Address address, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(address.value());
  }
}
