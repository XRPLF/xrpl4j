package org.xrpl.xrpl4j.model.jackson;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Unit tests for {@link ObjectMapperFactory}.
 */
class ObjectMapperFactoryTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void unannotatedZonedDateTimeSerializesAsIso8601() throws JsonProcessingException {
    ConsumerType consumerType = new ConsumerType();
    consumerType.when = ZonedDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);

    assertThat(objectMapper.writeValueAsString(consumerType)).isEqualTo("{\"when\":\"2024-01-02T03:04:05Z\"}");
  }

  @Test
  void unannotatedZonedDateTimeDeserializesFromIso8601() throws JsonProcessingException {
    ConsumerType consumerType = objectMapper.readValue("{\"when\":\"2024-01-02T03:04:05Z\"}", ConsumerType.class);

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
