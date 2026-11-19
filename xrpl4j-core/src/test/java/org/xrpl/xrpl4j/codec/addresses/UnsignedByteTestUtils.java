package org.xrpl.xrpl4j.codec.addresses;

import java.lang.reflect.Field;

/**
 * Test-only helpers for {@link UnsignedByte}.
 */
public final class UnsignedByteTestUtils {

  private UnsignedByteTestUtils() {
  }

  /**
   * Forcibly resets a (possibly shared/static) {@link UnsignedByte} to the supplied value and clears its destroyed
   * flag. Used as a safety net so that a regression in defensive copying cannot poison every other test in the fork.
   *
   * @param target The {@link UnsignedByte} to reset.
   * @param value  The value to restore.
   */
  public static void reset(final UnsignedByte target, final int value) {
    try {
      Field valueField = UnsignedByte.class.getDeclaredField("value");
      valueField.setAccessible(true);
      valueField.setInt(target, value);
      Field destroyedField = UnsignedByte.class.getDeclaredField("destroyed");
      destroyedField.setAccessible(true);
      destroyedField.setBoolean(target, false);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }
}
