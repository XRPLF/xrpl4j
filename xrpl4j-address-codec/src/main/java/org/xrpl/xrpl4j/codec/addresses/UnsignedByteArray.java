package org.xrpl.xrpl4j.codec.addresses;

import java.util.ArrayList;
import java.util.Collections;
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
   * Creates an {@link UnsignedByteArray} from a byte array.
   *
   * @param bytes The byte array to create an {@link UnsignedByteArray} from.
   * @return An {@link UnsignedByteArray} containing {@code bytes}.
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
   * Creates an {@link UnsignedByteArray} from one or more {@link UnsignedByte} values.
   *
   * @param first An {@link UnsignedByte} to include in the resulting {@link UnsignedByteArray}.
   * @param rest  Other {@link UnsignedByte}s to include in the resulting {@link UnsignedByteArray}.
   * @return An {@link UnsignedByteArray} with the given {@link UnsignedByte}s.
   */
  public static UnsignedByteArray of(UnsignedByte first, UnsignedByte... rest) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>();
    unsignedBytes.add(first);
    Collections.addAll(unsignedBytes, rest);
    return new UnsignedByteArray(unsignedBytes);
  }

  /**
   * Creates an empty {@link UnsignedByteArray}.
   *
   * @return An empty {@link UnsignedByteArray}.
   */
  public static UnsignedByteArray empty() {
    return new UnsignedByteArray(new ArrayList<>());
  }

  /**
   * Creates an {@link UnsignedByteArray} with a given number of bytes (where each byte has the value 0).
   *
   * @param size The size of the initialized {@link UnsignedByteArray}.
   * @return An {@link UnsignedByteArray} of the requested size with all 0 {@link UnsignedByte}s
   */
  public static UnsignedByteArray ofSize(int size) {
    return new UnsignedByteArray(fill(size));
  }

  /**
   * Converts a hex string to an {@link UnsignedByteArray}.
   *
   * @param hex A hexadecimal encoded {@link String}.
   * @return The hex value as an {@link UnsignedByteArray}.
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

  /**
   * Get the underlying {@link List} of {@link UnsignedByte}s for this {@link UnsignedByteArray}.
   *
   * @return The underlying {@link List} of {@link UnsignedByte}s.
   */
  public List<UnsignedByte> getUnsignedBytes() {
    return unsignedBytes;
  }

  /**
   * Converts this {@link UnsignedByteArray} to a byte array.
   *
   * @return This {@link UnsignedByteArray} as a byte array.
   */
  public byte[] toByteArray() {
    byte[] bytes = new byte[unsignedBytes.size()];

    for (int i = 0; i < unsignedBytes.size(); i++) {
      bytes[i] = unsignedBytes.get(i).asByte();
    }

    return bytes;
  }

  /**
   * Get this {@link UnsignedByteArray} as a hex encoded {@link String}.
   *
   * @return This {@link UnsignedByteArray} as a hex encoded {@link String}.
   */
  public String hexValue() {
    return ByteUtils.toHex(unsignedBytes);
  }

  /**
   * Get the length of this {@link UnsignedByteArray}.
   *
   * @return The length of this {@link UnsignedByteArray}, as an int.
   */
  public int length() {
    return unsignedBytes.size();
  }

  /**
   * Gets the {@link UnsignedByte} at a given index.
   *
   * @param index The index of the {@link UnsignedByte} to get.
   * @return The {@link UnsignedByte} at the given index.
   */
  public UnsignedByte get(int index) {
    return unsignedBytes.get(index);
  }

  /**
   * Appends an {@link UnsignedByte} to this {@link UnsignedByteArray}.
   *
   * @param unsignedByte An {@link UnsignedByte} to append.
   * @return This {@link UnsignedByteArray}, with the given {@link UnsignedByte} appended.
   */
  public UnsignedByteArray append(UnsignedByte unsignedByte) {
    unsignedBytes.add(unsignedByte);
    return this;
  }

  /**
   * Appends the given bytes to the end of this array.
   * Note: this method mutates the instance and returns the same instance (mainly for call chaining convenience).
   *
   * @param array An {@link UnsignedByteArray} to append to this {@link UnsignedByteArray}.
   * @return the same instance.
   */
  public UnsignedByteArray append(UnsignedByteArray array) {
    unsignedBytes.addAll(array.getUnsignedBytes());
    return this;
  }

  /**
   * Sets the value at the given index to the given {@link UnsignedByte}.
   *
   * @param index The index to set.
   * @param value The {@link UnsignedByte} to set at the given index.
   */
  public void set(int index, UnsignedByte value) {
    unsignedBytes.set(index, value);
  }

  /**
   * Returns a slice of the underlying byte array from the given start to the end index (exclusive).
   *
   * @param startIndex start index (inclusive)
   * @param endIndex   end index (exclusive)
   * @return An {@link UnsignedByteArray} containing the sliced elements.
   */
  public UnsignedByteArray slice(int startIndex, int endIndex) {
    return new UnsignedByteArray(unsignedBytes.subList(startIndex, endIndex));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof UnsignedByteArray)) {
      return false;
    }
    UnsignedByteArray that = (UnsignedByteArray) object;
    return getUnsignedBytes().equals(that.getUnsignedBytes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUnsignedBytes());
  }

  @Override
  public String toString() {
    return "UnsignedByteArray{" +
      "unsignedBytes=List(size=" + unsignedBytes.size() + ")" +
      '}';
  }
}
