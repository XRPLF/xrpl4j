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

public class VaultDepositFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(boolean tfVaultDonate, boolean tfInnerBatchTxn) {
    VaultDepositFlags flags = VaultDepositFlags.builder()
      .tfVaultDonate(tfVaultDonate)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue()).isEqualTo(getExpectedFlags(tfVaultDonate, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(boolean tfVaultDonate, boolean tfInnerBatchTxn) {
    long expectedFlags = getExpectedFlags(tfVaultDonate, tfInnerBatchTxn);
    VaultDepositFlags flags = VaultDepositFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfVaultDonate()).isEqualTo(tfVaultDonate);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testEmptyFlags() {
    VaultDepositFlags flags = VaultDepositFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfVaultDonate()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(boolean tfVaultDonate, boolean tfInnerBatchTxn) throws JSONException, JsonProcessingException {
    VaultDepositFlags flags = VaultDepositFlags.builder()
      .tfVaultDonate(tfVaultDonate)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson() throws JSONException, JsonProcessingException {
    VaultDepositFlags flags = VaultDepositFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  private long getExpectedFlags(boolean tfVaultDonate, boolean tfInnerBatchTxn) {
    return (VaultDepositFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfVaultDonate ? VaultDepositFlags.VAULT_DONATE.getValue() : 0L) |
      (tfInnerBatchTxn ? VaultDepositFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}
