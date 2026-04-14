package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SponsorshipSetFlags}.
 */
public class SponsorshipSetFlagsTest {

  @Test
  void testEmptyFlags() {
    SponsorshipSetFlags flags = SponsorshipSetFlags.empty();

    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfRequireSignForFee()).isFalse();
    assertThat(flags.tfRequireSignForReserve()).isFalse();
    assertThat(flags.tfDeleteObject()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.isEmpty()).isTrue();
  }

  @Test
  void testBuilderWithRequireSignForFee() {
    SponsorshipSetFlags flags = SponsorshipSetFlags.builder()
      .tfRequireSignForFee()
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfRequireSignForFee()).isTrue();
    assertThat(flags.tfRequireSignForReserve()).isFalse();
    assertThat(flags.tfDeleteObject()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
  }

  @Test
  void testBuilderWithRequireSignForReserve() {
    SponsorshipSetFlags flags = SponsorshipSetFlags.builder()
      .tfRequireSignForReserve()
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfRequireSignForFee()).isFalse();
    assertThat(flags.tfRequireSignForReserve()).isTrue();
    assertThat(flags.tfDeleteObject()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
  }

  @Test
  void testBuilderWithDeleteObject() {
    SponsorshipSetFlags flags = SponsorshipSetFlags.builder()
      .tfDeleteObject()
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfRequireSignForFee()).isFalse();
    assertThat(flags.tfRequireSignForReserve()).isFalse();
    assertThat(flags.tfDeleteObject()).isTrue();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
  }

  @Test
  void testBuilderWithAllFlags() {
    SponsorshipSetFlags flags = SponsorshipSetFlags.builder()
      .tfRequireSignForFee()
      .tfRequireSignForReserve()
      .tfDeleteObject()
      .tfInnerBatchTxn(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfRequireSignForFee()).isTrue();
    assertThat(flags.tfRequireSignForReserve()).isTrue();
    assertThat(flags.tfDeleteObject()).isTrue();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
  }

  @Test
  void testOfWithLongValue() {
    // Test with tfSponsorshipSetRequireSignForFee = 0x00010000
    SponsorshipSetFlags flags = SponsorshipSetFlags.of(0x00010000L);
    assertThat(flags.tfRequireSignForFee()).isTrue();
    assertThat(flags.tfRequireSignForReserve()).isFalse();
    assertThat(flags.tfDeleteObject()).isFalse();
  }

  @Test
  void testOfWithCombinedValues() {
    // Test with tfSponsorshipSetRequireSignForFee | tfSponsorshipSetRequireSignForReserve | tfDeleteObject
    // 0x00010000 | 0x00040000 | 0x00100000 = 0x00150000
    SponsorshipSetFlags flags = SponsorshipSetFlags.of(0x00150000L);
    assertThat(flags.tfRequireSignForFee()).isTrue();
    assertThat(flags.tfRequireSignForReserve()).isTrue();
    assertThat(flags.tfDeleteObject()).isTrue();
  }

  @Test
  void testFlagValues() {
    // Verify the flag values match the spec
    assertThat(SponsorshipSetFlags.of(0x00010000L).tfRequireSignForFee()).isTrue();
    assertThat(SponsorshipSetFlags.of(0x00040000L).tfRequireSignForReserve()).isTrue();
    assertThat(SponsorshipSetFlags.of(0x00100000L).tfDeleteObject()).isTrue();
  }

}
