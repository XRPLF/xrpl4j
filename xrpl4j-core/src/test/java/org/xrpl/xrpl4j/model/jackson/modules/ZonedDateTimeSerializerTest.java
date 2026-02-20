package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Unit tests for {@link ZonedDateTimeSerializer}.
 */
class ZonedDateTimeSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void testSerializeWithNineDecimalPlaces() throws JsonProcessingException {
    ZonedDateTime dateTime = ZonedDateTime.of(2020, 3, 24, 1, 41, 11, 0, ZoneId.of("UTC"));
    SerializerDateTimeWrapper wrapper = SerializerDateTimeWrapper.of(dateTime);

    String json = objectMapper.writeValueAsString(wrapper);

    assertThat(json).contains("2020-Mar-24 01:41:11.000000000 UTC");
  }

  @Test
  void testSerializeWithSixDecimalPlaces() throws JsonProcessingException {
    ZonedDateTime dateTime = ZonedDateTime.of(2020, 3, 24, 1, 27, 42, 147330000, ZoneId.of("UTC"));
    SerializerDateTimeWrapper wrapper = SerializerDateTimeWrapper.of(dateTime);

    String json = objectMapper.writeValueAsString(wrapper);

    // Note: The formatter uses 9 decimal places, so 147330000 nanos = .147330000
    assertThat(json).contains("2020-Mar-24 01:27:42.147330000 UTC");
  }

  @Test
  void testSerializeConvertsToUtc() throws JsonProcessingException {
    // Create a ZonedDateTime in a different timezone (CET is UTC+1 in March)
    ZonedDateTime dateTime = ZonedDateTime.of(2020, 3, 24, 2, 41, 11, 0, ZoneId.of("Europe/Paris"));
    SerializerDateTimeWrapper wrapper = SerializerDateTimeWrapper.of(dateTime);

    String json = objectMapper.writeValueAsString(wrapper);

    // Should convert to UTC (1 hour earlier)
    assertThat(json).contains("2020-Mar-24 01:41:11.000000000 UTC");
  }

  @Test
  void testSerializeWithZoneOffset() throws JsonProcessingException {
    // Create with ZoneOffset.UTC instead of ZoneId.of("UTC")
    ZonedDateTime dateTime = ZonedDateTime.of(2020, 3, 24, 1, 41, 11, 0, ZoneOffset.UTC);
    SerializerDateTimeWrapper wrapper = SerializerDateTimeWrapper.of(dateTime);

    String json = objectMapper.writeValueAsString(wrapper);

    // Should still output "UTC" not "Z"
    assertThat(json).contains("2020-Mar-24 01:41:11.000000000 UTC");
    assertThat(json).doesNotContain(" Z\"");
  }

  @Test
  void testSerializeWithNonZeroNanos() throws JsonProcessingException {
    // Test with a specific nanosecond value
    ZonedDateTime dateTime = ZonedDateTime.of(2020, 3, 24, 1, 27, 42, 123456789, ZoneId.of("UTC"));
    SerializerDateTimeWrapper wrapper = SerializerDateTimeWrapper.of(dateTime);

    String json = objectMapper.writeValueAsString(wrapper);

    assertThat(json).contains("2020-Mar-24 01:27:42.123456789 UTC");
  }

  /**
   * Test wrapper class for ZonedDateTime serialization tests.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableSerializerDateTimeWrapper.class)
  @JsonDeserialize(as = ImmutableSerializerDateTimeWrapper.class)
  interface SerializerDateTimeWrapper {

    static SerializerDateTimeWrapper of(ZonedDateTime time) {
      return ImmutableSerializerDateTimeWrapper.builder().time(time).build();
    }

    @JsonProperty("time")
    @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z", locale = "en_US")
    ZonedDateTime time();
  }
}
