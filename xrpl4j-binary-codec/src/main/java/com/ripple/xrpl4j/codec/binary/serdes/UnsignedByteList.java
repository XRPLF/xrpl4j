package com.ripple.xrpl4j.codec.binary.serdes;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.codec.binary.ByteUtils;
import com.ripple.xrpl4j.codec.binary.UnsignedByte;

import java.util.ArrayList;
import java.util.List;

public class UnsignedByteList {

  private final List<UnsignedByte> values;

  public UnsignedByteList() {
    this(new ArrayList<>());
  }

  public UnsignedByteList(int size) {
    this();
    fill(size);
  }

  public UnsignedByteList(String hex) {
    this(ByteUtils.parse(hex));
  }

  public UnsignedByteList(UnsignedByte... values) {
    this(Lists.newArrayList(values));
  }

  public UnsignedByteList(List<UnsignedByte> values) {
    this.values = values;
  }

  public UnsignedByteList(byte[] rawBytes) {
    values = new ArrayList<>();
    for(byte rawByte : rawBytes) {
      values.add(UnsignedByte.of(rawByte));
    }
  }

  public int getLength() {
    return values.size();
  }

  public UnsignedByte get(int index) {
    return values.get(index);
  }

  public void put(UnsignedByteList list) {
    values.addAll(list.values);
  }

  public void set(int i, UnsignedByte of) {
    values.set(i, of);
  }

  public UnsignedByteList slice(int startIndex, int endIndex) {
    return new UnsignedByteList(values.subList(startIndex, endIndex));
  }

  private void fill(int amount) {
    for (int i = 0; i < amount; i++) {
      values.add(i, UnsignedByte.of(0));
    }
  }

  public void toByteSink(UnsignedByteList bytes) {
    bytes.values.addAll(values);
  }

  public String toHex() {
    return ByteUtils.toHex(values);
  }

  public byte[] toBytes() {
    return BaseEncoding.base16().decode(toHex());
  }

}
