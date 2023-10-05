package org.xrpl.xrpl4j.crypto.signing.bc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.io.BaseEncoding;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.math.BigInteger;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Unit tests for {@link Secp256k1}.
 */
class Secp256k1Test {

  /////////
  // toUnsignedByteArray(BigInteger)
  /////////

  @Test
  void fromBigIntegerWithInvalidInputs() {
    assertThrows(NullPointerException.class, () -> Secp256k1.toUnsignedByteArray(
      null, // <-- The crux of the test
      0
    ));
    assertThrows(IllegalArgumentException.class, () -> Secp256k1.toUnsignedByteArray(
      BigInteger.valueOf(-1L), // <-- The crux of the test
      0
    ));
    assertThrows(IllegalArgumentException.class, () -> Secp256k1.toUnsignedByteArray(
      BigInteger.valueOf(1L),
      -1 // <-- The crux of the test
    ));
  }

  @Test
  void fromBigIntegerWithZeroLength() {
    Assertions.assertThat(Secp256k1.toUnsignedByteArray(
        BigInteger.valueOf(1L),
        0 // <-- The crux of the test
      )
      .hexValue()).isEqualTo("01");
  }

  @ParameterizedTest
  @ArgumentsSource(BigIntegerByteEncodingsProvider.class)
  void fromBigIntegerWithZeroPaddingBytes(
    final String amountString,
    final String amountToString16,
    final String amountToByteArrayHexUnpadded,
    final String amountToByteArrayHexPrefixPadded
  ) {
    final BigInteger amount = new BigInteger(amountString);
    // NOTE `amount.toString(16)` strips off all leading 0s, even in a nibble (beware of using this in actual impl code)
    Assertions.assertThat(amount.toString(16).toUpperCase(Locale.ENGLISH)).isEqualTo(amountToString16);
    Assertions.assertThat(BaseEncoding.base16().encode(amount.toByteArray())).isEqualTo(amountToByteArrayHexUnpadded);
    Assertions.assertThat(Secp256k1.toUnsignedByteArray(amount, 33).hexValue())
      .isEqualTo(amountToByteArrayHexPrefixPadded);
  }

  @Test
  void fromBigIntegerWithNumberGreaterThan33Bytes() {
    final BigInteger amount = new BigInteger(
      "194815934319126504488398097255143744553440248783815166056530734282223472643" +
        "194815934319126504488398097255143744553440248783815166056530734282223472643");
    // NOTE `amount.toString(16)` strips off all leading 0s, even in a nibble (beware of using this in actual impl code)
    Assertions.assertThat(amount.toString(16).toUpperCase(Locale.ENGLISH)).isEqualTo(
      "F3C607BB6CA7C4C335A24D0302484D16956259AC4510289E3E77A87BD72F36D1EED47F97D33F05F1715F603B45E83748DE37C087" +
        "9DDE6060821AAAAD5003"
    );
    Assertions.assertThat(BaseEncoding.base16().encode(amount.toByteArray())).isEqualTo(
      "00F3C607BB6CA7C4C335A24D0302484D16956259AC4510289E3E77A87BD72F36D1EED47F97D33F05F1715F603B45E83748DE37C087" +
        "9DDE6060821AAAAD5003");
    Assertions.assertThat(Secp256k1.toUnsignedByteArray(amount, 33).hexValue()).isEqualTo(
      "00F3C607BB6CA7C4C335A24D0302484D16956259AC4510289E3E77A87BD72F36D1EED47F97D33F05F1715F603B45E83748DE37C087" +
        "9DDE6060821AAAAD5003");
  }

  /////////////////////////
  // withZeroPrefixPadding(UnsignedByteArray)
  /////////////////////////

