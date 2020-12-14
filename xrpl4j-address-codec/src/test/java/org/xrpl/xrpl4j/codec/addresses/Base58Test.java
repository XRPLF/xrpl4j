package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.junit.Test;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;

import java.util.Arrays;

public class Base58Test {

  @Test
  public void testEncodeDecodeString() throws Exception {
    byte[] decoded = "Hello World" .getBytes();
    String encoded = "JxErpTiA7PhnBMd";
    assertThat(Base58.encode(decoded)).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded);
  }

  @Test
  public void testEncodeUnsignedInteger() throws EncodingFormatException {
    UnsignedLong decoded = UnsignedLong.valueOf(3471844090L);
    String encoded = "raHofH1";
    assertThat(Base58.encode(decoded.bigIntegerValue().toByteArray())).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded.bigIntegerValue().toByteArray());
  }

  @Test
  public void testEncodeDecodeZeroByte() throws EncodingFormatException {
    byte[] decoded = new byte[1];
    String encoded = "r";
    assertThat(Base58.encode(decoded)).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded);
  }

  @Test
  public void testEncodeDecodeZeroBytes() throws EncodingFormatException {
    byte[] decoded = new byte[7];
    String encoded = "rrrrrrr";
    assertThat(Base58.encode(decoded)).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded);
  }

  @Test
  public void testEncodeDecodeChecked() {
    byte[] input = "123456789" .getBytes();
    String encoded = AddressBase58.encodeChecked(input, Lists.newArrayList(Version.ACCOUNT_ID));
    assertThat(encoded).isEqualTo("rnaC7gW34M77Kneb78s");

    byte[] decoded = Base58.decodeChecked(encoded);
    // Base58Check decode adds a leading 0.
    assertThat(decoded[0]).isZero();
    assertThat(Arrays.copyOfRange(decoded, 1, decoded.length)).isEqualTo(input);
  }

  @Test
  public void testDecodeInvalidBase58() {
    try {
      Base58.decode("This isn't valid base58");
      fail();
    } catch (EncodingFormatException e) {

    }
  }
}
