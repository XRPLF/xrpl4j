package org.xrpl.xrpl4j.model.client.serverinfo;

import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for dealing with XRP Ledger ranges.
 */
public class LedgerRangeUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(LedgerRangeUtils.class);

  /**
   * Transforms completeLedgers() part of various ServerInfo responses from a range expression to a
   * {@code List<Range<UnsignedLong>>}.
   *
   * @param completeLedgers A hyphen separated {@link String} containing ledger range. e.g. "2-6000798"
   *
   * @return A {@link List} of {@link Range} of type {@link UnsignedLong} containing the range of ledgers that a rippled
   *   node contains in its history.
   */
  public static List<Range<UnsignedLong>> completeLedgerRanges(String completeLedgers) {
    // Split completeLedgers by comma...
    return Stream.of(completeLedgers.split(","))
      .map(String::trim)
      .filter($ -> !$.equals("empty")) // <-- `empty` is a valid value for completed ledgers.
      .map(range -> {
        final String[] parts = range.split("-");
        if (parts.length == 1) {
          try {
            return Range.singleton(UnsignedLong.valueOf(parts[0]));
          } catch (Exception e) {
            return null; // <-- filtered out of the ultimate List below.
          }
        }
        if (parts.length == 2) {
          final UnsignedLong lower;
          final UnsignedLong upper;
          try {
            lower = UnsignedLong.valueOf(parts[0]);
          } catch (Exception e) {
            LOGGER.warn("Unable to parse valid lower bound number (ignoring range).", e);
            return null; // <-- filtered out of the ultimate List below.
          }

          try {
            upper = UnsignedLong.valueOf(parts[1]);
          } catch (Exception e) {
            LOGGER.warn("Unable to parse valid upper bound number (ignoring range).", e);
            return null; // <-- filtered out of the ultimate List below.
          }
          return Range.closed(lower, upper);
        } else {
          LOGGER.warn("Range had too many dashes (ignoring range)");
          return null; // <-- filtered out of the ultimate List below.
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
}
