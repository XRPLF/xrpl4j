package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.VaultSetFlags;

/**
 * Unit tests for {@link VaultSet} validation logic.
 */
class VaultSetTest {

  @Test
  void defaultFlagsAreEmptyVaultSetFlags() {
    VaultSet vaultSet = baseBuilder().build();

    assertThat(vaultSet.flags()).isInstanceOf(VaultSetFlags.class);
    assertThat(vaultSet.flags().isEmpty()).isTrue();
    assertThat(vaultSet.flags().tfVaultDepositBlock()).isFalse();
    assertThat(vaultSet.flags().tfVaultDepositUnblock()).isFalse();
  }

  @Test
  void depositBlockFlagIsAllowed() {
    VaultSet vaultSet = assertDoesNotThrow(() -> baseBuilder()
      .flags(VaultSetFlags.builder().tfVaultDepositBlock(true).build())
      .build()
    );

    assertThat(vaultSet.flags().tfVaultDepositBlock()).isTrue();
    assertThat(vaultSet.flags().tfVaultDepositUnblock()).isFalse();
  }

  @Test
  void depositUnblockFlagIsAllowed() {
    VaultSet vaultSet = assertDoesNotThrow(() -> baseBuilder()
      .flags(VaultSetFlags.builder().tfVaultDepositUnblock(true).build())
      .build()
    );

    assertThat(vaultSet.flags().tfVaultDepositBlock()).isFalse();
    assertThat(vaultSet.flags().tfVaultDepositUnblock()).isTrue();
  }

  /**
   * {@link VaultSetFlags.Builder} rejects this combination outright, so this asserts the transaction-level guard that
   * still applies when flags are constructed from a raw value via {@link VaultSetFlags#of(long)}.
   */
  @Test
  void depositBlockAndUnblockAreMutuallyExclusive() {
    VaultSetFlags bothSet = VaultSetFlags.of(
      VaultSetFlags.VAULT_DEPOSIT_BLOCK.getValue() | VaultSetFlags.VAULT_DEPOSIT_UNBLOCK.getValue()
    );

    assertThatThrownBy(() -> baseBuilder()
      .flags(bothSet)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("tfVaultDepositBlock and tfVaultDepositUnblock cannot both be set.");
  }

  /**
   * Returns a builder pre-populated with required fields for VaultSet.
   */
  private ImmutableVaultSet.Builder baseBuilder() {
    return VaultSet.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .vaultId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      );
  }
}
