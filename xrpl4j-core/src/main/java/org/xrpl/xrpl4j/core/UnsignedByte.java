package org.xrpl.xrpl4j.core;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.util.Objects;
import javax.security.auth.Destroyable;

/**
 * Unsigned byte where value can be 0 to 255.
 */
public class UnsignedByte implements Destroyable {

  // since Java byte is signed need to use int to handle max value 255
  private int value;
  private boolean destroyed;

  private UnsignedByte(final int value) {
    Preconditions.checkArgument(value >= 0);
    Preconditions.checkArgument(value <= 255);
    this.value = value;
  }

  /**
   * Construct an {@link UnsignedByte} from an {@code int}.
   *
   * @param value An {@code int} value.
   *
   * @return An {@link UnsignedByte}.
   */
  public static UnsignedByte of(final int value) {
    return new UnsignedByte(value);
  }

  /**
   * Construct an {@link UnsignedByte} from a {@code byte}.
   *
   * @param value A {@code byte} value.
   *
   * @return An {@link UnsignedByte}.
   */
  public static UnsignedByte of(final byte value) {
    return new UnsignedByte(value & 0xff);
  }

  /**
   * Construct an {@link UnsignedByte} from high bits and low bits.
   *
   * @param highBits A {@code byte} of the high bits.
   * @param lowBits  A {@code byte} of the low bits.
   *
   * @return An {@link UnsignedByte}.
   */
  public static UnsignedByte of(final byte highBits, final byte lowBits) {
    return new UnsignedByte((highBits << 4) + lowBits);
  }

  /**
   * Construct an {@link UnsignedByte} from a hexadecimal {@link String}.
   *
   * @param hex A hexadecimal encoded {@link String}.
   *
   * @return An {@link UnsignedByte}.
   */
  public static UnsignedByte of(final String hex) {
    Objects.requireNonNull(hex);
    byte highBits = new BigInteger(hex.substring(0, 1), 16).byteValue();
    byte lowBits = new BigInteger(hex.substring(1, 2), 16).byteValue();
    return UnsignedByte.of(highBits, lowBits);
  }

  /**
   * Converts the {@link UnsignedByte} to a signed int. Necessary if the {@link UnsignedByte} has a value greater than
   * 127 and the result needs to be used for numeric purposes.
   *
   * @return This {@link UnsignedByte}'s value as a signed int.
   */
  public int asInt() {
    return value;
  }

  /**
   * Converts the {@link UnsignedByte} to a signed byte. Note that this can be unsafe to do if the underlying value is
   * greater than 127 which is the max value for signed byte in Java AND the byte is being used for numerical purposes.
   *
   * @return This {@link UnsignedByte}'s value as a signed byte.
   */
  public byte asByte() {
    return (byte) value;
  }

  /**
   * Gets the 4 high order bits of the underlying {@link UnsignedByte}.
   *
   * @return The 4 high order bits of this {@link UnsignedByte}.
   */
  public int getHighBits() {
    return value >> 4;
  }

  /**
   * Gets the 4 low order bits of the underlying {@link UnsignedByte}.
   *
   * @return The 4 low order bits of this {@link UnsignedByte}.
   */
  public int getLowBits() {
    return value & 0x0F;
  }

  /**
   * Checks if the nth bit (1-based index from left to right) is set.
   *
   * @param nth The index of the bit to check.
   *
   * @return {@code true} if the nth bit is set, otherwise {@code false}.
   */
  public boolean isNthBitSet(final int nth) {
    Preconditions.checkArgument(nth >= 1 && nth <= 8);
    return ((value >> (8 - nth)) & 1) == 1;
  }

  /**
   * Does a bitwise OR on this {@link UnsignedByte} and the given {@link UnsignedByte}, and returns a new {@link
   * UnsignedByte} as the result.
   *
   * @param unsignedByte The {@link UnsignedByte} to perform a bitwise OR on.
   *
   * @return The result of the bitwise OR operation.
   */
  public UnsignedByte or(final UnsignedByte unsignedByte) {
    return UnsignedByte.of(value | unsignedByte.value);
  }

  /**
   * Encodes this {@link UnsignedByte} as a hexadecimal {@link String}.
   *
   * @return The hex {@link String} value of this {@link UnsignedByte}.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(new byte[] {(byte) asInt()});
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof UnsignedByte)) {
      return false;
    }
    UnsignedByte that = (UnsignedByte) object;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public void destroy() {
    this.value = 0;
    this.destroyed = true;
  }

  @Override
  public boolean isDestroyed() {
    return this.destroyed;
  }
}
