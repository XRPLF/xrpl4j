package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;

import java.io.IOException;

public class LedgerSpecifierSerializer extends StdSerializer<LedgerSpecifier> {

  public LedgerSpecifierSerializer() {
    super(LedgerSpecifier.class, false);
  }

  @Override
  public void serialize(
    LedgerSpecifier ledgerSpecifier,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  ) throws IOException {

  }
}
