package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.VaultDepositFlags;

/**
 * Unit tests for {@link VaultDeposit}.
 */
class VaultDepositTest {

  @Test
  void defaultFlagsAreEmptyVaultDepositFlags() {
    VaultDeposit vaultDeposit = baseBuilder().build();

    assertThat(vaultDeposit.flags()).isInstanceOf(VaultDepositFlags.class);
    assertThat(vaultDeposit.flags().isEmpty()).isTrue();
    assertThat(vaultDeposit.flags().tfVaultDonate()).isFalse();
  }

  @Test
  void donateFlagAccessor() {
    VaultDeposit vaultDeposit = baseBuilder()
      .flags(VaultDepositFlags.builder().tfVaultDonate(true).build())
      .build();

    assertThat(vaultDeposit.flags().tfVaultDonate()).isTrue();
    assertThat(vaultDeposit.flags().tfInnerBatchTxn()).isFalse();
  }

  /**
   * Returns a builder pre-populated with required fields for VaultDeposit.
   */
  private ImmutableVaultDeposit.Builder baseBuilder() {
    return VaultDeposit.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      );
  }
}
