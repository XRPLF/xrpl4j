package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

public class NfTokenMintTest {

  @Test
  public void buildTx() {

    UnsignedLong taxon = UnsignedLong.valueOf(146999694L);
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(taxon)
      .build();

    assertThat(nfTokenMint.tokenTaxon()).isEqualTo(taxon);
  }

  @Test
  public void buildTxWithMissingParam() {

    UnsignedLong taxon = UnsignedLong.valueOf(146999694L);
    assertThrows(
      IllegalStateException.class,
      () -> NfTokenMint.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .tokenTaxon(taxon)
        .build()
    );
  }
}
