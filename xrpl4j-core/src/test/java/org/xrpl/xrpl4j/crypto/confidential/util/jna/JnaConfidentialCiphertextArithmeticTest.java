package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.sun.jna.Memory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;

/**
 * Unit tests for {@link JnaConfidentialCiphertextArithmetic} using a mocked {@link MptCryptoLibrary}, verifying the
 * marshalling to the native homomorphic routines and error handling without loading the native mpt-crypto library.
 */
class JnaConfidentialCiphertextArithmeticTest {

  private static final EncryptedAmount A = EncryptedAmount.of(Strings.repeat("02", 66));
  private static final EncryptedAmount B = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final byte[] COMBINED = new byte[66];

  static {
    java.util.Arrays.fill(COMBINED, (byte) 0x07);
  }

  private MptCryptoLibrary lib;
  private JnaConfidentialCiphertextArithmetic arithmetic;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    arithmetic = new JnaConfidentialCiphertextArithmetic(lib);
    // A non-null native context, valid point parsing, and a serializer that emits COMBINED.
    when(lib.mpt_secp256k1_context()).thenReturn(new Memory(1));
    when(lib.mpt_make_ec_pair(any(), any(), any())).thenReturn(true);
    when(lib.mpt_serialize_ec_pair(any(), any(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(2);
      System.arraycopy(COMBINED, 0, out, 0, out.length);
      return true;
    });
  }

  @Test
  void addReturnsCombinedCiphertext() {
    when(lib.secp256k1_elgamal_add(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

    EncryptedAmount result = arithmetic.add(A, B);

    assertThat(result.value().toByteArray()).isEqualTo(COMBINED);
    verify(lib).secp256k1_elgamal_add(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void subtractReturnsCombinedCiphertext() {
    when(lib.secp256k1_elgamal_subtract(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

    EncryptedAmount result = arithmetic.subtract(A, B);

    assertThat(result.value().toByteArray()).isEqualTo(COMBINED);
    verify(lib).secp256k1_elgamal_subtract(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void rejectsNullArguments() {
    assertThatThrownBy(() -> arithmetic.add(null, B))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("left");
    assertThatThrownBy(() -> arithmetic.add(A, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("right");
    assertThatThrownBy(() -> arithmetic.subtract(null, B))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("left");
    assertThatThrownBy(() -> arithmetic.subtract(A, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("right");
  }

  @Test
  void throwsWhenContextIsNull() {
    when(lib.mpt_secp256k1_context()).thenReturn(null);
    assertThatThrownBy(() -> arithmetic.add(A, B))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("null context");
  }

  @Test
  void throwsWhenMakeEcPairFails() {
    when(lib.mpt_make_ec_pair(any(), any(), any())).thenReturn(false);
    assertThatThrownBy(() -> arithmetic.add(A, B))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("left ciphertext");
  }

  @Test
  void throwsWhenSecondMakeEcPairFails() {
    // The first (left) parse succeeds, the second (right) fails.
    when(lib.mpt_make_ec_pair(any(), any(), any())).thenReturn(true, false);
    assertThatThrownBy(() -> arithmetic.add(A, B))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("right ciphertext");
  }

  @Test
  void throwsWhenElgamalReturnsNonSuccess() {
    when(lib.secp256k1_elgamal_add(any(), any(), any(), any(), any(), any(), any())).thenReturn(0);
    assertThatThrownBy(() -> arithmetic.add(A, B))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("secp256k1_elgamal_add failed");
  }

  @Test
  void throwsWhenSerializeFails() {
    when(lib.secp256k1_elgamal_add(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
    // doReturn avoids re-invoking the thenAnswer stub from setUp during re-stubbing.
    doReturn(false).when(lib).mpt_serialize_ec_pair(any(), any(), any());
    assertThatThrownBy(() -> arithmetic.add(A, B))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("mpt_serialize_ec_pair");
  }
}
