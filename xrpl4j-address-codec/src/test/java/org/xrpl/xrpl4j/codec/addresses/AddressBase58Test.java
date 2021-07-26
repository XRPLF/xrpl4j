package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;

public class AddressBase58Test {

  @Test
  public void decodeDataWithLengthLessThanFour() {
    assertThrows(
      EncodingFormatException.class,
      () -> AddressBase58.decode("1234", Version.ACCOUNT_ID),
      "Input must be longer than 3 characters."
    );
  }

  @Test
  public void decodeDataWithIncorrectVersion() {
    assertThrows(
      DecodeException.class,
      () -> AddressBase58.decode("rnaC7gW34M77Kneb78s", Version.ED25519_SEED),
      "Version is invalid. Version bytes do not match any of the provided versions."
    );
  }

  @Test
  public void decodeDataWithInvalidChecksum() {
    assertThrows(
      EncodingFormatException.class,
      () -> AddressBase58.decode("123456789", Version.ACCOUNT_ID),
      "Checksum does not validate"
    );
  }

  @Test
  public void decodeDataWithoutExpectedLength() {
    Decoded expected = Decoded.builder()
        .version(Version.ACCOUNT_ID)
        .bytes(UnsignedByteArray.of("123456789" .getBytes()))
        .build();

    Decoded decoded = AddressBase58.decode("rnaC7gW34M77Kneb78s", Version.ACCOUNT_ID);
    assertThat(decoded).isEqualTo(expected);
  }

  @Test
  public void decodeDataWithExpectedLength() {
    Decoded expected = Decoded.builder()
        .version(Version.ACCOUNT_ID)
        .bytes(UnsignedByteArray.of("123456789" .getBytes()))
        .build();

    Decoded decoded = AddressBase58.decode(
        "rnaC7gW34M77Kneb78s",
        Lists.newArrayList(Version.ACCOUNT_ID),
        UnsignedInteger.valueOf(9)
    );
    assertThat(decoded).isEqualTo(expected);
  }

  @Test
  public void decodedDatatWithWrongExpectedLength() {
    assertThrows(
      DecodeException.class,
      () ->  AddressBase58.decode(
        "rnaC7gW34M77Kneb78s",
        Lists.newArrayList(Version.ACCOUNT_ID),
        UnsignedInteger.valueOf(8)
      ),
      "Version is invalid. Version bytes do not match any of the provided versions."
    );

    assertThrows(
      DecodeException.class,
      () -> AddressBase58.decode(
        "rnaC7gW34M77Kneb78s",
        Lists.newArrayList(Version.ACCOUNT_ID),
        UnsignedInteger.valueOf(10)
      ),
      "Version is invalid. Version bytes do not match any of the provided versions."
    );
  }

}
