package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class AccountChannelsResultTest {

  @Test
  void testWithLedgerIndex() {
    AccountChannelsResult result = AccountChannelsResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addChannels(
        PaymentChannelResultObject.builder()
          .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
          .amount(XrpCurrencyAmount.ofDrops(100000000))
          .balance(XrpCurrencyAmount.ofDrops(0))
          .channelId(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
          .destinationAccount(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .destinationTag(UnsignedInteger.valueOf(20170428))
          .publicKey("aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3")
          .publicKeyHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6")
          .expiration(UnsignedLong.valueOf(10000))
          .cancelAfter(UnsignedLong.valueOf(10000))
          .sourceTag(UnsignedInteger.valueOf(10000))
          .settleDelay(UnsignedInteger.valueOf(86400))
          .build()
      )
      .build();

    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThat(result.ledgerIndexSafe()).isEqualTo(result.ledgerIndex());
  }

  @Test
  void testWithLedgerCurrentIndex() {
    AccountChannelsResult result = AccountChannelsResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(false)
      .addChannels(
        PaymentChannelResultObject.builder()
          .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
          .amount(XrpCurrencyAmount.ofDrops(100000000))
          .balance(XrpCurrencyAmount.ofDrops(0))
          .channelId(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
          .destinationAccount(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .destinationTag(UnsignedInteger.valueOf(20170428))
          .publicKey("aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3")
          .publicKeyHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6")
          .expiration(UnsignedLong.valueOf(10000))
          .cancelAfter(UnsignedLong.valueOf(10000))
          .sourceTag(UnsignedInteger.valueOf(10000))
          .settleDelay(UnsignedInteger.valueOf(86400))
          .build()
      )
      .build();

    assertThat(result.ledgerIndex()).isNull();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
  }
}
