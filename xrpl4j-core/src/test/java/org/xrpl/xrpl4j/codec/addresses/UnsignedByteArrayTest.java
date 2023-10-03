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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Unit tests for {@link UnsignedByteArray}.
 */
public class UnsignedByteArrayTest {

  static byte MAX_BYTE = (byte) 255;

  /////////
  // of(byte[])
  /////////

  @Test
  void ofByteArray() {
    assertThat(UnsignedByteArray.of(new byte[] {0}).hexValue()).isEqualTo("00");
    assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE}).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByteArray.of(new byte[] {0, MAX_BYTE}).hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE, 0}).hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE, MAX_BYTE}).hexValue()).isEqualTo("FFFF");
  }

  /////////
  // from(BigInteger)
  /////////

  @Test
  void fromBigIntegerWithNull() {
    assertThrows(NullPointerException.class, () -> UnsignedByteArray.from(
      null, // <-- The crux of the test
      0
    ));
  }

  @Test
  void fromBigIntegerWithNegativeAmount() {
    assertThrows(IllegalArgumentException.class, () -> UnsignedByteArray.from(
      BigInteger.valueOf(-1L), // <-- The crux of the test
      0
    ));
  }

  @Test
  void fromBigIntegerWithNegativeLength() {
    assertThrows(IllegalArgumentException.class, () -> UnsignedByteArray.from(
      BigInteger.valueOf(1L),
      -1 // <-- The crux of the test
    ));
  }

  @Test
  void fromBigIntegerWithZeroLength() {
    assertThat(UnsignedByteArray.from(
        BigInteger.valueOf(1L),
        0 // <-- The crux of the test
      )
      .hexValue()).isEqualTo("01");
  }

  @ParameterizedTest
  @CsvSource( {
    // A BigInteger comprised of 33 Bytes; toByteArray has 33 bytes; 0 extra padding added
    "107371972967791294617431936514364612285184717182742299921102689634201232605691," + // <-- The BigInteger
      "ED6262116F8D51F1FDD98C184F74CA48DDA7B049CB741F1F7A0564B88FE601FB," + // .toString(16)
      "00ED6262116F8D51F1FDD98C184F74CA48DDA7B049CB741F1F7A0564B88FE601FB," + // .toByteArray()
      "00ED6262116F8D51F1FDD98C184F74CA48DDA7B049CB741F1F7A0564B88FE601FB", // <-- Padded to 33 bytes
    // A BigInteger comprised of 33 Bytes; toByteArray has 33 bytes; 0 extra padding added
    "84513109120471239583994879976286018548016554258021069224677925571161262209437," + // <-- The BigInteger
      "BAD8B981A239980B1EC4CB901D698DDE7AA15F264D9537C7D141EE119DD5399D," + // .toString(16)
      "00BAD8B981A239980B1EC4CB901D698DDE7AA15F264D9537C7D141EE119DD5399D," + // .toByteArray()
      "00BAD8B981A239980B1EC4CB901D698DDE7AA15F264D9537C7D141EE119DD5399D", // <-- BigInteger Hex, Padded to 33 bytes
    // A BigInteger comprised of 32 Bytes; toByteArray has 32 bytes; 1 extra padding added
    "8427551091932113544047724072139537481003293113704693219824523888925672289487," + // <-- The BigInteger
      "12A1D32B744B18FA0186A44F32D9241869FA0A05B5B831F188831A07163534CF," + // .toString(16)
      "12A1D32B744B18FA0186A44F32D9241869FA0A05B5B831F188831A07163534CF," + // .toByteArray()
      "0012A1D32B744B18FA0186A44F32D9241869FA0A05B5B831F188831A07163534CF", // <-- BigInteger Hex, Padded to 33 bytes
    // A BigInteger comprised of 32 Bytes; toByteArray has 32 bytes; 1 extra padding added
    "49026876502144691037964633390198098042987098960613207831256521276903508291997," + // <-- The BigInteger
      "6C643A8EB51D365F3FF5B08C575DEA44B0D3CA5795BDD7B080A7057ABB9A319D," + // .toString(16)
      "6C643A8EB51D365F3FF5B08C575DEA44B0D3CA5795BDD7B080A7057ABB9A319D," + // .toByteArray()
      "006C643A8EB51D365F3FF5B08C575DEA44B0D3CA5795BDD7B080A7057ABB9A319D", // <-- BigInteger Hex, Padded to 33 bytes
    // A BigInteger comprised of 31 Bytes; toByteArray has 31 bytes; 2 extra padding added
    "125364023161033659590032058970590371956067907570302268576097734468145372487," + // <-- The BigInteger
      "46F41A0ECE7D0C61B5B36EA377E20621E23C13BD0ABBAEF80754180E9DDD47," + // .toString(16)
      "46F41A0ECE7D0C61B5B36EA377E20621E23C13BD0ABBAEF80754180E9DDD47," + // .toByteArray()
      "000046F41A0ECE7D0C61B5B36EA377E20621E23C13BD0ABBAEF80754180E9DDD47", // <-- BigInteger Hex, Padded to 33 bytes
    // A BigInteger comprised of 31 Bytes; toByteArray has 31 bytes; 2 extra padding added
    "194815934319126504488398097255143744553440248783815166056530734282223472643," + // <-- The BigInteger
      "6E430C9E47DFB2194C97385CC85C406DC69773145AE5DE6060821AAAAD5003," + // .toString(16)
      "6E430C9E47DFB2194C97385CC85C406DC69773145AE5DE6060821AAAAD5003," + // .toByteArray()
      "00006E430C9E47DFB2194C97385CC85C406DC69773145AE5DE6060821AAAAD5003", // <-- BigInteger Hex, Padded to 33 bytes
    // A BigInteger comprised of 30 Bytes; toByteArray has 30 bytes; 3 extra padding added
    "116983811426126878045574354873599490265363256342001470285420476536129339," + // <-- The BigInteger
      "10F32BB4E0B8B00469196EDACCCAA87A55409FF1C66330D7590449C7073B," + // .toString(16)
      "10F32BB4E0B8B00469196EDACCCAA87A55409FF1C66330D7590449C7073B," + // .toByteArray()
      "00000010F32BB4E0B8B00469196EDACCCAA87A55409FF1C66330D7590449C7073B", // <-- BigInteger Hex, Padded to 33 bytes
    // A BigInteger comprised of 30 Bytes; toByteArray has 30 bytes; 3 extra padding added
    "95191719494323154714287471792160232496133380576373117355131561137428728," + // <-- The BigInteger
      "DCADB6BD9E78F0ECE39BB26928F8E4B2A5C7F9CF62C15C1C554B5F458F8," + // .toString(16)
      "0DCADB6BD9E78F0ECE39BB26928F8E4B2A5C7F9CF62C15C1C554B5F458F8," + // .toByteArray()
      "0000000DCADB6BD9E78F0ECE39BB26928F8E4B2A5C7F9CF62C15C1C554B5F458F8", // <-- BigInteger Hex, Padded to 33 bytes
  })
  void fromBigIntegerWithZeroPaddingBytes(
    final String amountString,
    final String amountToString16,
    final String amountToByteArrayHexUnpadded,
    final String amountToByteArrayHexPrefixPadded
  ) {
    final BigInteger amount = new BigInteger(amountString);
    // NOTE `amount.toString(16)` strips off all leading 0s, even in a nibble (beware of using this in actual impl code)
    assertThat(amount.toString(16).toUpperCase(Locale.ENGLISH)).isEqualTo(amountToString16);
    assertThat(BaseEncoding.base16().encode(amount.toByteArray())).isEqualTo(amountToByteArrayHexUnpadded);
    assertThat(UnsignedByteArray.from(amount, 33).hexValue()).isEqualTo(amountToByteArrayHexPrefixPadded);
  }

  @Test
  void fromBigIntegerWithNumberGreaterThan33Bytes() {
    final BigInteger amount = new BigInteger(
      "194815934319126504488398097255143744553440248783815166056530734282223472643" +
        "194815934319126504488398097255143744553440248783815166056530734282223472643");
    // NOTE `amount.toString(16)` strips off all leading 0s, even in a nibble (beware of using this in actual impl code)
    assertThat(amount.toString(16).toUpperCase(Locale.ENGLISH)).isEqualTo(
      "F3C607BB6CA7C4C335A24D0302484D16956259AC4510289E3E77A87BD72F36D1EED47F97D33F05F1715F603B45E83748DE37C087" +
        "9DDE6060821AAAAD5003"
    );
    assertThat(BaseEncoding.base16().encode(amount.toByteArray())).isEqualTo(
      "00F3C607BB6CA7C4C335A24D0302484D16956259AC4510289E3E77A87BD72F36D1EED47F97D33F05F1715F603B45E83748DE37C087" +
        "9DDE6060821AAAAD5003");
    assertThat(UnsignedByteArray.from(amount, 33).hexValue()).isEqualTo(
      "00F3C607BB6CA7C4C335A24D0302484D16956259AC4510289E3E77A87BD72F36D1EED47F97D33F05F1715F603B45E83748DE37C087" +
        "9DDE6060821AAAAD5003");
  }

  /////////
  // withPrefixPadding
  /////////

  @Test
  void withPrefixPadding() {
    final byte[] bytes32 = new byte[32];
    final UnsignedByteArray uba32 = UnsignedByteArray.of(bytes32);
    final byte[] bytes33 = new byte[33];
    final UnsignedByteArray uba33 = UnsignedByteArray.of(bytes33);

    assertThrows(IllegalArgumentException.class, () -> uba32.withPrefixPadding(-1));
    assertThat(uba32.withPrefixPadding(0)).isEqualTo(uba32);
    assertThat(uba32.withPrefixPadding(1)).isEqualTo(uba32);
    assertThat(uba32.withPrefixPadding(32)).isEqualTo(uba32);
    assertThat(uba32.withPrefixPadding(33)).isEqualTo(uba33);
  }

  /////////
  // of(UnsignedByteArray)
  /////////

  @Test
  void ofUnsignedByteArray() {
    assertThat(UnsignedByteArray.of(UnsignedByte.of(0)).hexValue()).isEqualTo("00");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(MAX_BYTE)).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(0), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("00FF");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((0))).hexValue()).isEqualTo("FF00");
    assertThat(UnsignedByteArray.of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((MAX_BYTE))).hexValue())
      .isEqualTo("FFFF");
  }

  @Test
  void lowerCaseOrUpperCase() {
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
  void empty() {
    assertThat(UnsignedByteArray.empty()).isEqualTo(UnsignedByteArray.of(new byte[] {}));
    assertThat(UnsignedByteArray.empty().length()).isEqualTo(0);
    assertThat(
      UnsignedByteArray.empty().equals(UnsignedByteArray.of(new byte[] {}))
    ).isTrue();
  }

  @Test
  void length() {
    final int size = 2;
    assertThat(UnsignedByteArray.of(new byte[] {0, MAX_BYTE}).length()).isEqualTo(size);
    assertThat(UnsignedByteArray.of(new byte[] {0, 1}).length()).isEqualTo(UnsignedByteArray.ofSize(size).length());
    assertThat(UnsignedByteArray.ofSize(size).length()).isEqualTo(size);
  }

  @Test
  void ofSize() {
    final int size = 2;
    assertThat(UnsignedByteArray.ofSize(size)).isEqualTo(UnsignedByteArray.of(new byte[] {0, 0}));
    assertThat(UnsignedByteArray.ofSize(size).length()).isEqualTo(size);
    assertThat(UnsignedByteArray.ofSize(size).length()).isEqualTo(UnsignedByteArray.of(new byte[] {0, 0}).length());
    assertThat(UnsignedByteArray.ofSize(size).equals(UnsignedByteArray.of(new byte[] {0, 0}))).isTrue();
  }

  @Test
  void get() {
    byte[] byteArray = new byte[] {0, 1, 2};
    UnsignedByteArray array = UnsignedByteArray.of(byteArray);
    assertThat(array.get(0)).isEqualTo(array.getUnsignedBytes().get(0));
    assertThat(array.get(0).asInt()).isEqualTo(byteArray[0]);
    assertThat(array.get(1).asInt()).isEqualTo(byteArray[1]);
  }

  @Test
  void appendByte() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {0, 1, 9});
    int initialLength = array1.length();
    assertThat(array1.append(UnsignedByte.of(9))).isEqualTo(array2);
    assertThat(array1.length() - 1).isEqualTo(initialLength);
    assertThat(array2.length()).isEqualTo(initialLength + 1);
  }

  @Test
  void appendByteArray() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {0, 1, 8, 9});
    int initialLength = array1.length();
    assertThat(array1.append(UnsignedByteArray.of(new byte[] {8, 9}))).isEqualTo(array2);
    assertThat(array1.length()).isEqualTo(initialLength + 2);
    assertThat(array2.length()).isEqualTo(initialLength + 2);
  }

  @Test
  void fill() {
    List<UnsignedByte> unsignedBytes1 = new ArrayList<>();
    List<UnsignedByte> unsignedBytes2 = Arrays.asList(UnsignedByte.of(0), UnsignedByte.of(0));
    List<UnsignedByte> filledBytes = UnsignedByteArray.fill(2);
    assertThat(unsignedBytes1).isEqualTo(UnsignedByteArray.fill(0));
    assertThat(unsignedBytes1.size()).isEqualTo(0);
    assertThat(unsignedBytes2).isEqualTo(filledBytes);
    assertThat(unsignedBytes2.equals(filledBytes)).isTrue();
  }

  @Test
  void set() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {0, 9});
    assertThat(array1).isNotEqualTo(array2);
    array1.set(1, UnsignedByte.of(9));
    assertThat(array1).isEqualTo(array2);
  }

  @Test
  void slice() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 8, 9, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {8, 9});
    assertThat(array1).isNotEqualTo(array2);
    assertThat(array1.slice(1, 3)).isEqualTo(array2);
    assertThrows(IndexOutOfBoundsException.class, () -> array1.slice(1, 5));
  }

  @Test
  void hashcode() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    assertThat(array1).isNotEqualTo(UnsignedByteArray.of(new byte[] {8, 9}));
    assertThat(array1.hashCode()).isNotEqualTo(UnsignedByteArray.of(new byte[] {8, 9}).hashCode());
    assertThat(array1.hashCode()).isEqualTo(array1.hashCode());
    assertThat(array1.hashCode()).isEqualTo(UnsignedByteArray.of(new byte[] {0, 1}).hashCode());
  }

  @Test
  void unsignedByteArrayToString() {
    UnsignedByteArray array1 = UnsignedByteArray.of(new byte[] {0, 1});
    UnsignedByteArray array2 = UnsignedByteArray.of(new byte[] {8, 9});
    assertThat(array1.toString()).isEqualTo(array1.toString());
    assertThat(array1).isNotEqualTo(array2);
    assertThat(array1.toString()).isEqualTo(array2.toString());
  }

  @Test
  void unsignedByteArrayEqualsTest() {
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
    assertThat(array1.equals(new Object())).isFalse();
  }

  @Test
  void destroy() {
    UnsignedByteArray uba = UnsignedByteArray.of(new byte[] {0, MAX_BYTE});
    uba.destroy();
    assertThat(uba.isDestroyed()).isTrue();
    assertThat(uba.toByteArray()).isEqualTo(new byte[0]);
    assertThat(uba.hexValue()).isEqualTo("");
    assertThat(uba.hashCode()).isEqualTo(32);
  }
}
