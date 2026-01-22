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
}

