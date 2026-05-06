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

public class VaultCreateFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfVaultPrivate,
    boolean tfVaultShareNonTransferable
  ) {
    VaultCreateFlags flags = VaultCreateFlags.builder()
      .tfVaultPrivate(tfVaultPrivate)
      .tfVaultShareNonTransferable(tfVaultShareNonTransferable)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfVaultPrivate, tfVaultShareNonTransferable));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfVaultPrivate,
    boolean tfVaultShareNonTransferable
  ) {
    long expectedFlags = getExpectedFlags(tfVaultPrivate, tfVaultShareNonTransferable);
    VaultCreateFlags flags = VaultCreateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfVaultPrivate()).isEqualTo(tfVaultPrivate);
    assertThat(flags.tfVaultShareNonTransferable()).isEqualTo(tfVaultShareNonTransferable);
  }

  @Test
  void testEmptyFlags() {
    VaultCreateFlags flags = VaultCreateFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfVaultPrivate()).isFalse();
    assertThat(flags.tfVaultShareNonTransferable()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfVaultPrivate,
    boolean tfVaultShareNonTransferable
  ) throws JSONException, JsonProcessingException {
    VaultCreateFlags flags = VaultCreateFlags.builder()
      .tfVaultPrivate(tfVaultPrivate)
      .tfVaultShareNonTransferable(tfVaultShareNonTransferable)
      .build();

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson() throws JSONException, JsonProcessingException {
    VaultCreateFlags flags = VaultCreateFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  private long getExpectedFlags(
    boolean tfVaultPrivate,
    boolean tfVaultShareNonTransferable
  ) {
    return (VaultCreateFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfVaultPrivate ? VaultCreateFlags.VAULT_PRIVATE.getValue() : 0L) |
      (tfVaultShareNonTransferable ? VaultCreateFlags.VAULT_SHARE_NON_TRANSFERABLE.getValue() : 0L);
  }
}

