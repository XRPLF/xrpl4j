package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NfTokenBurnTest {

  @Test
  public void buildTx() {

    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .nfTokenId(id)
      .build();

    Assertions.assertThat(id.equals(nfTokenBurn.nfTokenId()));
    assertThat(nfTokenBurn.nfTokenId()).isEqualTo(id);
  }

  @Test
  public void buildTxWithOwner() {

    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    Address ownerAddress = Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .owner(ownerAddress)
      .nfTokenId(id)
      .build();

    Assertions.assertThat(id.equals(nfTokenBurn.nfTokenId()));
    assertThat(nfTokenBurn.nfTokenId()).isEqualTo(id);
    assertThat(nfTokenBurn.owner().get()).isEqualTo(ownerAddress);
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
        .nfTokenId(NfTokenId.of("random"))
        .build(),
      "tokenId must be 64 characters (256 bits), but was 6 characters long."
    );
  }
}
