package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class NfTokenIdTest {

  @Test
  public void nfTokenEquality() {
    assertThat(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65")
      .equals(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65")));

    assertThat(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65")
      .equals(NfTokenId.of("000b013a95f14b0044f78a264e41713c64b5f89242540ee208c3098e00000d65")));

    assertThat(NfTokenId.of("0000000000000000000000000000000000000000000000000000000000000000")).isNotEqualTo(null);
    assertThat(NfTokenId.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(new Object());
  }

  @Test
  public void nfTokenHashcode() {
    assertThat(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65").hashCode())
      .isEqualTo(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65").hashCode());
  }
}
