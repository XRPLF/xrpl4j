package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class VaultFlagsTest extends AbstractFlagsTest {

  @Test
  void testUnsetFlags() {
    VaultFlags flags = VaultFlags.UNSET;

    assertThat(flags.lsfVaultPrivate()).isFalse();
    assertThat(flags.lsfVaultDepositBlocked()).isFalse();
    assertThat(flags.lsfVaultOwnerCanBlockDeposit()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testVaultPrivateFlag() {
    VaultFlags flags = VaultFlags.VAULT_PRIVATE;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfVaultPrivate()).isTrue();
    assertThat(flags.lsfVaultDepositBlocked()).isFalse();
    assertThat(flags.lsfVaultOwnerCanBlockDeposit()).isFalse();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testVaultDepositBlockedFlag() {
    VaultFlags flags = VaultFlags.VAULT_DEPOSIT_BLOCKED;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfVaultPrivate()).isFalse();
    assertThat(flags.lsfVaultDepositBlocked()).isTrue();
    assertThat(flags.lsfVaultOwnerCanBlockDeposit()).isFalse();
    assertThat(flags.getValue()).isEqualTo(131072L);
  }

  @Test
  void testVaultOwnerCanBlockDepositFlag() {
    VaultFlags flags = VaultFlags.VAULT_OWNER_CAN_BLOCK_DEPOSIT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfVaultPrivate()).isFalse();
    assertThat(flags.lsfVaultDepositBlocked()).isFalse();
    assertThat(flags.lsfVaultOwnerCanBlockDeposit()).isTrue();
    assertThat(flags.getValue()).isEqualTo(262144L);
  }

  @Test
  void testOfWithValue() {
    VaultFlags flags = VaultFlags.of(0x00010000);
    assertThat(flags.lsfVaultPrivate()).isTrue();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testOfWithMultipleFlags() {
    VaultFlags flags = VaultFlags.of(0x00010000 | 0x00020000 | 0x00040000);

    assertThat(flags.lsfVaultPrivate()).isTrue();
    assertThat(flags.lsfVaultDepositBlocked()).isTrue();
    assertThat(flags.lsfVaultOwnerCanBlockDeposit()).isTrue();
    assertThat(flags.getValue()).isEqualTo(458752L);
  }

  @Test
  void testOfWithZero() {
    VaultFlags flags = VaultFlags.of(0);
    assertThat(flags.lsfVaultPrivate()).isFalse();
    assertThat(flags.lsfVaultDepositBlocked()).isFalse();
    assertThat(flags.lsfVaultOwnerCanBlockDeposit()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testVaultPrivateJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(VaultFlags.VAULT_PRIVATE);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", VaultFlags.VAULT_PRIVATE.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testVaultDepositBlockedJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(VaultFlags.VAULT_DEPOSIT_BLOCKED);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", VaultFlags.VAULT_DEPOSIT_BLOCKED.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testVaultOwnerCanBlockDepositJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(VaultFlags.VAULT_OWNER_CAN_BLOCK_DEPOSIT);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", VaultFlags.VAULT_OWNER_CAN_BLOCK_DEPOSIT.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUnsetJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(VaultFlags.UNSET);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", VaultFlags.UNSET.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

}
