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

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray.of;

import org.junit.jupiter.api.Test;

import java.util.Arrays;


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
    assertThat(of(UnsignedByte.of(0)).hexValue()).isEqualTo("00");
    assertThat(of(UnsignedByte.of(MAX_BYTE)).hexValue()).isEqualTo("FF");
    assertThat(of(UnsignedByte.of(0), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("00FF");
    assertThat(of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((0))).hexValue()).isEqualTo("FF00");
    assertThat(of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("FFFF");
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
  public void unsignedByteArrayEqualsTest() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, MAX_BYTE});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {MAX_BYTE, 0});
    UnsignedByteArray array3 = UnsignedByteArray.of(new byte[] {0, MAX_BYTE});
    UnsignedByteArray array4 = array1;

    assertThat(array1.equals(array1)).isTrue();
    assertThat(array1.equals(array2)).isFalse();
    assertThat(array1.equals(array3)).isTrue();
    assertThat(array1 == array3).isFalse();
    assertThat(array1.toByteArray() == array3.toByteArray()).isFalse();
    assertThat(array1.getUnsignedBytes() == array3.getUnsignedBytes()).isFalse();
    assertThat(array1.equals(array3)).isTrue();
    assertThat(array1.equals(array4)).isTrue();
    assertThat(array1.equals(array2)).isFalse();
    assertThat(array1.equals(array3)).isTrue();
    assertThat(Arrays.equals(array1.toByteArray(), array3.toByteArray())).isTrue();
  }

}
