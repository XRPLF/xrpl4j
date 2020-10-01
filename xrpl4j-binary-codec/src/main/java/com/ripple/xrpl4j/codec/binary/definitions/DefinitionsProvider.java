package com.ripple.xrpl4j.codec.binary.definitions;

import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;

import java.util.function.Supplier;

/**
 * Provider for {@link Definitions}
 */
public interface DefinitionsProvider extends Supplier<Definitions> {

  DefinitionsProvider INSTANCE = new DefaultDefinitionsProvider(ObjectMapperFactory.getObjectMapper());

  static DefinitionsProvider getInstance() {
    return INSTANCE;
  }

}