  @Test
  void withZeroPrefixPaddingWithUnsignedByteArrayWithInvalidInputs() {
    UnsignedByteArray nullUba = null;
    assertThrows(NullPointerException.class, () -> Secp256k1.withZeroPrefixPadding(
      nullUba, // <-- The crux of the test
      0
    ));

    assertThat(Secp256k1.withZeroPrefixPadding(
      UnsignedByteArray.of(BigInteger.valueOf(-1L).toByteArray()), // <-- The crux of the test
      0
    )).isEqualTo(UnsignedByteArray.of(UnsignedByte.of(255)));

    assertThrows(IllegalArgumentException.class, () -> Secp256k1.withZeroPrefixPadding(
      UnsignedByteArray.of(BigInteger.valueOf(1L).toByteArray()),
      -1 // <-- The crux of the test
    ));
  }

  @Test
  void withZeroPrefixPaddingWithUnsignedByteArrayWithZeroLength() {
    Assertions.assertThat(Secp256k1.withZeroPrefixPadding(
        UnsignedByteArray.of(BigInteger.valueOf(1L).toByteArray()),
        0 // <-- The crux of the test
      )
      .hexValue()).isEqualTo("01");
  }

  @ParameterizedTest
  @ArgumentsSource(BigIntegerByteEncodingsProvider.class)
  void withZeroPrefixPaddingWithUnsignedByteArray(
    final String amountString,
    final String amountToString16,
    final String amountToByteArrayHexUnpadded,
    final String amountToByteArrayHexPrefixPadded
  ) {
    final BigInteger amount = new BigInteger(amountString);
    // NOTE `amount.toString(16)` strips off all leading 0s, even in a nibble (beware of using this in actual impl code)
    Assertions.assertThat(amount.toString(16).toUpperCase(Locale.ENGLISH)).isEqualTo(amountToString16);
    Assertions.assertThat(BaseEncoding.base16().encode(amount.toByteArray())).isEqualTo(amountToByteArrayHexUnpadded);

    UnsignedByteArray uba = UnsignedByteArray.of(amount.toByteArray());
    Assertions.assertThat(Secp256k1.withZeroPrefixPadding(uba, 33).hexValue())
      .isEqualTo(amountToByteArrayHexPrefixPadded);
  }

  @Test
  void withZeroPrefixPaddingWithUnsignedByteArrayExtend32() {
    final byte[] bytes32 = new byte[32];
    final UnsignedByteArray uba32 = UnsignedByteArray.of(bytes32);
    final byte[] bytes33 = new byte[33];
    final UnsignedByteArray uba33 = UnsignedByteArray.of(bytes33);

    assertThrows(IllegalArgumentException.class, () -> Secp256k1.withZeroPrefixPadding(uba32, -1));
    assertThat(Secp256k1.withZeroPrefixPadding(uba32, 0)).isEqualTo(uba32);
    assertThat(Secp256k1.withZeroPrefixPadding(uba32, 1)).isEqualTo(uba32);
    assertThat(Secp256k1.withZeroPrefixPadding(uba32, 32)).isEqualTo(uba32);
    assertThat(Secp256k1.withZeroPrefixPadding(uba32, 33)).isEqualTo(uba33);
  }

  /////////////////////////
  // withZeroPrefixPadding(byte[])
  /////////////////////////

  @Test
  void withZeroPrefixPaddingWithByteArrayWithInvalidInputs() {
    byte[] nullByteArray = null;
    assertThrows(NullPointerException.class, () -> Secp256k1.withZeroPrefixPadding(
      nullByteArray, // <-- The crux of the test
      0
    ));

    assertThat(Secp256k1.withZeroPrefixPadding(
      BigInteger.valueOf(-1L).toByteArray(), // <-- The crux of the test
      0
    )).isEqualTo(UnsignedByteArray.of(UnsignedByte.of(255)));

    assertThrows(IllegalArgumentException.class, () -> Secp256k1.withZeroPrefixPadding(
      BigInteger.valueOf(1L).toByteArray(),
      -1 // <-- The crux of the test
    ));
  }

