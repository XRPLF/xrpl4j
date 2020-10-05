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

  public void put(UnsignedByteList list) {
    values.addAll(list.values);
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
