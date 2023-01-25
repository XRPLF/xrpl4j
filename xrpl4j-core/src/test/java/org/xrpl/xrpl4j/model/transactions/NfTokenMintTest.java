package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

  @Test
  public void buildTxWithTransferFee() {
    TransferFee transferFee = TransferFee.of(UnsignedInteger.valueOf(10));
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .transferFee(transferFee)
      .build();

    assertThat(nfTokenMint.transferFee()).isEqualTo(Optional.of(transferFee));
  }

  @Test
  public void transferFeeWithoutFlagSet() {
    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenMint.builder()
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .tokenTaxon(UnsignedLong.valueOf(146999694L))
        .transferFee(TransferFee.of(UnsignedInteger.valueOf(1000)))
        .build(),
      "tfTransferable flag must be set for secondary sale."
    );
  }

  @Test
  public void transferFeeOutOfBounds() {
    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenMint.builder()
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .tokenTaxon(UnsignedLong.valueOf(146999694L))
        .flags(Flags.NfTokenMintFlags.builder()
          .tfTransferable(true)
          .build())
        .transferFee(TransferFee.of(UnsignedInteger.valueOf(50001)))
        .build(),
      "TransferFee should be in the range 0 to 50000."
    );
  }

  @Test
  public void transferFeeUsingPercent() {
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .transferFee(TransferFee.ofPercent(BigDecimal.valueOf(99.99)))
      .build();

    assertThat(nfTokenMint.transferFee().equals(Optional.of(9999)));
  }

  @Test
  public void txWithUri() {


    UnsignedLong taxon = UnsignedLong.valueOf(146999694L);
    String uri = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(taxon)
      .uri(NfTokenUri.ofPlainText(uri))
      .build();

    String expected = BaseEncoding.base16().encode(uri.getBytes(StandardCharsets.UTF_8));
    assertThat(nfTokenMint.tokenTaxon()).isEqualTo(taxon);
    assertThat(nfTokenMint.uri()).isEqualTo(Optional.of(NfTokenUri.of(expected)));
  }

}
