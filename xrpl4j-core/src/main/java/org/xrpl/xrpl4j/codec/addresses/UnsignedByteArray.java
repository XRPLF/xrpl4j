package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: address-codec
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.google.common.annotations.Beta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.security.auth.Destroyable;

/**
 * <p>Wrapper for holding unsigned bytes since unsigned bytes are hard in Java and XRPL ledger does many operations on
 * arrays on unsigned bytes.</p>
 *
 * <p>Note: several of the methods in this class mutate the underlying value.</p>
 */
public class UnsignedByteArray implements Destroyable {

  private final List<UnsignedByte> unsignedBytes;
  private boolean destroyed;

  /**
   * Required-args constructor.
   *
   * <p>Note: {@link UnsignedByte} is mutable (see {@link UnsignedByte#destroy()}), so this constructor defensively
   * copies both the supplied {@link List} and every {@link UnsignedByte} in it. This guarantees that destroying this
   * array can never mutate an {@link UnsignedByte} owned by the caller (e.g., a shared static constant).
   *
   * @param unsignedBytes A {@link List} of {@link UnsignedByte}s.
   */
  public UnsignedByteArray(final List<UnsignedByte> unsignedBytes) {
    Objects.requireNonNull(unsignedBytes);
    this.unsignedBytes = copyOf(unsignedBytes);
  }

  /**
   * Private constructor that takes ownership of {@code unsignedBytes} without copying. Only for use by
   * factories in this class that have just allocated fresh {@link UnsignedByte} instances.
   *
   * @param unsignedBytes A freshly allocated {@link List} of freshly allocated {@link UnsignedByte}s.
   * @param ignored       Disambiguates this constructor from the public one.
   */
  private UnsignedByteArray(final List<UnsignedByte> unsignedBytes, final boolean ignored) {
    this.unsignedBytes = Objects.requireNonNull(unsignedBytes);
  }

  /**
   * Deep-copies a {@link List} of {@link UnsignedByte}s so the result shares no instances with the input.
   *
   * @param source The {@link List} to copy.
   *
   * @return A new {@link List} containing new {@link UnsignedByte} instances with equal values.
   */
  private static List<UnsignedByte> copyOf(final List<UnsignedByte> source) {
    final List<UnsignedByte> copy = new ArrayList<>(source.size());
    for (UnsignedByte unsignedByte : source) {
      copy.add(UnsignedByte.of(unsignedByte));
    }
    return copy;
  }

  /**
   * Creates an {@link UnsignedByteArray} from a byte array.
   *
   * @param bytes The byte array to create an {@link UnsignedByteArray} from.
   *
   * @return An {@link UnsignedByteArray} containing {@code bytes}.
   */
  public static UnsignedByteArray of(final byte[] bytes) {
    Objects.requireNonNull(bytes);

    List<UnsignedByte> unsignedBytes = new ArrayList<>(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      unsignedBytes.add(i, UnsignedByte.of(bytes[i]));
    }
    return new UnsignedByteArray(unsignedBytes, true); // <-- all instances freshly allocated above.
  }

