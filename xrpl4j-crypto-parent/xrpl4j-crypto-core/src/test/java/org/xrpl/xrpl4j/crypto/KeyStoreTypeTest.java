package org.xrpl.xrpl4j.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KeyStoreType}.
 */
public class KeyStoreTypeTest {

  @Test
  public void testKeystoreId() {
    assertThat(KeyStoreType.DERIVED_SERVER_SECRET.keystoreId()).isEqualTo("derived_server_secret");
    assertThat(KeyStoreType.GCP_KMS.keystoreId()).isEqualTo("gcp_kms");
  }

  @Test
  public void testFromKeystoreTypeIdWithNullId() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> KeyStoreType.fromKeystoreTypeId(null),
      "KeyStoreType must be non-null and have at least 1 character");
  }

  @Test
  public void testFromKeystoreTypeId() {
    assertThat(KeyStoreType.fromKeystoreTypeId("derived_server_secret")).isEqualTo(KeyStoreType.DERIVED_SERVER_SECRET);
    assertThat(KeyStoreType.fromKeystoreTypeId("DERIVED_SERVER_SECRET")).isEqualTo(KeyStoreType.DERIVED_SERVER_SECRET);
    assertThat(KeyStoreType.fromKeystoreTypeId("gcp_kms")).isEqualTo(KeyStoreType.GCP_KMS);
    assertThat(KeyStoreType.fromKeystoreTypeId("foo").keystoreId()).isEqualTo("foo");
  }

  @Test
  public void testFromKeystoreTypeEmpty() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> KeyStoreType.fromKeystoreTypeId(""),
      "KeyStoreType must be non-null and have at least 1 character");
  }

}