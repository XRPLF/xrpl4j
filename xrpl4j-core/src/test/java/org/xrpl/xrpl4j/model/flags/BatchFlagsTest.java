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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Unit tests for {@link BatchFlags}.
 */
class BatchFlagsTest extends AbstractFlagsTest {

  @Test
  void testAllOrNothingFlag() {
    BatchFlags flags = BatchFlags.ALL_OR_NOTHING;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfAllOrNothing()).isTrue();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x00010000L);
  }

  @Test
  void testOnlyOneFlag() {
    BatchFlags flags = BatchFlags.ONLY_ONE;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isTrue();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x00020000L);
  }

  @Test
  void testUntilFailureFlag() {
    BatchFlags flags = BatchFlags.UNTIL_FAILURE;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isTrue();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x00040000L);
  }

  @Test
  void testIndependentFlag() {
    BatchFlags flags = BatchFlags.INDEPENDENT;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x00080000L);
  }

  @Test
  void testOfAllOrNothing() {
    assertThat(BatchFlags.ofAllOrNothing()).isEqualTo(BatchFlags.ALL_OR_NOTHING);
  }

  @Test
  void testOfOnlyOne() {
    assertThat(BatchFlags.ofOnlyOne()).isEqualTo(BatchFlags.ONLY_ONE);
  }

  @Test
  void testOfUntilFailure() {
    assertThat(BatchFlags.ofUntilFailure()).isEqualTo(BatchFlags.UNTIL_FAILURE);
  }

  @Test
  void testOfIndependent() {
    assertThat(BatchFlags.ofIndependent()).isEqualTo(BatchFlags.INDEPENDENT);
  }

  @Test
  void testOfWithValue() {
    BatchFlags flags = BatchFlags.of(0x00010000L);
    assertThat(flags.tfAllOrNothing()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x00010000L);
  }

  @Test
  void testOfWithOnlyOneValue() {
    BatchFlags flags = BatchFlags.of(0x00020000L);
    assertThat(flags.tfOnlyOne()).isTrue();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x00020000L);
  }

  @Test
  void testOfWithUntilFailureValue() {
    BatchFlags flags = BatchFlags.of(0x00040000L);
    assertThat(flags.tfUntilFailure()).isTrue();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x00040000L);
  }

  @Test
  void testOfWithIndependentValue() {
    BatchFlags flags = BatchFlags.of(0x00080000L);
    assertThat(flags.tfIndependent()).isTrue();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0x00080000L);
  }

  @Test
  void testOfWithZeroValue() {
    BatchFlags flags = BatchFlags.of(0L);
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  // ////////////////
  // Test with() method
  // ////////////////

  @Test
  void testWithCombinesFlags() {
    BatchFlags combined = BatchFlags.ALL_OR_NOTHING.with(BatchFlags.ONLY_ONE);
    assertThat(combined.tfAllOrNothing()).isTrue();
    assertThat(combined.tfOnlyOne()).isTrue();
    assertThat(combined.tfUntilFailure()).isFalse();
    assertThat(combined.tfIndependent()).isFalse();
    assertThat(combined.getValue()).isEqualTo(0x00010000L | 0x00020000L);
  }

  @Test
  void testWithMultipleFlags() {
    BatchFlags combined = BatchFlags.UNTIL_FAILURE.with(BatchFlags.INDEPENDENT);
    assertThat(combined.tfUntilFailure()).isTrue();
    assertThat(combined.tfIndependent()).isTrue();
    assertThat(combined.tfAllOrNothing()).isFalse();
    assertThat(combined.tfOnlyOne()).isFalse();
    assertThat(combined.getValue()).isEqualTo(0x00040000L | 0x00080000L);
  }

  @Test
  void testWithUnsetFlag() {
    BatchFlags combined = BatchFlags.ALL_OR_NOTHING.with(BatchFlags.UNSET);
    assertThat(combined.tfAllOrNothing()).isTrue();
    assertThat(combined.getValue()).isEqualTo(0x00010000L);
  }

  @Test
  void testWithChaining() {
    BatchFlags combined = BatchFlags.ALL_OR_NOTHING
      .with(BatchFlags.ONLY_ONE)
      .with(BatchFlags.UNTIL_FAILURE);
    assertThat(combined.tfAllOrNothing()).isTrue();
    assertThat(combined.tfOnlyOne()).isTrue();
    assertThat(combined.tfUntilFailure()).isTrue();
    assertThat(combined.tfIndependent()).isFalse();
    assertThat(combined.getValue()).isEqualTo(0x00010000L | 0x00020000L | 0x00040000L);
  }

  // ////////////////
  // Test Builder
  // ////////////////

  public static Stream<Arguments> builderData() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("builderData")
  void testBuilderWithAllCombinations(
    boolean tfAllOrNothing,
    boolean tfOnlyOne,
    boolean tfUntilFailure,
    boolean tfIndependent
  ) {
    BatchFlags flags = BatchFlags.builder()
      .tfAllOrNothing(tfAllOrNothing)
      .tfOnlyOne(tfOnlyOne)
      .tfUntilFailure(tfUntilFailure)
      .tfIndependent(tfIndependent)
      .build();

    assertThat(flags.tfOnlyOne()).isEqualTo(tfOnlyOne);
    assertThat(flags.tfUntilFailure()).isEqualTo(tfUntilFailure);
    assertThat(flags.tfIndependent()).isEqualTo(tfIndependent);
  }

  @Test
  void testBuilderDefaultValues() {
    BatchFlags flags = BatchFlags.builder().build();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
  }

  @Test
  void testBuilderWithAllOrNothingOnly() {
    BatchFlags flags = BatchFlags.builder()
      .tfAllOrNothing(true)
      .build();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
  }

  @Test
  void testBuilderWithOnlyOneOnly() {
    BatchFlags flags = BatchFlags.builder()
      .tfOnlyOne(true)
      .build();
    assertThat(flags.tfOnlyOne()).isTrue();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
  }

  @Test
  void testBuilderWithUntilFailureOnly() {
    BatchFlags flags = BatchFlags.builder()
      .tfUntilFailure(true)
      .build();
    assertThat(flags.tfUntilFailure()).isTrue();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfIndependent()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
  }

  @Test
  void testBuilderWithIndependentOnly() {
    BatchFlags flags = BatchFlags.builder()
      .tfIndependent(true)
      .build();
    assertThat(flags.tfIndependent()).isTrue();
    assertThat(flags.tfAllOrNothing()).isFalse();
    assertThat(flags.tfOnlyOne()).isFalse();
    assertThat(flags.tfUntilFailure()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
  }

  // ////////////////
  // Test TransactionFlags as Batch Flags
  // ////////////////

  @Test
  void testAllOrNothingJson() throws JSONException, JsonProcessingException {
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(BatchFlags.ALL_OR_NOTHING);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.ALL_OR_NOTHING.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testOnlyOneJson() throws JSONException, JsonProcessingException {
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(BatchFlags.ONLY_ONE);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.ONLY_ONE.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyFlags() throws JSONException, JsonProcessingException {
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(BatchFlags.EMPTY);
    String json = "{}";
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUnsetFlags() throws JSONException, JsonProcessingException {
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(BatchFlags.UNSET);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.UNSET.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUntilFailureJson() throws JSONException, JsonProcessingException {
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(BatchFlags.UNTIL_FAILURE);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.UNTIL_FAILURE.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testIndependentJson() throws JSONException, JsonProcessingException {
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(BatchFlags.INDEPENDENT);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.INDEPENDENT.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  // ////////////////
  // Test Batch Flags as Transaction Flags
  // ////////////////

  @Test
  void testAllOrNothingBatchJson() throws JSONException, JsonProcessingException {
    BatchFlagsWrapper wrapper = BatchFlagsWrapper.of(BatchFlags.ALL_OR_NOTHING);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.ALL_OR_NOTHING.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testOnlyOneBatchJson() throws JSONException, JsonProcessingException {
    BatchFlagsWrapper wrapper = BatchFlagsWrapper.of(BatchFlags.ONLY_ONE);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.ONLY_ONE.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyBatchFlags() throws JSONException, JsonProcessingException {
    BatchFlagsWrapper wrapper = BatchFlagsWrapper.of(BatchFlags.EMPTY);
    String json = "{}";
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUnsetBatchFlags() throws JSONException, JsonProcessingException {
    BatchFlagsWrapper wrapper = BatchFlagsWrapper.of(BatchFlags.UNSET);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.UNSET.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUntilFailureBatchJson() throws JSONException, JsonProcessingException {
    BatchFlagsWrapper wrapper = BatchFlagsWrapper.of(BatchFlags.UNTIL_FAILURE);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.UNTIL_FAILURE.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testIndependentBatchJson() throws JSONException, JsonProcessingException {
    BatchFlagsWrapper wrapper = BatchFlagsWrapper.of(BatchFlags.INDEPENDENT);
    String json = String.format("{\n" +
      "\"flags\": %s\n" +
      "}", BatchFlags.INDEPENDENT.getValue());
    assertCanSerializeAndDeserialize(wrapper, json);
  }
}
