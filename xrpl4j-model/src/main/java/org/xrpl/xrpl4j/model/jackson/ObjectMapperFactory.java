package org.xrpl.xrpl4j.model.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ripple.cryptoconditions.jackson.CryptoConditionsModule;
import com.ripple.cryptoconditions.jackson.Encoding;
import org.xrpl.xrpl4j.model.jackson.modules.Xrpl4jModule;

/**
 * A factory for constructing instances of {@link ObjectMapper} for all connector components.
 */
public class ObjectMapperFactory {

  /**
   * Construct an {@link ObjectMapper} that can be used to serialize and deserialize JSON where all numbers are Strings,
   * by default.
   *
   * @return An {@link ObjectMapper}.
   */
  public static ObjectMapper create() {

    return JsonMapper.builder()
        .addModule(new Jdk8Module())
        .addModule(new GuavaModule())
        .addModule(new Xrpl4jModule())
        .addModule(new JavaTimeModule())
        .addModule(new CryptoConditionsModule(Encoding.HEX))
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .serializationInclusion(JsonInclude.Include.NON_EMPTY)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        .build();
  }
}
