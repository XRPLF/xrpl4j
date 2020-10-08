package com.ripple.xrpl4j.codec.binary.definitions;

import com.ripple.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;

import java.util.function.Supplier;

/**
 * Provider for {@link Definitions}
 */
public interface DefinitionsProvider extends Supplier<Definitions> {

  DefinitionsProvider INSTANCE = new DefaultDefinitionsProvider(BinaryCodecObjectMapperFactory.getObjectMapper());

  static DefinitionsProvider getInstance() {
    return INSTANCE;
  }

}
