package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link Address}es.
 */
public class AddressDeserializer extends StdDeserializer<Address> {

  public AddressDeserializer() {
    super(Address.class);
  }

  @Override
  public Address deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return Address.of(jsonParser.getText());
  }
}
