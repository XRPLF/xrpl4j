package org.xrpl.xrpl4j.model.client.vault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * Unit tests for {@link VaultInfoRequestParams}.
 */
public class VaultInfoRequestParamsTest {

  private static final Hash256 VAULT_ID = Hash256.of(
    "1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF"
  );
  private static final Address OWNER = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j");
  private static final UnsignedInteger SEQ = UnsignedInteger.valueOf(123);

  @Test
  public void testWithVaultId() {
    VaultInfoRequestParams params = VaultInfoRequestParams.of(VAULT_ID);

    assertThat(params.vaultId()).isNotEmpty().get().isEqualTo(VAULT_ID);
    assertThat(params.owner()).isEmpty();
    assertThat(params.seq()).isEmpty();
  }

  @Test
  public void testWithOwnerAndSeq() {
    VaultInfoRequestParams params = VaultInfoRequestParams.of(OWNER, SEQ);

    assertThat(params.vaultId()).isEmpty();
    assertThat(params.owner()).isNotEmpty().get().isEqualTo(OWNER);
    assertThat(params.seq()).isNotEmpty().get().isEqualTo(SEQ);
  }

  @Test
  public void testBuilderWithVaultId() {
    VaultInfoRequestParams params = VaultInfoRequestParams.builder()
      .vaultId(VAULT_ID)
      .build();

    assertThat(params.vaultId()).isNotEmpty().get().isEqualTo(VAULT_ID);
    assertThat(params.owner()).isEmpty();
    assertThat(params.seq()).isEmpty();
  }

  @Test
  public void testBuilderWithOwnerAndSeq() {
    VaultInfoRequestParams params = VaultInfoRequestParams.builder()
      .owner(OWNER)
      .seq(SEQ)
      .build();

    assertThat(params.vaultId()).isEmpty();
    assertThat(params.owner()).isNotEmpty().get().isEqualTo(OWNER);
    assertThat(params.seq()).isNotEmpty().get().isEqualTo(SEQ);
  }

  @Test
  public void testValidationFailsWithNoFields() {
    assertThrows(IllegalArgumentException.class, () ->
      VaultInfoRequestParams.builder().build()
    );
  }

  @Test
  public void testValidationFailsWithBothIdentificationMethods() {
    assertThrows(IllegalArgumentException.class, () ->
      VaultInfoRequestParams.builder()
        .vaultId(VAULT_ID)
        .owner(OWNER)
        .seq(SEQ)
        .build()
    );
  }

  @Test
  public void testValidationFailsWithOnlyOwner() {
    assertThrows(IllegalArgumentException.class, () ->
      VaultInfoRequestParams.builder()
        .owner(OWNER)
        .build()
    );
  }

  @Test
  public void testValidationFailsWithOnlySeq() {
    assertThrows(IllegalArgumentException.class, () ->
      VaultInfoRequestParams.builder()
        .seq(SEQ)
        .build()
    );
  }

  @Test
  public void testValidationFailsWithVaultIdAndOwnerOnly() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
      VaultInfoRequestParams.builder()
        .vaultId(VAULT_ID)
        .owner(OWNER)
        .build()
    );
    assertThat(exception.getMessage()).isEqualTo("VaultInfoRequestParams cannot specify both vaultId and owner/seq");
  }

  @Test
  public void testValidationFailsWithVaultIdAndSeqOnly() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
      VaultInfoRequestParams.builder()
        .vaultId(VAULT_ID)
        .seq(SEQ)
        .build()
    );
    assertThat(exception.getMessage()).isEqualTo("VaultInfoRequestParams cannot specify both vaultId and owner/seq");
  }
}

