package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * A custom Jackson deserializer for {@link ZonedDateTime} usage in xrpl4j.
 *
 * <p>This deserializer is needed to allow newer versions of Jackson to deserialize ZonedDateTime values that conform
 * to the xrpld format, which uses `UTC` for all timezone designations. Newer versions of Jackson instead emit `Z`, so
 * this serializer ensures that Jackson's serialization for ZonedDateTime uses "UTC" instead of "Z".</p>
 */
public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> implements ContextualDeserializer {

  // Some XRPL dates have 9 digits of nanosecond precision (some have 6). This deserializer parses dates from various
  // sources that may have variable nanosecond precision, so using `[.SSSSSSSSS][.SSSSSS][.SSS]` handles 9, 6, or 3
  // decimal places correctly. For example, if this implementation merely used `[.n]`, n expects exactly 9 digits OR
  // interprets shorter values as literal nanoseconds. So, `.486384` (6 digits) → parsed as 486,384 nanoseconds, which
  // would be incorrect. Conversely, `.486384000` (9 digits) → parsed as 486,384,000 nanoseconds, which would be
  // correct. Therefore, we need to use multiple patterns to handle variable precision.
  private static final DateTimeFormatter DEFAULT_DESERIALIZATION_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS] z", Locale.US);

  private final DateTimeFormatter formatter;

  public ZonedDateTimeDeserializer() {
    this(DEFAULT_DESERIALIZATION_FORMATTER);
  }

  private ZonedDateTimeDeserializer(final DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter);
    this.formatter = formatter;
  }

  @Override
  public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    ZonedDateTime parsed = ZonedDateTime.parse(jsonParser.getText(), formatter);
    // Ensure the zone is explicitly UTC, not just Z
    return parsed.withZoneSameInstant(ZoneOffset.UTC);
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
    if (property != null) {
      JsonFormat.Value format = property.findPropertyFormat(context.getConfig(), ZonedDateTime.class);
      if (format != null && format.hasPattern()) {
        Locale locale = format.hasLocale() ? format.getLocale() : Locale.US;
        return new ZonedDateTimeDeserializer(DateTimeFormatter.ofPattern(format.getPattern(), locale));
      }
    }
    return this;
  }
}
