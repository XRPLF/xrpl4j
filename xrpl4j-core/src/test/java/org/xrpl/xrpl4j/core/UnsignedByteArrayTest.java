package org.xrpl.xrpl4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link UnsignedByteArray}.
 */
public class UnsignedByteArrayTest {

  static byte MAX_BYTE = (byte) 255;

  @Test
  public void ofByteArray() {
    assertThat(UnsignedByteArray.of(new byte[] {0}).hexValue()).isEqualTo("00");
    assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE}).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByteArray.of(new byte[] {0, MAX_BYTE}).hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE, 0}).hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE, MAX_BYTE}).hexValue()).isEqualTo("FFFF");
  }

  @Test
  public void ofUnsignedByteArray() {
    assertThat(UnsignedByteArray.of(UnsignedByte.of(0)).hexValue()).isEqualTo("00");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(MAX_BYTE)).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(0), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((0))).hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((MAX_BYTE))).hexValue())
      .isEqualTo("FFFF");
  }

  @Test
  public void lowerCaseOrUpperCase() {
    assertThat(UnsignedByteArray.fromHex("Ff").hexValue()).isEqualTo("FF");
    assertThat(UnsignedByteArray.fromHex("00fF").hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.fromHex("00ff").hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.fromHex("00FF").hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.fromHex("fF00").hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.fromHex("ff00").hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.fromHex("FF00").hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.fromHex("abcdef0123").hexValue()).isEqualTo("ABCDEF0123");
  }

  @Test
  public void empty() {
    assertThat(UnsignedByteArray.empty()).isEqualTo(UnsignedByteArray.of(new byte[] {}));
    assertThat(UnsignedByteArray.empty().length()).isEqualTo(0);
    assertThat(
      UnsignedByteArray.empty().equals(UnsignedByteArray.of(new byte[] {}))
    ).isTrue();
  }

  @Test
  public void length() {
    final int size = 2;
    assertThat(UnsignedByteArray.of(new byte[] {0, MAX_BYTE}).length()).isEqualTo(size);
    assertThat(UnsignedByteArray.of(new byte[] {0, 1}).length()).isEqualTo(UnsignedByteArray.ofSize(size).length());
    assertThat(UnsignedByteArray.ofSize(size).length()).isEqualTo(size);
  }

  @Test
  public void ofSize() {
    final int size = 2;
    assertThat(UnsignedByteArray.ofSize(size)).isEqualTo(UnsignedByteArray.of(new byte[] {0, 0}));
    assertThat(UnsignedByteArray.ofSize(size).length()).isEqualTo(size);
    assertThat(UnsignedByteArray.ofSize(size).length()).isEqualTo(UnsignedByteArray.of(new byte[] {0, 0}).length());
    assertThat(UnsignedByteArray.ofSize(size).equals(UnsignedByteArray.of(new byte[] {0, 0}))).isTrue();
  }

  @Test
  public void get() {
    byte[] byteArray = new byte[] {0, 1, 2};
    UnsignedByteArray array = UnsignedByteArray.of(byteArray);
    assertThat(array.get(0)).isEqualTo(array.getUnsignedBytes().get(0));
    assertThat(array.get(0).asInt()).isEqualTo(byteArray[0]);
    assertThat(array.get(1).asInt()).isEqualTo(byteArray[1]);
  }

  @Test
  public void appendByte() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {0, 1, 9});
    int initialLength = array1.length();
    assertThat(array1.append(UnsignedByte.of(9))).isEqualTo(array2);
    assertThat(array1.length() - 1).isEqualTo(initialLength);
    assertThat(array2.length()).isEqualTo(initialLength + 1);
  }

  @Test
  public void appendByteArray() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {0, 1, 8, 9});
    int initialLength = array1.length();
    assertThat(array1.append(UnsignedByteArray.of(new byte[] {8, 9}))).isEqualTo(array2);
    assertThat(array1.length()).isEqualTo(initialLength + 2);
    assertThat(array2.length()).isEqualTo(initialLength + 2);
  }

  @Test
  public void fill() {
    List<UnsignedByte> unsignedBytes1 = new ArrayList<>();
    List<UnsignedByte> unsignedBytes2 = Arrays.asList(UnsignedByte.of(0), UnsignedByte.of(0));
    List<UnsignedByte> filledBytes = UnsignedByteArray.fill(2);
    assertThat(unsignedBytes1).isEqualTo(UnsignedByteArray.fill(0));
    assertThat(unsignedBytes1.size()).isEqualTo(0);
    assertThat(unsignedBytes2).isEqualTo(filledBytes);
    assertThat(unsignedBytes2.equals(filledBytes)).isTrue();
  }

  @Test
  public void set() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {0, 9});
    assertThat(array1).isNotEqualTo(array2);
    array1.set(1, UnsignedByte.of(9));
    assertThat(array1).isEqualTo(array2);
  }

  @Test
  public void slice() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 8, 9, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {8, 9});
    assertThat(array1).isNotEqualTo(array2);
    assertThat(array1.slice(1, 3)).isEqualTo(array2);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> array1.slice(1, 5));
  }

  @Test
  public void hashcode() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    assertThat(array1).isNotEqualTo(UnsignedByteArray.of(new byte[] {8, 9}));
    assertThat(array1.hashCode()).isNotEqualTo(UnsignedByteArray.of(new byte[] {8, 9}).hashCode());
    assertThat(array1.hashCode()).isEqualTo(array1.hashCode());
    assertThat(array1.hashCode()).isEqualTo(UnsignedByteArray.of(new byte[] {0, 1}).hashCode());
  }

  @Test
  public void unsignedByteArrayToString() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {8, 9});
    assertThat(array1.toString()).isEqualTo(array1.toString());
    assertThat(array1).isNotEqualTo(array2);
    assertThat(array1.toString()).isEqualTo(array2.toString());
  }

  @Test
  public void unsignedByteArrayEqualsTest() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, MAX_BYTE});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {MAX_BYTE, 0});
    UnsignedByteArray array3 = UnsignedByteArray.of(new byte[] {0, MAX_BYTE});

    assertThat(array1.equals(array1)).isTrue();
    assertThat(array1.equals(array2)).isFalse();
    assertThat(array1.equals(array3)).isTrue();
    assertThat(array1 == array3).isFalse();
    assertThat(array1.toByteArray() == array3.toByteArray()).isFalse();
    assertThat(array1.getUnsignedBytes() == array3.getUnsignedBytes()).isFalse();
    assertThat(array1.equals(array3)).isTrue();
    assertThat(array1.equals(array2)).isFalse();
    assertThat(array1.equals(array3)).isTrue();
    assertThat(Arrays.equals(array1.toByteArray(), array3.toByteArray())).isTrue();
    assertThat(array1.equals(new Object())).isFalse();
  }

}
