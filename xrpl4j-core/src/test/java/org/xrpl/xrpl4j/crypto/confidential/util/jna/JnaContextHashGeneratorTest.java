package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaContextHashGenerator#generateConvertContext} using a mocked {@link MptCryptoLibrary}, so the
 * marshalling and error handling are exercised without loading the native mpt-crypto library.
 */
class JnaContextHashGeneratorTest {

  private static final Address ACCOUNT = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  private static final MpTokenIssuanceId ISSUANCE_ID = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
  private static final UnsignedInteger SEQUENCE = UnsignedInteger.valueOf(42);

  private MptCryptoLibrary lib;
  private JnaContextHashGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaContextHashGenerator(lib);
  }

  @Test
  void generateConvertContextReturnsNativeHash() {
    byte[] expected = new byte[32];
    Arrays.fill(expected, (byte) 0x07);
    when(lib.mpt_get_convert_context_hash(any(), any(), anyInt(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(3);
      System.arraycopy(expected, 0, out, 0, 32);
      return 0;
    });

    ConfidentialMptConvertContext context = generator.generateConvertContext(ACCOUNT, SEQUENCE, ISSUANCE_ID);

    assertThat(context.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateConvertContextThrowsOnNativeError() {
    when(lib.mpt_get_convert_context_hash(any(), any(), anyInt(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateConvertContext(ACCOUNT, SEQUENCE, ISSUANCE_ID))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_convert_context_hash failed");
  }

  @Test
  void generateConvertContextRejectsNullArguments() {
    assertThatThrownBy(() -> generator.generateConvertContext(null, SEQUENCE, ISSUANCE_ID))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("account");
    assertThatThrownBy(() -> generator.generateConvertContext(ACCOUNT, null, ISSUANCE_ID))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("sequence");
    assertThatThrownBy(() -> generator.generateConvertContext(ACCOUNT, SEQUENCE, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("issuanceId");
  }
}
