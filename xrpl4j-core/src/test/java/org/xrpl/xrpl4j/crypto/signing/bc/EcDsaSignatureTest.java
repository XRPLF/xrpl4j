package org.xrpl.xrpl4j.crypto.signing.bc;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import static org.mockito.Mockito.mock;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.signing.bc.EcDsaSignature;

import java.math.BigInteger;

/**
 * Unit tests for {@link EcDsaSignature}.
 */
class EcDsaSignatureTest {

  EcDsaSignature signature;

  @BeforeEach
  void setUp() {
    signature = EcDsaSignature.builder()
      .r(BigInteger.ONE)
      .s(BigInteger.ONE)
      .build();
  }

  @Test
  void fromDer() {
    byte[] derBytes = BaseEncoding.base16().decode("3006020101020101");
    EcDsaSignature signature = EcDsaSignature.fromDer(derBytes);

    assertThat(signature.r()).isEqualTo(BigInteger.ONE);
    assertThat(signature.s()).isEqualTo(BigInteger.ONE);
  }

  @Test
  void der() {
    assertThat(BaseEncoding.base16().encode(signature.der().toByteArray())).isEqualTo("3006020101020101");
  }

  @Test
  void badDerThrowsWhenFormingSignature() {
    assertThrows(
      RuntimeException.class,
      () -> EcDsaSignature.fromDer(new byte[] {1, 2, 3, 4})
    );
  }

  @Test
  void ecDsaSignatureWithR0() {
    IllegalArgumentException illegalArgumentException = assertThrows(
      IllegalArgumentException.class,
      () -> EcDsaSignature.builder()
        .r(BigInteger.ZERO)
        .s(BigInteger.ONE)
        .build()
    );
    assertThat(illegalArgumentException.getMessage()).isEqualTo("r cannot be 0.");
  }

  @Test
  void ecDsaSignatureWithS0() {
    IllegalArgumentException illegalArgumentException = assertThrows(
      IllegalArgumentException.class,
      () -> EcDsaSignature.builder()
        .r(BigInteger.ONE)
        .s(BigInteger.ZERO)
        .build()
    );
    assertThat(illegalArgumentException.getMessage()).isEqualTo("s cannot be 0.");
  }

  @Test
  void ecDsaSignatureWithRNegativeThrows() {
    IllegalArgumentException illegalArgumentException = assertThrows(
      IllegalArgumentException.class,
      () -> EcDsaSignature.builder()
        .r(BigInteger.ONE.negate())
        .s(BigInteger.valueOf(2))
        .build()
    );
    assertThat(illegalArgumentException.getMessage()).isEqualTo("r cannot be negative.");
  }

  @Test
  void ecDsaSignatureWithSNegativeThrows() {
    IllegalArgumentException illegalArgumentException = assertThrows(
      IllegalArgumentException.class,
      () -> EcDsaSignature.builder()
        .r(BigInteger.valueOf(2))
        .s(BigInteger.ONE.negate())
        .build()
    );
    assertThat(illegalArgumentException.getMessage()).isEqualTo("s cannot be negative.");
  }
}
