package org.xrpl.xrpl4j.model.client.common;

import com.google.common.primitives.UnsignedLong;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for converting between XRPL timestamps and other time formats.
 */
public class TimeUtils {

  /**
   * XRP Ledger represents dates using a custom epoch called Ripple Epoch. This is a constant for
   * the start of that epoch.
   */
  static UnsignedLong RIPPLE_EPOCH = UnsignedLong.valueOf(946684800);

  /**
   * Convert an XRPL timestamp to a {@link ZonedDateTime}. The time zone of the {@link ZonedDateTime} will be UTC.
   *
   * @param xrplTime An {@link UnsignedLong} representing an XRPL timestamp.
   *
   * @return A {@link ZonedDateTime} in UTC.
   */
  public static ZonedDateTime xrplTimeToZonedDateTime(UnsignedLong xrplTime) {
    return Instant.ofEpochSecond(RIPPLE_EPOCH.plus(xrplTime).longValue()).atZone(ZoneId.of("UTC"));
  }

}
