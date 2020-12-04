package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;

// TODO: Unittest
public class LedgerIndexSerializer extends StdScalarSerializer<String> {

  public LedgerIndexSerializer() {
    super(Address.class, false);
  }

  @Override
  public void serialize(String ledgerIndex, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (isInteger(ledgerIndex)) {
      gen.writeNumber(Integer.parseInt(ledgerIndex));
    } else {
      gen.writeString(ledgerIndex);
    }
  }

  private boolean isInteger(final String value) {
    try {
      Integer.parseInt(value);
      // is an integer!
      return true;
    } catch (NumberFormatException e) {
      // not an integer!
      return false;
    }
  }
}
