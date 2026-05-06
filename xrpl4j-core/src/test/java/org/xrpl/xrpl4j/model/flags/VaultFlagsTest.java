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
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testVaultPrivateFlag() {
    VaultFlags flags = VaultFlags.VAULT_PRIVATE;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfVaultPrivate()).isTrue();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testOfWithValue() {
    VaultFlags flags = VaultFlags.of(0x00010000);
    assertThat(flags.lsfVaultPrivate()).isTrue();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testOfWithZero() {
    VaultFlags flags = VaultFlags.of(0);
    assertThat(flags.lsfVaultPrivate()).isFalse();
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
  void testUnsetJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(VaultFlags.UNSET);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", VaultFlags.UNSET.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

}
