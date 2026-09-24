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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class VaultSetFlagsTests extends AbstractFlagsTest {

  /**
   * Every combination of the three flags, except the mutually exclusive {@code tfVaultDepositBlock} +
   * {@code tfVaultDepositUnblock} pair, which is asserted separately in {@link #blockAndUnblockCannotBothBeSet()}.
   *
   * @return A {@link Stream} of {@link Arguments}.
   */
  public static Stream<Arguments> data() {
    return getBooleanCombinations(3)
      .filter(args -> !((boolean) args.get()[0] && (boolean) args.get()[1]));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfVaultDepositBlock,
    boolean tfVaultDepositUnblock,
    boolean tfInnerBatchTxn
  ) {
    VaultSetFlags flags = VaultSetFlags.builder()
      .tfVaultDepositBlock(tfVaultDepositBlock)
      .tfVaultDepositUnblock(tfVaultDepositUnblock)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfVaultDepositBlock, tfVaultDepositUnblock, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfVaultDepositBlock,
    boolean tfVaultDepositUnblock,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(tfVaultDepositBlock, tfVaultDepositUnblock, tfInnerBatchTxn);
    VaultSetFlags flags = VaultSetFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfVaultDepositBlock()).isEqualTo(tfVaultDepositBlock);
    assertThat(flags.tfVaultDepositUnblock()).isEqualTo(tfVaultDepositUnblock);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void blockAndUnblockCannotBothBeSet() {
    assertThatThrownBy(() -> VaultSetFlags.builder()
      .tfVaultDepositBlock(true)
      .tfVaultDepositUnblock(true)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("tfVaultDepositBlock and tfVaultDepositUnblock must not both be set");
  }

  @Test
  void testEmptyFlags() {
    VaultSetFlags flags = VaultSetFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfVaultDepositBlock()).isFalse();
    assertThat(flags.tfVaultDepositUnblock()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfVaultDepositBlock,
    boolean tfVaultDepositUnblock,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    VaultSetFlags flags = VaultSetFlags.builder()
      .tfVaultDepositBlock(tfVaultDepositBlock)
      .tfVaultDepositUnblock(tfVaultDepositUnblock)
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
    VaultSetFlags flags = VaultSetFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  private long getExpectedFlags(
    boolean tfVaultDepositBlock,
    boolean tfVaultDepositUnblock,
    boolean tfInnerBatchTxn
  ) {
    return (VaultSetFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfVaultDepositBlock ? VaultSetFlags.VAULT_DEPOSIT_BLOCK.getValue() : 0L) |
      (tfVaultDepositUnblock ? VaultSetFlags.VAULT_DEPOSIT_UNBLOCK.getValue() : 0L) |
      (tfInnerBatchTxn ? VaultSetFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}
