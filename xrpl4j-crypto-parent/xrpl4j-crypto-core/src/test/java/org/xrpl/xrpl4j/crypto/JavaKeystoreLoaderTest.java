package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
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

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyStore;

/**
 * Unit tests for {@link JavaKeystoreLoader}.
 */
public class JavaKeystoreLoaderTest {

  @Test
  public void testLoadFromClasspathWithNullName() {
    Assertions.assertThrows(NullPointerException.class,
      () -> JavaKeystoreLoader.loadFromClasspath(null, "password".toCharArray()));
  }

  @Test
  public void testLoadFromClasspathWithNullPassword() {
    Assertions.assertThrows(NullPointerException.class,
      () -> JavaKeystoreLoader.loadFromClasspath("crypto/crypto.p12", null));
  }

  @Test
  public void testLoadFromClasspathWithInvalidPassword() {
    Assertions.assertThrows(RuntimeException.class,
      () -> JavaKeystoreLoader.loadFromClasspath("crypto/crypto.p12", "foo".toCharArray()));
  }

  @Test
  public void testLoadFromClasspath() throws Exception {
    KeyStore actual = JavaKeystoreLoader.loadFromClasspath("crypto/crypto.p12", "password".toCharArray());
    assertThat(actual.getKey("secret0", "password".toCharArray()).getAlgorithm()).isEqualTo("AES");
    byte[] secretKey0Bytes = actual.getKey("secret0", "password".toCharArray()).getEncoded();
    assertThat(BaseEncoding.base64().encode(secretKey0Bytes)).isEqualTo("MKzVrebbY4Cw58xuyJf64ynZ4SY3whBG3A4c72eBdYc=");
  }
}
