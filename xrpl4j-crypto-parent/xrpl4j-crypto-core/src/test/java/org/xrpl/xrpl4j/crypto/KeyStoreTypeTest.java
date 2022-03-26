package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

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
