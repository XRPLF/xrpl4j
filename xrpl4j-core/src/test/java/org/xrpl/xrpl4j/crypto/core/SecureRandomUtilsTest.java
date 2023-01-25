package org.xrpl.xrpl4j.crypto.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.core.SecureRandomUtils.isAndroidRuntime;
import static org.xrpl.xrpl4j.crypto.core.SecureRandomUtils.secureRandom;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SecureRandomUtils}.
 */
public class SecureRandomUtilsTest {

  @Test
  public void testSecureRandom() {
    secureRandom().nextInt();
  }

  @Test
  public void testIsNotAndroidRuntime() {
    assertThat(isAndroidRuntime()).isFalse();
  }
}