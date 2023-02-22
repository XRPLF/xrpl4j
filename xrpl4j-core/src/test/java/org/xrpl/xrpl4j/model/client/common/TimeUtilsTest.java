package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

class TimeUtilsTest {

  @Test
  void convertXrplTimeToZonedDateTime() {
    UnsignedLong xrplTimestamp = UnsignedLong.valueOf(666212460);
    ZonedDateTime zonedDateTime = TimeUtils.xrplTimeToZonedDateTime(xrplTimestamp);
    assertThat(zonedDateTime).isEqualTo(ZonedDateTime.of(LocalDateTime.of(2021, 2, 9, 19, 1, 0), ZoneId.of("UTC")));
  }
}