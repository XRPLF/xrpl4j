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
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaContextHashGenerator} using a mocked {@link MptCryptoLibrary}, so the marshalling and error
 * handling of each context hash generation method are exercised without loading the native mpt-crypto library.
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

  @Test
  void generateConvertBackContextReturnsNativeHash() {
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    byte[] expected = new byte[32];
    Arrays.fill(expected, (byte) 0x08);
    when(lib.mpt_get_convert_back_context_hash(any(), any(), anyInt(), anyInt(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(4);
      System.arraycopy(expected, 0, out, 0, 32);
      return 0;
    });

    ConfidentialMptConvertBackContext context =
      generator.generateConvertBackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, version);

    assertThat(context.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateConvertBackContextThrowsOnNativeError() {
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    when(lib.mpt_get_convert_back_context_hash(any(), any(), anyInt(), anyInt(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateConvertBackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, version))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_convert_back_context_hash failed");
  }

  @Test
  void generateConvertBackContextRejectsNullArguments() {
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    assertThatThrownBy(() -> generator.generateConvertBackContext(null, SEQUENCE, ISSUANCE_ID, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("account");
    assertThatThrownBy(() -> generator.generateConvertBackContext(ACCOUNT, null, ISSUANCE_ID, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("sequence");
    assertThatThrownBy(() -> generator.generateConvertBackContext(ACCOUNT, SEQUENCE, null, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("issuanceId");
    assertThatThrownBy(() -> generator.generateConvertBackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("version");
  }

  @Test
  void generateSendContextReturnsNativeHash() {
    Address destination = Address.of("rMTi57fNy2UkUb4RcdoUeJm7gjxVQvxzUo");
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    byte[] expected = new byte[32];
    Arrays.fill(expected, (byte) 0x09);
    when(lib.mpt_get_send_context_hash(any(), any(), anyInt(), any(), anyInt(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(5);
      System.arraycopy(expected, 0, out, 0, 32);
      return 0;
    });

    ConfidentialMptSendContext context =
      generator.generateSendContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, destination, version);

    assertThat(context.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateSendContextThrowsOnNativeError() {
    Address destination = Address.of("rMTi57fNy2UkUb4RcdoUeJm7gjxVQvxzUo");
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    when(lib.mpt_get_send_context_hash(any(), any(), anyInt(), any(), anyInt(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateSendContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, destination, version))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_send_context_hash failed");
  }

  @Test
  void generateSendContextRejectsNullArguments() {
    Address destination = Address.of("rMTi57fNy2UkUb4RcdoUeJm7gjxVQvxzUo");
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    assertThatThrownBy(() -> generator.generateSendContext(null, SEQUENCE, ISSUANCE_ID, destination, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("account");
    assertThatThrownBy(() -> generator.generateSendContext(ACCOUNT, null, ISSUANCE_ID, destination, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("sequence");
    assertThatThrownBy(() -> generator.generateSendContext(ACCOUNT, SEQUENCE, null, destination, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("issuanceId");
    assertThatThrownBy(() -> generator.generateSendContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, null, version))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("destination");
    assertThatThrownBy(() -> generator.generateSendContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, destination, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("version");
  }

  @Test
  void generateClawbackContextReturnsNativeHash() {
    Address holder = Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w");
    byte[] expected = new byte[32];
    Arrays.fill(expected, (byte) 0x0A);
    when(lib.mpt_get_clawback_context_hash(any(), any(), anyInt(), any(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(4);
      System.arraycopy(expected, 0, out, 0, 32);
      return 0;
    });

    ConfidentialMptClawbackContext context =
      generator.generateClawbackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, holder);

    assertThat(context.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateClawbackContextThrowsOnNativeError() {
    Address holder = Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w");
    when(lib.mpt_get_clawback_context_hash(any(), any(), anyInt(), any(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateClawbackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, holder))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_clawback_context_hash failed");
  }

  @Test
  void generateClawbackContextRejectsNullArguments() {
    Address holder = Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w");
    assertThatThrownBy(() -> generator.generateClawbackContext(null, SEQUENCE, ISSUANCE_ID, holder))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("account");
    assertThatThrownBy(() -> generator.generateClawbackContext(ACCOUNT, null, ISSUANCE_ID, holder))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("sequence");
    assertThatThrownBy(() -> generator.generateClawbackContext(ACCOUNT, SEQUENCE, null, holder))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("issuanceId");
    assertThatThrownBy(() -> generator.generateClawbackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("holder");
  }
}
