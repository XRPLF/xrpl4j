package org.xrpl.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Suppliers;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class DefaultDefinitionsProvider implements DefinitionsProvider {

  private final Supplier<Definitions> supplier;

  /**
   * Required-args Constructor.
   *
   * @param objectMapper An {@link ObjectMapper}.
   */
  @SuppressWarnings("UnstableApiUsage")
  public DefaultDefinitionsProvider(final ObjectMapper objectMapper) {
    Objects.requireNonNull(objectMapper);

    this.supplier = Suppliers.memoize(() -> {
      try {
        return objectMapper.readerFor(Definitions.class).readValue(Resources.getResource(DefaultDefinitionsProvider.class, "/definitions.json"));
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
