package org.xrpl.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Resources;

import java.io.IOException;

public class DefaultDefinitionsProvider implements DefinitionsProvider {

  private final Supplier<Definitions> supplier;

  public DefaultDefinitionsProvider(ObjectMapper objectMapper) {
    this.supplier = Suppliers.memoize(() -> {
      try {
        return objectMapper.readerFor(Definitions.class).readValue(Resources.getResource("definitions.json"));
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read definition.json file", e);
      }
    });
  }

  @Override
  public Definitions get() {
    return supplier.get();
  }

}
