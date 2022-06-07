package org.xrpl.xrpl4j.crypto.bc.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

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