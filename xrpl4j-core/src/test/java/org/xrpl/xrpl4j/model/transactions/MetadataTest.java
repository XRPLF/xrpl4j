package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.immutables.Wrapped;

/**
 * Unit tests for the {@link Wrappers.Metadata} base class, using a test-only Immutables-generated wrapper.
 */
class MetadataTest {

  @Value.Immutable
  @Wrapped
  abstract static class _TestMetadata extends Wrappers.Metadata {

    @Override
    protected UnsignedInteger maxBytes() {
      return UnsignedInteger.valueOf(4);
    }
  }

  // //////////////////////
  // validateLength
  // //////////////////////

  @Test
  void emptyValueIsRejected() {
    assertThatThrownBy(() -> TestMetadata.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must not be empty");
  }

  @Test
  void exactlyMaxBytesIsAccepted() {
    // 4 bytes = 8 hex chars
    assertThatNoException().isThrownBy(() -> TestMetadata.of("AABBCCDD"));
  }

  @Test
  void belowMaxBytesIsAccepted() {
    assertThatNoException().isThrownBy(() -> TestMetadata.of("AABB"));
  }

  @Test
  void exceedingMaxBytesIsRejected() {
    // 5 bytes = 10 hex chars, max is 4
    assertThatThrownBy(() -> TestMetadata.of("AABBCCDDEE"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be <= 4 bytes or <= 8 hex characters");
  }

  @Test
  void maxBytesUsesSubclassValue() {
    // Verify the error message includes the subclass-specific limit
    assertThatThrownBy(() -> TestMetadata.of("AABBCCDDEE"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be <= 4 bytes");
  }

  // //////////////////////
  // validateHexEncoding
  // //////////////////////

  @Test
  void validHexIsAccepted() {
    assertThatNoException().isThrownBy(() -> TestMetadata.of("AABB"));
    assertThatNoException().isThrownBy(() -> TestMetadata.of("aabb"));
    assertThatNoException().isThrownBy(() -> TestMetadata.of("0189AbCd"));
  }

  @Test
  void oddLengthHexIsRejected() {
    assertThatThrownBy(() -> TestMetadata.of("A"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  @Test
  void nonHexCharactersAreRejected() {
    assertThatThrownBy(() -> TestMetadata.of("ZZZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  // //////////////////////
  // equals / hashCode
  // //////////////////////

  @Test
  void equalsCaseInsensitive() {
    TestMetadata lower = TestMetadata.of("aabb");
    TestMetadata upper = TestMetadata.of("AABB");
    assertThat(lower).isEqualTo(upper);
    assertThat(upper).isEqualTo(lower);
  }

  @Test
  void hashCodeCaseInsensitive() {
    assertThat(TestMetadata.of("aabb").hashCode())
      .isEqualTo(TestMetadata.of("AABB").hashCode());
  }

  @Test
  void differentValuesAreNotEqual() {
    assertThat(TestMetadata.of("AABB")).isNotEqualTo(TestMetadata.of("CCDD"));
  }

  @Test
  void notEqualToNull() {
    assertThat(TestMetadata.of("AABB")).isNotEqualTo(null);
  }

  @Test
  void notEqualToNonMetadataObject() {
    assertThat(TestMetadata.of("AABB")).isNotEqualTo("AABB");
  }

  @Test
  void differentMetadataSubclassesAreNotEqual() {
    // Same value, different Metadata subclass — equals uses getClass() check
    assertThat(TestMetadata.of("AABB")).isNotEqualTo(VaultData.of("AABB"));
  }

  // //////////////////////
  // toString
  // //////////////////////

  @Test
  void toStringReturnsRawValue() {
    assertThat(TestMetadata.of("aaBB").toString()).isEqualTo("aaBB");
  }
}
