package org.xrpl.xrpl4j.model.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.modules.Xrpl4jModule;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Unit tests for {@link ObjectMapperFactory}.
 */
class ObjectMapperFactoryTest {

  private static final ZonedDateTime WHEN = ZonedDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
  private static final String ISO_JSON = "{\"when\":\"2024-01-02T03:04:05Z\"}";

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  /**
   * {@link ObjectMapperFactory#create()} is the documented mapper for xrpl4j types. A consumer whose own types share an
   * object graph with xrpl4j types (e.g., a DTO holding a {@code TransactionResult}) must run both through it, so its
   * handling of a consumer-owned {@link ZonedDateTime} must stay the Jackson default (ISO-8601).
   */
  @Test
  void unannotatedZonedDateTimeSerializesAsIso8601() throws JsonProcessingException {
    ConsumerType consumerType = new ConsumerType();
    consumerType.when = WHEN;

    assertThat(objectMapper.writeValueAsString(consumerType)).isEqualTo(ISO_JSON);
  }

  @Test
  void unannotatedZonedDateTimeDeserializesFromIso8601() throws JsonProcessingException {
    ConsumerType consumerType = objectMapper.readValue(ISO_JSON, ConsumerType.class);

    assertThat(consumerType.when.toInstant()).isEqualTo(WHEN.toInstant());
  }

  /**
   * Defense in depth: registering {@link Xrpl4jModule} on its own is not sufficient to handle xrpl4j types (the
   * factory's other modules and features are also required), but the module itself must not carry a mapper-wide
   * {@link ZonedDateTime} registration either.
   */
  @Test
  void xrpl4jModuleAloneDoesNotChangeZonedDateTimeHandling() throws JsonProcessingException {
    ObjectMapper mapperWithModule = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .addModule(new Xrpl4jModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .build();
    ConsumerType consumerType = new ConsumerType();
    consumerType.when = WHEN;

    assertThat(mapperWithModule.writeValueAsString(consumerType)).isEqualTo(ISO_JSON);
    assertThat(mapperWithModule.readValue(ISO_JSON, ConsumerType.class).when.toInstant()).isEqualTo(WHEN.toInstant());
  }

  /**
   * Stands in for a type owned by a consumer of xrpl4j that has no xrpl4j-specific annotations.
   */
  public static class ConsumerType {

    public ZonedDateTime when;
  }
}
