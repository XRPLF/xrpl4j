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

import com.google.common.base.Preconditions;

import java.math.BigInteger;
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

  public UnsignedByteArray(final List<UnsignedByte> unsignedBytes) {
    Objects.requireNonNull(unsignedBytes);
    this.unsignedBytes = unsignedBytes;
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
    return new UnsignedByteArray(unsignedBytes);
  }

  /**
   * Creates an {@link UnsignedByteArray} from one or more {@link UnsignedByte} values.
   *
   * @param first An {@link UnsignedByte} to include in the resulting {@link UnsignedByteArray}.
   * @param rest  Other {@link UnsignedByte}s to include in the resulting {@link UnsignedByteArray}.
   *
   * @return An {@link UnsignedByteArray} with the given {@link UnsignedByte}s.
   */
  public static UnsignedByteArray of(UnsignedByte first, UnsignedByte... rest) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>();
    unsignedBytes.add(first);
    Collections.addAll(unsignedBytes, rest);
    return new UnsignedByteArray(unsignedBytes);
  }

  /**
   * Creates an {@link UnsignedByteArray} from the bytes of a supplied {@link BigInteger}. If the length of the
   * resulting array is not at least {@code minFinalByteLength}, then the result is prefix padded with `0x00` bytes
   * until the final array length is {@code minFinalByteLength}.
   *
   * <p>This function primarily exists to ensure that transformation of secp256k1 private keys from one form to another
   * (e.g., from {@link BigInteger} to a byte array) are done in a consistent manner, always yielding the desired number
   * of bytes. For example, secp256k1 private keys are 32-bytes long naturally. However, when transformed to a byte
   * array via {@link BigInteger#toByteArray()}, the result will not always have the same number of leading zero bytes
   * that one might expect. Sometimes the returned array will have 33 bytes, one of which is a zero-byte prefix pad that
   * is meant to ensure the underlying number is not represented as a negative number. Other times, the array will have
   * fewer than 32 bytes, for example 31 or even 30, if the byte array has redundant leading zero bytes.
   *
   * <p>Thus, this function can be used to normalize a byte array with a desired number of 0-byte padding to ensure
   * that the resulting byte array is always the desired {@code minFinalByteLength} (e.g., in this library, secp256k1
   * private keys should always be comprised of a 32-byte natural private key with a one-byte `0x00` prefix pad).
   *
   * @param amount             A {@link BigInteger} to convert into an {@link UnsignedByteArray}.
   * @param minFinalByteLength The minimum length, in bytes, that the final result must be. If the final byte length is
   *                           less than this number, the resulting array will be prefix padded to increase its length
   *                           to this number.
   *
   * @return An {@link UnsignedByteArray} with a length of at least {@code minFinalByteLength}.
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/486"
   */
  public static UnsignedByteArray from(final BigInteger amount, int minFinalByteLength) {
    Objects.requireNonNull(amount);
    Preconditions.checkArgument(amount.signum() >= 0, "amount must not be negative");
    Preconditions.checkArgument(minFinalByteLength >= 0, "minFinalByteLength must not be negative");

    // Return the `amount` as an UnsignedByteArray, but with the proper zero-byte prefix padding.
    return withPrefixPadding(amount.toByteArray(), minFinalByteLength);
  }

  /**
   * Construct a new {@link UnsignedByteArray} that contains this byte array's bytes, but with enough `0x00` prefix
   * padding bytes such that the final length of the returned value is {@code minFinalByteLength}.
   *
   * @param minFinalByteLength The minimum length, in bytes, that the final result must be zero-byte prefix-padded to.
   *                           If this number is greater-than {@code #length}, then this value will be reduced to
   *                           {@code #length}.
   *
   * @return A copy of this {@link UnsignedByteArray} that has been zero-byte prefix-padded such that its final length
   *   is at least {@code minFinalByteLength}.
   */
  public UnsignedByteArray withPrefixPadding(int minFinalByteLength) {
    Preconditions.checkArgument(minFinalByteLength >= 0, "minFinalByteLength must not be negative");

    return withPrefixPadding(this.toByteArray(), minFinalByteLength);
  }

  /**
   * Creates a new {@link UnsignedByteArray} from a byte array by prefixing enough zero-pad bytes to ensure that the
   * result has at least {@code minFinalByteLength} bytes.
   *
   * @param bytes              A byte array to convert into an {@link UnsignedByteArray}.
   * @param minFinalByteLength The minimum length, in bytes, that the final result must be prefix-padded to. If this
   *                           number is greater-than {@code amount#length}, then this value will be reduced to
   *                           {@code amount#length}.
   *
   * @return A zero-byte prefix-padded {@link UnsignedByteArray} with a length of at least {@code minFinalByteLength}.
   */
  private static UnsignedByteArray withPrefixPadding(final byte[] bytes, int minFinalByteLength) {
    Preconditions.checkArgument(minFinalByteLength >= 0, "minFinalByteLength must not be negative");

    if (bytes.length > minFinalByteLength) { // <-- Increase `minFinalByteLength` to be at least bytes.length
      minFinalByteLength = bytes.length;
    }

    final int numPadBytes = minFinalByteLength - bytes.length; // <-- numPadBytes will never be negative
    byte[] resultBytes = new byte[minFinalByteLength];
    System.arraycopy(bytes, 0, resultBytes, numPadBytes, bytes.length);
    return UnsignedByteArray.of(resultBytes);
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
   *
   * @return An {@link UnsignedByteArray} of the requested size with all 0 {@link UnsignedByte}s
   */
  public static UnsignedByteArray ofSize(int size) {
    return new UnsignedByteArray(fill(size));
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
    return new UnsignedByteArray(unsignedBytes);
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
   * Appends an {@link UnsignedByte} to this {@link UnsignedByteArray}.
   *
   * @param unsignedByte An {@link UnsignedByte} to append.
   *
   * @return This {@link UnsignedByteArray}, with the given {@link UnsignedByte} appended.
   */
  public UnsignedByteArray append(UnsignedByte unsignedByte) {
    unsignedBytes.add(unsignedByte);
    return this;
  }

  /**
   * Appends the given bytes to the end of this array. Note: this method mutates the instance and returns the same
   * instance (mainly for call chaining convenience).
   *
   * @param array An {@link UnsignedByteArray} to append to this {@link UnsignedByteArray}.
   *
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
   *
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
