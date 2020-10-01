package com.ripple.xrpl4j.codec.binary.addresses;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class AddressCodecTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  AddressCodec addressCodec;

  @Before
  public void setUp() throws Exception {
    addressCodec = new AddressCodec();
  }

  @Test
  public void decodeMultipleVersionsWithNoExpectedLength() {
    expectedException.expect(DecodeException.class);
    expectedException.expectMessage("expectedLength is required because there are >= 2 possible versions");
    addressCodec.decode(
      "rnaC7gW34M77Kneb78s",
      Lists.newArrayList(Version.ED25519_SEED, Version.FAMILY_SEED)
    );
  }

  @Test
  public void decodeDataWithLengthLessThanFour() {
    expectedException.expect(EncodingFormatException.class);
    expectedException.expectMessage("Input must be longer than 3 characters.");
    addressCodec.decode("1234", Lists.newArrayList(Version.ACCOUNT_ID));
  }

  @Test
  public void decodeDataWithIncorrectVersion() {
    expectedException.expect(DecodeException.class);
    expectedException.expectMessage("Version is invalid. Version bytes do not match any of the provided versions.");
    addressCodec.decode("rnaC7gW34M77Kneb78s", Lists.newArrayList(Version.ED25519_SEED));
  }


  @Test
  public void decodeDataWithInvalidChecksum() {
    expectedException.expect(EncodingFormatException.class);
    expectedException.expectMessage("Checksum does not validate");
    addressCodec.decode("123456789", Lists.newArrayList(Version.ACCOUNT_ID));
  }

  @Test
  public void decodeDataWithoutExpectedLength() {
    Decoded expected = Decoded.builder()
      .version(Version.ACCOUNT_ID)
      .bytes(UnsignedByteArray.of("123456789".getBytes()))
      .build();

    Decoded decoded = addressCodec.decode("rnaC7gW34M77Kneb78s", Lists.newArrayList(Version.ACCOUNT_ID));
    assertThat(decoded).isEqualTo(expected);
  }

  @Test
  public void decodeDataWithExpectedLength() {
    Decoded expected = Decoded.builder()
      .version(Version.ACCOUNT_ID)
      .bytes(UnsignedByteArray.of("123456789".getBytes()))
      .build();

    Decoded decoded = addressCodec.decode(
      "rnaC7gW34M77Kneb78s",
      Lists.newArrayList(Version.ACCOUNT_ID),
      UnsignedInteger.valueOf(9)
    );
    assertThat(decoded).isEqualTo(expected);
  }

  @Test
  public void decodedDatatWithWrongExpectedLength() {
    Decoded expected = Decoded.builder()
      .version(Version.ACCOUNT_ID)
      .bytes(UnsignedByteArray.of("123456789".getBytes()))
      .build();

    expectedException.expect(DecodeException.class);
    expectedException.expectMessage("Version is invalid. Version bytes do not match any of the provided versions.");

    Decoded decoded = addressCodec.decode(
      "rnaC7gW34M77Kneb78s",
      Lists.newArrayList(Version.ACCOUNT_ID),
      UnsignedInteger.valueOf(8)
    );
  }
}
