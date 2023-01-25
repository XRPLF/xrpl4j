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

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class ByteUtilsTest {

  @Test
  public void toByteArraySingleByteMinValue() {
    assertThat(ByteUtils.toByteArray(0, 1)).isEqualTo(new byte[] {0});
  }

  @Test
  public void toByteArrayPadded() {
    assertThat(ByteUtils.toByteArray(1, 2)).isEqualTo(new byte[] {0, 1});
  }

  @Test
  public void toByteArraySingleByteMaxValue() {
    assertThat(ByteUtils.toByteArray(15, 1)[0]).isEqualTo((byte) 15);
  }

  @Test
  public void checkSizeMaxValue() {
    ByteUtils.checkSize(4, BigInteger.valueOf(15));
  }

  @Test
  public void checkSizeExceedMaxValue() {
    assertThrows(
      IllegalArgumentException.class,
      () -> ByteUtils.checkSize(4, BigInteger.valueOf(17))
    );
  }

  @Test
  public void checkSizeMinValue() {
    ByteUtils.checkSize(4, BigInteger.valueOf(0));
  }

}