  /**
   * Creates an {@link UnsignedByteArray} from one or more {@link UnsignedByte} values. The supplied values are copied,
   * so the returned array never aliases the caller's instances.
   *
   * @param first An {@link UnsignedByte} to include in the resulting {@link UnsignedByteArray}.
   * @param rest  Other {@link UnsignedByte}s to include in the resulting {@link UnsignedByteArray}.
   *
   * @return An {@link UnsignedByteArray} with the given {@link UnsignedByte}s.
   */
  public static UnsignedByteArray of(UnsignedByte first, UnsignedByte... rest) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>(1 + rest.length);
    unsignedBytes.add(UnsignedByte.of(first));
    for (UnsignedByte unsignedByte : rest) {
      unsignedBytes.add(UnsignedByte.of(unsignedByte));
    }
    return new UnsignedByteArray(unsignedBytes, true); // <-- all instances freshly copied above.
  }

  /**
   * Creates an empty {@link UnsignedByteArray}.
   *
   * @return An empty {@link UnsignedByteArray}.
   */
  public static UnsignedByteArray empty() {
    return new UnsignedByteArray(new ArrayList<>(), true);
  }

  /**
   * Creates an {@link UnsignedByteArray} with a given number of bytes (where each byte has the value 0).
   *
   * @param size The size of the initialized {@link UnsignedByteArray}.
   *
   * @return An {@link UnsignedByteArray} of the requested size with all 0 {@link UnsignedByte}s
   */
  public static UnsignedByteArray ofSize(int size) {
    return new UnsignedByteArray(fill(size), true); // <-- `fill` allocates fresh instances.
  }

  /**
   * Converts a hex string to an {@link UnsignedByteArray}.
   *
   * @param hex A hexadecimal encoded {@link String}.
   *
   * @return The hex value as an {@link UnsignedByteArray}.
   */
  public static UnsignedByteArray fromHex(String hex) {
    Objects.requireNonNull(hex);
    List<UnsignedByte> unsignedBytes = ByteUtils.parse(hex.toUpperCase(Locale.ENGLISH));
    return new UnsignedByteArray(unsignedBytes, true); // <-- `parse` allocates fresh instances.
  }

  protected static List<UnsignedByte> fill(int amount) {
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
   * Get this {@link UnsignedByteArray} as an upper-cased Hex-encoded {@link String}.
   *
   * @return This {@link UnsignedByteArray} as a hex encoded {@link String}.
   */
  public String hexValue() {
    return ByteUtils.toHex(unsignedBytes).toUpperCase(Locale.ENGLISH);
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
   *
   * @return The {@link UnsignedByte} at the given index.
   */
  public UnsignedByte get(int index) {
    return unsignedBytes.get(index);
  }

  /**
   * Appends a copy of an {@link UnsignedByte} to this {@link UnsignedByteArray}. Note: this method mutates the instance
   * and returns the same instance (mainly for call chaining convenience).
   *
   * @param unsignedByte An {@link UnsignedByte} to append (copied; the caller's instance is never aliased).
   *
   * @return This {@link UnsignedByteArray}, with the given {@link UnsignedByte} appended.
   */
  public UnsignedByteArray append(UnsignedByte unsignedByte) {
    unsignedBytes.add(UnsignedByte.of(unsignedByte));
    return this;
  }

  /**
   * Appends copies of the given bytes to the end of this array. Note: this method mutates the instance and returns the
   * same instance (mainly for call chaining convenience). The elements of {@code array} are copied, so the two arrays
   * never share {@link UnsignedByte} instances.
   *
   * @param array An {@link UnsignedByteArray} to append to this {@link UnsignedByteArray}.
   *
   * @return the same instance.
   */
  public UnsignedByteArray append(UnsignedByteArray array) {
    unsignedBytes.addAll(copyOf(array.getUnsignedBytes()));
    return this;
  }

  /**
   * Sets the value at the given index to a copy of the given {@link UnsignedByte}.
   *
   * @param index The index to set.
   * @param value The {@link UnsignedByte} to set at the given index (copied; the caller's instance is never aliased).
   */
  public void set(int index, UnsignedByte value) {
    unsignedBytes.set(index, UnsignedByte.of(value));
  }

  /**
   * Returns a slice of the underlying byte array from the given start to the end index (exclusive).
   *
   * @param startIndex start index (inclusive)
   * @param endIndex   end index (exclusive)
   *
   * @return A new, independent {@link UnsignedByteArray} containing copies of the sliced elements.
   */
  public UnsignedByteArray slice(int startIndex, int endIndex) {
    return new UnsignedByteArray(unsignedBytes.subList(startIndex, endIndex)); // <-- public ctor copies.
  }

  /**
   * Returns a new {@link UnsignedByteArray} with the bytes in reversed order.
   * This is useful for converting between big-endian and little-endian byte order.
   *
   * @return A new {@link UnsignedByteArray} with bytes in reversed order.
   */
  @Beta
  public UnsignedByteArray reverse() {
    List<UnsignedByte> reversed = copyOf(unsignedBytes);
    Collections.reverse(reversed);
    return new UnsignedByteArray(reversed, true); // <-- `copyOf` allocates fresh instances.
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

  @Override
  public void destroy() {
    this.unsignedBytes.forEach(UnsignedByte::destroy);
    this.unsignedBytes.clear();
    this.destroyed = true;
  }

  @Override
  public boolean isDestroyed() {
    return this.destroyed;
  }
}
