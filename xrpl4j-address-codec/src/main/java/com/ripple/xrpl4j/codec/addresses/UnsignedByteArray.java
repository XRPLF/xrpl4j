package com.ripple.xrpl4j.codec.addresses;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UnsignedByteArray {

  private final List<UnsignedByte> unsignedBytes;

  public static UnsignedByteArray of(final byte[] bytes) {
    Objects.requireNonNull(bytes);

    List<UnsignedByte> unsignedBytes = new ArrayList<>(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      unsignedBytes.add(i, UnsignedByte.of(bytes[i]));
    }
    return new UnsignedByteArray(unsignedBytes);
  }

  public static UnsignedByteArray of(UnsignedByte first, UnsignedByte... rest) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>();
    unsignedBytes.add(first);
    for (int i = 0; i < rest.length; i++) {
      unsignedBytes.add(i, rest[i]);
    }
    return new UnsignedByteArray(unsignedBytes);
  }

  public static UnsignedByteArray empty() {
    return new UnsignedByteArray(new ArrayList<>());
  }

  public static UnsignedByteArray ofSize(int size) {
    return new UnsignedByteArray(fill(size));
  }

  public static UnsignedByteArray fromHex(String hex) {
    Objects.requireNonNull(hex);
    List<UnsignedByte> unsignedBytes = ByteUtils.parse(hex);
    return new UnsignedByteArray(unsignedBytes);
  }

  public UnsignedByteArray(final List<UnsignedByte> unsignedBytes) {
    Objects.requireNonNull(unsignedBytes);
    this.unsignedBytes = unsignedBytes;
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

  public UnsignedByteArray concat(final UnsignedByteArray bytes) {
    Objects.requireNonNull(bytes);

    unsignedBytes.addAll(bytes.getUnsignedBytes());
    return this;
  }

  public int length() {
    return unsignedBytes.size();
  }

  public UnsignedByte get(int index) {
    return unsignedBytes.get(index);
  }

  public void add(UnsignedByteArray list) {
    unsignedBytes.addAll(list.getUnsignedBytes());
  }

  public void set(int i, UnsignedByte of) {
    unsignedBytes.set(i, of);
  }

  public UnsignedByteArray slice(int startIndex, int endIndex) {
    return new UnsignedByteArray(unsignedBytes.subList(startIndex, endIndex));
  }

  private static List<UnsignedByte> fill(int amount) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      unsignedBytes.add(i, UnsignedByte.of(0));
    }
    return unsignedBytes;
  }

  public void toByteSink(UnsignedByteArray bytes) {
    bytes.unsignedBytes.addAll(unsignedBytes);
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
