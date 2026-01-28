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

/**
 * A custom Jackson deserializer for {@link ZonedDateTime} usage in xrpl4j.
 *
 * <p>This deserializer is needed to allow newer versions of Jackson to deserialize ZonedDateTime values that conform
 * to the xrpld format, which uses `UTC` for all timezone designations. Newer versions of Jackson instead emit `Z`, so
 * this serializer ensures that Jackson's serialization for ZonedDateTime uses "UTC" instead of "Z".</p>
 */
public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> implements ContextualDeserializer {

  private static final DateTimeFormatter DEFAULT_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z", Locale.US);

  private final DateTimeFormatter formatter;

  public ZonedDateTimeDeserializer() {
    this(DEFAULT_FORMATTER);
  }

  private ZonedDateTimeDeserializer(DateTimeFormatter formatter) {
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
