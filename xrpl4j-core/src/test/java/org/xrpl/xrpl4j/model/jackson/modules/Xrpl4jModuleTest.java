package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Unit tests for {@link Xrpl4jModule}.
 */
class Xrpl4jModuleTest {

  /**
   * A consumer that registers {@link Xrpl4jModule} on its own mapper must keep the default date handling for its own
   * types.
   */
  private final ObjectMapper consumerMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .addModule(new Xrpl4jModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build();

  @Test
  void registeringModuleDoesNotChangeZonedDateTimeSerialization() throws JsonProcessingException {
    ConsumerType consumerType = new ConsumerType();
    consumerType.when = ZonedDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);

    assertThat(consumerMapper.writeValueAsString(consumerType)).isEqualTo("{\"when\":\"2024-01-02T03:04:05Z\"}");
  }

  @Test
  void registeringModuleDoesNotChangeZonedDateTimeDeserialization() throws JsonProcessingException {
    ConsumerType consumerType = consumerMapper.readValue("{\"when\":\"2024-01-02T03:04:05Z\"}", ConsumerType.class);

    assertThat(consumerType.when.toInstant())
      .isEqualTo(ZonedDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC).toInstant());
  }

  /**
   * Stands in for a type owned by a consumer of xrpl4j that has no xrpl4j-specific annotations.
   */
  public static class ConsumerType {

    public ZonedDateTime when;
  }
}
