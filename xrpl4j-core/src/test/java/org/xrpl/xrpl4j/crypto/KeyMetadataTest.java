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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KeyMetadata}.
 */
public class KeyMetadataTest {

  @Test
  public void testPlatformIdentifierWithNullPlatformIdentifier() {
    assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      // .platformIdentifier("foo")
      .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyPassword("foo")
      .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierWithNullKeyringIdentifier() {
    assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .platformIdentifier("foo")
      // .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyPassword("foo")
      .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierWithNullKeyIdentifier() {
    assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .platformIdentifier("foo")
      .keyringIdentifier("foo")
      // .keyIdentifier("foo")
      .keyPassword("foo")
      .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierWithNullKeyVersion() {
    assertThrows(IllegalStateException.class, () -> KeyMetadata.builder()
      .platformIdentifier("foo")
      .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyPassword("foo")
      // .keyVersion("foo")
      .build());
  }

  @Test
  public void testPlatformIdentifierBuilder() {
    assertThat(KeyMetadata.builder()
      .platformIdentifier("foo")
      .keyringIdentifier("foo")
      .keyIdentifier("foo")
      .keyVersion("foo")
      .build()).isNotNull();
  }

  @Test
  public void testEmptyKeyMetadata() {
    assertThat(KeyMetadata.EMPTY)
      .isEqualTo(KeyMetadata.builder()
        .platformIdentifier("n/a")
        .keyringIdentifier("n/a")
        .keyIdentifier("n/a")
        .keyVersion("n/a")
        .build());
  }
}
