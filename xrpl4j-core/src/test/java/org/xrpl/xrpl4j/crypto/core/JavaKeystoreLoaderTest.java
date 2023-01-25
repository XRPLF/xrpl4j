package org.xrpl.xrpl4j.crypto.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    assertThrows(NullPointerException.class,
      () -> JavaKeystoreLoader.loadFromClasspath(null, "password".toCharArray()));
  }

  @Test
  public void testLoadFromClasspathWithNullPassword() {
    assertThrows(NullPointerException.class,
      () -> JavaKeystoreLoader.loadFromClasspath("crypto/crypto.p12", null));
  }

  @Test
  public void testLoadFromClasspathWithInvalidPassword() {
    assertThrows(RuntimeException.class,
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