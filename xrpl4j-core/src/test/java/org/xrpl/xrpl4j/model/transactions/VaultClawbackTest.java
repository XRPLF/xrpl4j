package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for {@link VaultClawback} validation logic.
 */
class VaultClawbackTest {

  @Test
  void validVaultClawbackWithMptAmount() {
    VaultClawback clawback = VaultClawback.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(MpTokenIssuanceId.of("00000001B8A2D9D2F4F8F8F8F8F8F8F8F8F8F8F8F8F8F8F8F8F8F8F8"))
          .value("1000")
          .build()
      )
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    assertThat(clawback.vaultId()).isEqualTo(
      Hash256.of("0000000000000000000000000000000000000000000000000000000000000001")
    );
    assertThat(clawback.holder()).isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(clawback.amount()).isPresent();
  }

  @Test
  void validVaultClawbackWithIssuedCurrencyAmount() {
    VaultClawback clawback = VaultClawback.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
          .value("100")
          .build()
      )
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    assertThat(clawback.vaultId()).isEqualTo(
      Hash256.of("0000000000000000000000000000000000000000000000000000000000000001")
    );
    assertThat(clawback.holder()).isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(clawback.amount()).isPresent();
  }

  @Test
  void validVaultClawbackWithoutAmount() {
    VaultClawback clawback = VaultClawback.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    assertThat(clawback.amount()).isEmpty();
  }

  @Test
  void vaultClawbackCannotHaveXrpAmount() {
    assertThatThrownBy(() -> VaultClawback.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .holder(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultClawback amount cannot be XRP.");
  }

}
