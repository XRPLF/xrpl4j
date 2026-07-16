package org.xrpl.xrpl4j.crypto.confidential.model.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link ConfidentialMptConvertBackContext}, which wraps the 32-byte SHA512Half context hash that binds
 * a ConfidentialMPTConvertBack proof to a specific transaction.
 */
class ConfidentialMptConvertBackContextTest {

  private static final String HASH_HEX = Strings.repeat("AB", 32); // 32 bytes.

  @Test
  void ofExposesValueAndHex() {
    ConfidentialMptConvertBackContext context =
      ConfidentialMptConvertBackContext.of(UnsignedByteArray.fromHex(HASH_HEX));
    assertThat(context.value().length()).isEqualTo(32);
    assertThat(context.hexValue()).isEqualTo(HASH_HEX);
  }

  @Test
  void fromHexRoundTrips() {
    assertThat(ConfidentialMptConvertBackContext.fromHex(HASH_HEX).hexValue()).isEqualTo(HASH_HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> ConfidentialMptConvertBackContext.fromHex(Strings.repeat("AB", 31)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Context hash must be");
  }
}
