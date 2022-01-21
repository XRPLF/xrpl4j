package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NfTokenBurnTest {

  @Test
  public void buildTx() {

    String id = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65";
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenId(id)
      .build();
    assertThat(nfTokenBurn.tokenId()).isEqualTo(id);
  }

  @Test
  public void buildTxWithMissingParam() {

    assertThrows(
      IllegalStateException.class,
      () -> NfTokenMint.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .build()
    );
  }

  @Test
  public void tokenIdTooShort() {
    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenBurn.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .tokenId("random")
        .build(),
      "tokenId must be 64 characters (256 bits), but was 6 characters long."
    );

  }
}

