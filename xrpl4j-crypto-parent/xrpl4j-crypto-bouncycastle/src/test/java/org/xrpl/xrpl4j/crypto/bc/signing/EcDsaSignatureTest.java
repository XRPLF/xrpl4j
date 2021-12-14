package org.xrpl.xrpl4j.crypto.bc.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
  @Disabled
  void isStrictlyCanonical() {
    // TODO: Add coverage.
  }
}