  @Test
  void withZeroPrefixPaddingWithByteArrayWithZeroLength() {
    Assertions.assertThat(Secp256k1.withZeroPrefixPadding(
        BigInteger.valueOf(1L).toByteArray(),
        0 // <-- The crux of the test
      )
      .hexValue()).isEqualTo("01");
  }

  @ParameterizedTest
  @ArgumentsSource(BigIntegerByteEncodingsProvider.class)
  void withZeroPrefixPaddingWithByteArray(
    final String amountString,
    final String amountToString16,
    final String amountToByteArrayHexUnpadded,
    final String amountToByteArrayHexPrefixPadded
  ) {
    final BigInteger amount = new BigInteger(amountString);
    // NOTE `amount.toString(16)` strips off all leading 0s, even in a nibble (beware of using this in actual impl code)
    Assertions.assertThat(amount.toString(16).toUpperCase(Locale.ENGLISH)).isEqualTo(amountToString16);
    Assertions.assertThat(BaseEncoding.base16().encode(amount.toByteArray())).isEqualTo(amountToByteArrayHexUnpadded);
    Assertions.assertThat(Secp256k1.withZeroPrefixPadding(amount.toByteArray(), 33).hexValue())
      .isEqualTo(amountToByteArrayHexPrefixPadded);
  }

  @Test
  void withZeroPrefixPaddingWithByteArrayExtend32() {
    final byte[] bytes32 = new byte[32];
    final UnsignedByteArray uba32 = UnsignedByteArray.of(bytes32);
    final byte[] bytes33 = new byte[33];
    final UnsignedByteArray uba33 = UnsignedByteArray.of(bytes33);

    assertThrows(IllegalArgumentException.class, () -> Secp256k1.withZeroPrefixPadding(bytes32, -1));
    assertThat(Secp256k1.withZeroPrefixPadding(bytes32, 0)).isEqualTo(uba32);
    assertThat(Secp256k1.withZeroPrefixPadding(bytes32, 1)).isEqualTo(uba32);
    assertThat(Secp256k1.withZeroPrefixPadding(bytes32, 32)).isEqualTo(uba32);
    assertThat(Secp256k1.withZeroPrefixPadding(bytes32, 33)).isEqualTo(uba33);
  }

