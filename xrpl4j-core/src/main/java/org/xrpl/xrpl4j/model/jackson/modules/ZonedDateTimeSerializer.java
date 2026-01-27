package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Custom Jackson serializer for {@link ZonedDateTime} usage in xrpl4j.
 *
 * <p>This serializer is needed because newer Jackson versions changed how the `z` pattern outputs `UTC` time zones.
 * Previous versions of Jackson would output UTC timezones using `UTC`. Newer versions of Jackson instead emit `Z` in
 * all cases. This serializer ensures that Jackson's serialization for ZonedDateTime uses "UTC" instead of "Z" to match
 * the format used by xrpld.</p>
 */
public class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> implements ContextualSerializer {

  // Some XRPL dates have  9 digits of millsecond precision (some have 6). This pattern supports arbitrary values
  // Use literal 'UTC' instead of 'z' pattern to ensure consistent output (z outputs Z for ZoneOffset.UTC)
  private static final DateTimeFormatter DEFAULT_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss[.n] 'UTC'", Locale.US);

  private final DateTimeFormatter formatter;

  public ZonedDateTimeSerializer() {
    this(DEFAULT_FORMATTER);
  }

  private ZonedDateTimeSerializer(DateTimeFormatter formatter) {
    super(ZonedDateTime.class);
    this.formatter = formatter;
  }

  @Override
  public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider)
    throws IOException {
    ZonedDateTime utcValue = value.withZoneSameInstant(ZoneOffset.UTC);
    String formatted = utcValue.format(formatter);
    gen.writeString(formatted);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
      if (format != null && format.hasPattern()) {
        // Replace the 'z' pattern with literal 'UTC' to avoid Z vs UTC issues
        String pattern = format.getPattern().replace(" z", " 'UTC'");
        Locale locale = format.hasLocale() ? format.getLocale() : Locale.US;
        return new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern(pattern, locale));
      }
    }
    return this;
  }
}
