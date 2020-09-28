package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class ObjectMapperFactory {

  private static ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new GuavaModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
