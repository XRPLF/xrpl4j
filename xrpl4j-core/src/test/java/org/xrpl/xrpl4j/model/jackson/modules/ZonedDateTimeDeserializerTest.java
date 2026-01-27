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
 * Unit tests for {@link ZonedDateTimeDeserializer}.
 */
class ZonedDateTimeDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void testDeserializeWithNineDecimalPlaces() throws JsonProcessingException {
    String json = "{\"time\":\"2020-Mar-24 01:41:11.000000000 UTC\"}";

    DeserializerDateTimeWrapper result = objectMapper.readValue(json, DeserializerDateTimeWrapper.class);

    ZonedDateTime expected = ZonedDateTime.of(2020, 3, 24, 1, 41, 11, 0, ZoneOffset.UTC);
    assertThat(result.time()).isEqualTo(expected);
  }

  @Test
  void testDeserializeWithSixDecimalPlaces() throws JsonProcessingException {
    String json = "{\"time\":\"2020-Mar-24 01:27:42.147330 UTC\"}";

    DeserializerDateTimeWrapper result = objectMapper.readValue(json, DeserializerDateTimeWrapper.class);

    ZonedDateTime expected = ZonedDateTime.of(2020, 3, 24, 1, 27, 42, 147330000, ZoneOffset.UTC);
    assertThat(result.time()).isEqualTo(expected);
  }

  @Test
  void testDeserializeWithThreeDecimalPlaces() throws JsonProcessingException {
    String json = "{\"time\":\"2020-Mar-24 01:27:42.147 UTC\"}";

    DeserializerDateTimeWrapper result = objectMapper.readValue(json, DeserializerDateTimeWrapper.class);

    ZonedDateTime expected = ZonedDateTime.of(2020, 3, 24, 1, 27, 42, 147000000, ZoneOffset.UTC);
    assertThat(result.time()).isEqualTo(expected);
  }

  @Test
  void testDeserializeWithNoDecimalPlaces() throws JsonProcessingException {
    String json = "{\"time\":\"2020-Mar-24 01:41:11 UTC\"}";

    DeserializerDateTimeWrapper result = objectMapper.readValue(json, DeserializerDateTimeWrapper.class);

    ZonedDateTime expected = ZonedDateTime.of(2020, 3, 24, 1, 41, 11, 0, ZoneOffset.UTC);
    assertThat(result.time()).isEqualTo(expected);
  }

  @Test
  void testDeserializeEnsuresUtcZone() throws JsonProcessingException {
    String json = "{\"time\":\"2020-Mar-24 01:41:11.000000000 UTC\"}";

    DeserializerDateTimeWrapper result = objectMapper.readValue(json, DeserializerDateTimeWrapper.class);

    // Verify the zone is ZoneOffset.UTC (the deserializer converts to UTC)
    assertThat(result.time().getZone()).isEqualTo(ZoneOffset.UTC);
  }

  @Test
  void testRoundTrip() throws JsonProcessingException {
    ZonedDateTime original = ZonedDateTime.of(2020, 3, 24, 1, 27, 42, 123456789, ZoneId.of("UTC"));
    RoundTripDateTimeWrapper wrapper = RoundTripDateTimeWrapper.of(original);

    String json = objectMapper.writeValueAsString(wrapper);
    RoundTripDateTimeWrapper result = objectMapper.readValue(json, RoundTripDateTimeWrapper.class);

    // Times should be equal (though zone representation may differ)
    assertThat(result.time().toInstant()).isEqualTo(original.toInstant());
  }

  /**
   * Test wrapper class for ZonedDateTime deserialization tests.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableDeserializerDateTimeWrapper.class)
  @JsonDeserialize(as = ImmutableDeserializerDateTimeWrapper.class)
  interface DeserializerDateTimeWrapper {

    static DeserializerDateTimeWrapper of(ZonedDateTime time) {
      return ImmutableDeserializerDateTimeWrapper.builder().time(time).build();
    }

    @JsonProperty("time")
    @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS] z", locale = "en_US")
    ZonedDateTime time();
  }

  /**
   * Test wrapper class for round-trip tests. Uses a fixed pattern that works for both serialization and deserialization.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableRoundTripDateTimeWrapper.class)
  @JsonDeserialize(as = ImmutableRoundTripDateTimeWrapper.class)
  interface RoundTripDateTimeWrapper {

    static RoundTripDateTimeWrapper of(ZonedDateTime time) {
      return ImmutableRoundTripDateTimeWrapper.builder().time(time).build();
    }

    @JsonProperty("time")
    @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z", locale = "en_US")
    ZonedDateTime time();
  }
}
