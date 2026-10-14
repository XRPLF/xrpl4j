package org.xrpl.xrpl4j.model.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.example.consumer.ConsumerDateType;
import com.example.consumer.ConsumerDecimalType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.modules.Xrpl4jModule;

import java.math.BigDecimal;
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
   * A mapper with Jackson's default configuration for {@link java.time} types, used as the reference for how a
   * consumer-owned {@link ZonedDateTime} should behave.
   */
  private final ObjectMapper referenceMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

  /**
   * {@link ObjectMapperFactory#create()} is the documented mapper for xrpl4j types. A consumer whose own types share an
   * object graph with xrpl4j types (e.g., a DTO holding a {@code TransactionResult}) must run both through it, so its
   * handling of a consumer-owned {@link ZonedDateTime} must match Jackson's default rather than xrpld's format.
   */
  @Test
  void unannotatedZonedDateTimeSerializesLikeJacksonDefault() throws JsonProcessingException {
    ConsumerDateType consumerType = new ConsumerDateType();
    consumerType.when = WHEN;

    assertThat(objectMapper.writeValueAsString(consumerType))
      .isEqualTo(referenceMapper.writeValueAsString(consumerType));
  }

  @Test
  void unannotatedZonedDateTimeDeserializesFromIso8601() throws JsonProcessingException {
    ConsumerDateType consumerType = objectMapper.readValue(ISO_JSON, ConsumerDateType.class);

    assertThat(consumerType.when.toInstant()).isEqualTo(WHEN.toInstant());
  }

  @Test
  void unannotatedZonedDateTimeRoundTrips() throws JsonProcessingException {
    ConsumerDateType consumerType = new ConsumerDateType();
    consumerType.when = WHEN;

    ConsumerDateType roundTripped = objectMapper.readValue(objectMapper.writeValueAsString(consumerType), ConsumerDateType.class);

    assertThat(roundTripped.when.toInstant()).isEqualTo(WHEN.toInstant());
  }

  /**
   * The factory must not impose xrpl4j's inclusion rules on consumer-owned types: a null property is written as
   * {@code null}, which is Jackson's default.
   */
  @Test
  void consumerTypeKeepsNullProperties() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(new ConsumerDateType())).isEqualTo("{\"when\":null}");
  }

  /**
   * The factory must not impose xrpl4j's unknown-property leniency on consumer-owned types.
   */
  @Test
  void consumerTypeFailsOnUnknownProperties() {
    assertThatThrownBy(() -> objectMapper.readValue("{\"when\":null,\"typo\":1}", ConsumerDateType.class))
      .isInstanceOf(UnrecognizedPropertyException.class);
  }

  /**
   * The factory must not impose case-insensitive property matching on consumer-owned types.
   */
  @Test
  void consumerTypePropertiesAreCaseSensitive() {
    assertThatThrownBy(() -> objectMapper.readValue("{\"WHEN\":null}", ConsumerDateType.class))
      .isInstanceOf(UnrecognizedPropertyException.class);
  }

  /**
   * The factory must not impose xrpl4j's plain-notation {@link BigDecimal} rendering on consumer-owned types.
   */
  @Test
  void consumerTypeWritesBigDecimalLikeJacksonDefault() throws JsonProcessingException {
    ConsumerDecimalType consumerType = new ConsumerDecimalType();
    consumerType.amount = new BigDecimal("1E-7");

    assertThat(objectMapper.writeValueAsString(consumerType)).isEqualTo("{\"amount\":1E-7}");
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
    ConsumerDateType consumerType = new ConsumerDateType();
    consumerType.when = WHEN;

    assertThat(mapperWithModule.writeValueAsString(consumerType)).isEqualTo(ISO_JSON);
    assertThat(mapperWithModule.readValue(ISO_JSON, ConsumerDateType.class).when.toInstant()).isEqualTo(WHEN.toInstant());
  }
}
