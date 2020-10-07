package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class ObjectMapperFactory {

  private static ObjectMapper objectMapper = JsonMapper.builder()
      .addModule(new Jdk8Module())
      .addModule(new GuavaModule())
      .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
      .configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
      .serializationInclusion(JsonInclude.Include.NON_EMPTY)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
      .build();

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
