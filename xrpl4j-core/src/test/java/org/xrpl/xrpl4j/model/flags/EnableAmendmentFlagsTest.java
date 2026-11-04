package org.xrpl.xrpl4j.model.flags;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class EnableAmendmentFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @Test
  public void testConstantsMatchRippled() {
    assertThat(EnableAmendmentFlags.GOT_MAJORITY.getValue()).isEqualTo(0x00010000L);
    assertThat(EnableAmendmentFlags.LOST_MAJORITY.getValue()).isEqualTo(0x00020000L);
  }

  @Test
  public void testEmpty() {
    EnableAmendmentFlags flags = EnableAmendmentFlags.empty();

    assertThat(flags.isEmpty()).isTrue();
    assertThat(flags.tfGotMajority()).isFalse();
    assertThat(flags.tfLostMajority()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.isEnabled()).isTrue();
    assertThat(flags).isEqualTo(TransactionFlags.EMPTY);
  }

  @Test
  public void testIsEnabledIgnoresUnrelatedBits() {
    // Only the two majority bits determine "enabled"; generic bits like tfFullyCanonicalSig must not affect it.
    assertThat(EnableAmendmentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()).isEnabled()).isTrue();
    assertThat(EnableAmendmentFlags.of(0L).isEnabled()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfGotMajority,
    boolean tfLostMajority
  ) {
    long expectedFlags = (tfGotMajority ? EnableAmendmentFlags.GOT_MAJORITY.getValue() : 0L) |
      (tfLostMajority ? EnableAmendmentFlags.LOST_MAJORITY.getValue() : 0L);

    EnableAmendmentFlags flags = EnableAmendmentFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfGotMajority()).isEqualTo(tfGotMajority);
    assertThat(flags.tfLostMajority()).isEqualTo(tfLostMajority);
    assertThat(flags.isEnabled()).isEqualTo(!tfGotMajority && !tfLostMajority);
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfGotMajority,
    boolean tfLostMajority
  ) throws JSONException, JsonProcessingException {
    long expectedFlags = (tfGotMajority ? EnableAmendmentFlags.GOT_MAJORITY.getValue() : 0L) |
      (tfLostMajority ? EnableAmendmentFlags.LOST_MAJORITY.getValue() : 0L);

    EnableAmendmentFlags flags = EnableAmendmentFlags.of(expectedFlags);

    TransactionFlagsWrapper flagsWrapper = TransactionFlagsWrapper.of(flags);

    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(flagsWrapper, json);
  }
}
