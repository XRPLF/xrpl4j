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
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;

import java.util.Arrays;

public class Base58Test {

  private static final Logger logger = LoggerFactory.getLogger(Base58Test.class);

  @Test
  public void testEncodeDecodeString() throws Exception {
    byte[] decoded = "Hello World".getBytes();
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
    byte[] input = "123456789".getBytes();
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
      logger.error("Test succeeded.");
    }
  }
}
