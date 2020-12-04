package org.xrpl.xrpl4j.codec.addresses;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper for holding unsigned bytes since unsigned bytes are hard in Java and XRPL ledger does many operations
 * on arrays on unsigned bytes.
 *
 * <p>Note: several of the methods in this class mutate the underlying value.
 */
public class UnsignedByteArray {

  private final List<UnsignedByte> unsignedBytes;

  public UnsignedByteArray(final List<UnsignedByte> unsignedBytes) {
    Objects.requireNonNull(unsignedBytes);
    this.unsignedBytes = unsignedBytes;
  }

  /**
   * Creates an UnsignedByteArray from a byte array.
   *
   * @param bytes
   * @return
   */
  public static UnsignedByteArray of(final byte[] bytes) {
    Objects.requireNonNull(bytes);

    List<UnsignedByte> unsignedBytes = new ArrayList<>(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      unsignedBytes.add(i, UnsignedByte.of(bytes[i]));
    }
    return new UnsignedByteArray(unsignedBytes);
  }

  /**
   * Creates an UnsignedByteArray from one or more UnsignedByte values.
   *
   * @param first
   * @param rest
   * @return
   */
  public static UnsignedByteArray of(UnsignedByte first, UnsignedByte... rest) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>();
    unsignedBytes.add(first);
    for (int i = 0; i < rest.length; i++) {
      unsignedBytes.add(rest[i]);
    }
    return new UnsignedByteArray(unsignedBytes);
  }

  /**
   * Creates an empty UnsignedByteArray.
   *
   * @return
   */
  public static UnsignedByteArray empty() {
    return new UnsignedByteArray(new ArrayList<>());
  }

  /**
   * Creates an UnsignedByteArray with a given number of bytes (where each byte has the value 0)
   *
   * @return
   */
  public static UnsignedByteArray ofSize(int size) {
    return new UnsignedByteArray(fill(size));
  }

  /**
   * Converts a hex string to an UnsignedByteArray.
   *
   * @param hex
   * @return
   */
  public static UnsignedByteArray fromHex(String hex) {
    Objects.requireNonNull(hex);
    List<UnsignedByte> unsignedBytes = ByteUtils.parse(hex);
    return new UnsignedByteArray(unsignedBytes);
  }

  private static List<UnsignedByte> fill(int amount) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      unsignedBytes.add(i, UnsignedByte.of(0));
    }
    return unsignedBytes;
  }

  public List<UnsignedByte> getUnsignedBytes() {
    return unsignedBytes;
  }

  public byte[] toByteArray() {
    byte[] bytes = new byte[unsignedBytes.size()];

    for (int i = 0; i < unsignedBytes.size(); i++) {
      bytes[i] = unsignedBytes.get(i).asByte();
    }

    return bytes;
  }

  public String hexValue() {
    return ByteUtils.toHex(unsignedBytes);
  }

  public int length() {
    return unsignedBytes.size();
  }

  /**
   * Gets the unsigned byte at a given index.
   *
   * @param index
   * @return
   */
  public UnsignedByte get(int index) {
    return unsignedBytes.get(index);
  }

  public UnsignedByteArray append(UnsignedByte unsignedByte) {
    unsignedBytes.add(unsignedByte);
    return this;
  }

  /**
   * Appends the given bytes to the end of this array.
   * Note: this method mutates the instance and returns the same instance (mainly for call chaining convenience).
   *
   * @param array
   * @return the same instance
   */
  public UnsignedByteArray append(UnsignedByteArray array) {
    unsignedBytes.addAll(array.getUnsignedBytes());
    return this;
  }

  /**
   * Sets the value value an unsigned byte at the given index.
   *
   * @param index
   * @param value
   */
  public void set(int index, UnsignedByte value) {
    unsignedBytes.set(index, value);
  }

  /**
   * Returns a slice of the underlying byte array from the given start to the end index (exclusive).
   *
   * @param startIndex start index (inclusive)
   * @param endIndex   end index (exclusive)
   * @return
   */
  public UnsignedByteArray slice(int startIndex, int endIndex) {
    return new UnsignedByteArray(unsignedBytes.subList(startIndex, endIndex));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnsignedByteArray)) {
      return false;
    }
    UnsignedByteArray that = (UnsignedByteArray) o;
    return getUnsignedBytes().equals(that.getUnsignedBytes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUnsignedBytes());
  }
}
