package org.xrpl.xrpl4j.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.SecureRandomUtils.isAndroidRuntime;
import static org.xrpl.xrpl4j.crypto.SecureRandomUtils.secureRandom;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.SecureRandomUtils;

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