  /**
   * An {@link ArgumentsProvider} that provides expected binary encodings for a variety of BigInteger representations.
   */
  static class BigIntegerByteEncodingsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
        // A BigInteger comprised of 33 Bytes; toByteArray has 33 bytes; 0 extra padding added
        Arguments.of(
          "107371972967791294617431936514364612285184717182742299921102689634201232605691", // BigInt
          "ED6262116F8D51F1FDD98C184F74CA48DDA7B049CB741F1F7A0564B88FE601FB", // .toString(16)
          "00ED6262116F8D51F1FDD98C184F74CA48DDA7B049CB741F1F7A0564B88FE601FB", // .toByteArray()
          "00ED6262116F8D51F1FDD98C184F74CA48DDA7B049CB741F1F7A0564B88FE601FB" // <-- Padded to 33 bytes
        ),
        // A BigInteger comprised of 33 Bytes; toByteArray has 33 bytes; 0 extra padding added
        Arguments.of(
          "84513109120471239583994879976286018548016554258021069224677925571161262209437", // BigInt
          "BAD8B981A239980B1EC4CB901D698DDE7AA15F264D9537C7D141EE119DD5399D", // .toString(16)
          "00BAD8B981A239980B1EC4CB901D698DDE7AA15F264D9537C7D141EE119DD5399D", // .toByteArray()
          "00BAD8B981A239980B1EC4CB901D698DDE7AA15F264D9537C7D141EE119DD5399D" // <-- BigInteger Hex, Padded to 33 bytes
        ),
        // A BigInteger comprised of 32 Bytes; toByteArray has 32 bytes; 1 extra padding added
        Arguments.of(
          "8427551091932113544047724072139537481003293113704693219824523888925672289487", // BigInt
          "12A1D32B744B18FA0186A44F32D9241869FA0A05B5B831F188831A07163534CF", // .toString(16)
          "12A1D32B744B18FA0186A44F32D9241869FA0A05B5B831F188831A07163534CF", // .toByteArray()
          "0012A1D32B744B18FA0186A44F32D9241869FA0A05B5B831F188831A07163534CF"// <-- BigInteger Hex, Padded to 33 bytes
        ),
        // A BigInteger comprised of 32 Bytes; toByteArray has 32 bytes; 1 extra padding added
        Arguments.of("49026876502144691037964633390198098042987098960613207831256521276903508291997", // BigInt
          "6C643A8EB51D365F3FF5B08C575DEA44B0D3CA5795BDD7B080A7057ABB9A319D", // .toString(16)
          "6C643A8EB51D365F3FF5B08C575DEA44B0D3CA5795BDD7B080A7057ABB9A319D", // .toByteArray()
          "006C643A8EB51D365F3FF5B08C575DEA44B0D3CA5795BDD7B080A7057ABB9A319D"// <-- BigInteger Hex, Padded to 33 bytes
        ),
        // A BigInteger comprised of 31 Bytes; toByteArray has 31 bytes; 2 extra padding added
        Arguments.of("125364023161033659590032058970590371956067907570302268576097734468145372487", // BigInt
          "46F41A0ECE7D0C61B5B36EA377E20621E23C13BD0ABBAEF80754180E9DDD47", // .toString(16)
          "46F41A0ECE7D0C61B5B36EA377E20621E23C13BD0ABBAEF80754180E9DDD47", // .toByteArray()
          "000046F41A0ECE7D0C61B5B36EA377E20621E23C13BD0ABBAEF80754180E9DDD47"// <-- BigInteger Hex, Padded to 33 bytes
        ),
        // A BigInteger comprised of 31 Bytes; toByteArray has 31 bytes; 2 extra padding added
        Arguments.of("194815934319126504488398097255143744553440248783815166056530734282223472643", // BigInt
          "6E430C9E47DFB2194C97385CC85C406DC69773145AE5DE6060821AAAAD5003", // .toString(16)
          "6E430C9E47DFB2194C97385CC85C406DC69773145AE5DE6060821AAAAD5003", // .toByteArray()
          "00006E430C9E47DFB2194C97385CC85C406DC69773145AE5DE6060821AAAAD5003"// <-- BigInteger Hex, Padded to 33 bytes
        ),
        // A BigInteger comprised of 30 Bytes; toByteArray has 30 bytes; 3 extra padding added
        Arguments.of("116983811426126878045574354873599490265363256342001470285420476536129339", // BigInt
          "10F32BB4E0B8B00469196EDACCCAA87A55409FF1C66330D7590449C7073B", // .toString(16)
          "10F32BB4E0B8B00469196EDACCCAA87A55409FF1C66330D7590449C7073B", // .toByteArray()
          "00000010F32BB4E0B8B00469196EDACCCAA87A55409FF1C66330D7590449C7073B" // <-- BigInteger Hex, Padded to 33 bytes
        ),
        // A BigInteger comprised of 30 Bytes; toByteArray has 30 bytes; 3 extra padding added
        Arguments.of("95191719494323154714287471792160232496133380576373117355131561137428728", // BigInt
          "DCADB6BD9E78F0ECE39BB26928F8E4B2A5C7F9CF62C15C1C554B5F458F8", // .toString(16)
          "0DCADB6BD9E78F0ECE39BB26928F8E4B2A5C7F9CF62C15C1C554B5F458F8", // .toByteArray()
          "0000000DCADB6BD9E78F0ECE39BB26928F8E4B2A5C7F9CF62C15C1C554B5F458F8" // <-- BigInteger Hex, Padded to 33 bytes
        )
      );
    }
  }
}