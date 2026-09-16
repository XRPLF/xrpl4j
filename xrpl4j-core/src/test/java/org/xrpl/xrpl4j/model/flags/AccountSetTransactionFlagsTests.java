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
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.stream.Stream;

public class AccountSetTransactionFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(7);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    AccountSetTransactionFlags.Builder builder = AccountSetTransactionFlags.builder()
      .tfInnerBatchTxn(tfInnerBatchTxn);

    if (tfRequireDestTag) {
      builder.tfRequireDestTag();
    }

    if (tfOptionalDestTag) {
      builder.tfOptionalDestTag();
    }

    if (tfRequireAuth) {
      builder.tfRequireAuth();
    }

    if (tfOptionalAuth) {
      builder.tfOptionalAuth();
    }

    if (tfDisallowXrp) {
      builder.tfDisallowXrp();
    }

    if (tfAllowXrp) {
      builder.tfAllowXrp();
    }

    if (tfRequireDestTag && tfOptionalDestTag) {
      assertThatThrownBy(
        builder::build
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tfRequireDestTag and tfOptionalDestTag cannot both be set to true.");
      return;
    }

    if (tfRequireAuth && tfOptionalAuth) {
      assertThatThrownBy(
        builder::build
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tfRequireAuth and tfOptionalAuth cannot both be set to true.");
      return;
    }

    if (tfDisallowXrp && tfAllowXrp) {
      assertThatThrownBy(
        builder::build
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tfDisallowXrp and tfAllowXrp cannot both be set to true.");
      return;
    }

    AccountSetTransactionFlags flags = builder.build();
    long expectedFlags = getExpectedFlags(
      tfRequireDestTag,
      tfOptionalDestTag,
      tfRequireAuth,
      tfOptionalAuth,
      tfDisallowXrp,
      tfAllowXrp,
      tfInnerBatchTxn
    );
    assertThat(flags.getValue()).isEqualTo(expectedFlags);

    FlagsWrapper wrapper = FlagsWrapper.of(flags);

    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .build();

    assertThat(accountSet.flags().isEmpty()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(
      tfRequireDestTag,
      tfOptionalDestTag,
      tfRequireAuth,
      tfOptionalAuth,
      tfDisallowXrp,
      tfAllowXrp,
      tfInnerBatchTxn
    );

    if (tfRequireDestTag && tfOptionalDestTag) {
      assertThatThrownBy(
        () -> AccountSetTransactionFlags.of(expectedFlags)
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tfRequireDestTag and tfOptionalDestTag cannot both be set to true.");
      return;
    }

    if (tfRequireAuth && tfOptionalAuth) {
      assertThatThrownBy(
        () -> AccountSetTransactionFlags.of(expectedFlags)
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tfRequireAuth and tfOptionalAuth cannot both be set to true.");
      return;
    }

    if (tfDisallowXrp && tfAllowXrp) {
      assertThatThrownBy(
        () -> AccountSetTransactionFlags.of(expectedFlags)
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tfDisallowXrp and tfAllowXrp cannot both be set to true.");
      return;
    }

    AccountSetTransactionFlags flags = AccountSetTransactionFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfRequireDestTag()).isEqualTo(tfRequireDestTag);
    assertThat(flags.tfRequireAuth()).isEqualTo(tfRequireAuth);
    assertThat(flags.tfOptionalAuth()).isEqualTo(tfOptionalAuth);
    assertThat(flags.tfDisallowXrp()).isEqualTo(tfDisallowXrp);
    assertThat(flags.tfAllowXrp()).isEqualTo(tfAllowXrp);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlagsWithEmptyFlags() {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .build();

    assertThat(accountSet.flags().isEmpty()).isTrue();
  }

  @Test
  void testEmptyFlags() throws JSONException, JsonProcessingException {
    AccountSetTransactionFlags flags = AccountSetTransactionFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfAllowXrp()).isFalse();
    assertThat(flags.tfDisallowXrp()).isFalse();
    assertThat(flags.tfRequireAuth()).isFalse();
    assertThat(flags.tfOptionalAuth()).isFalse();
    assertThat(flags.tfRequireDestTag()).isFalse();
    assertThat(flags.tfOptionalDestTag()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{\n" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testInnerBatchTxn() {
    AccountSetTransactionFlags flags = AccountSetTransactionFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfRequireDestTag()).isFalse();
    assertThat(flags.tfOptionalDestTag()).isFalse();
    assertThat(flags.tfRequireAuth()).isFalse();
    assertThat(flags.tfOptionalAuth()).isFalse();
    assertThat(flags.tfDisallowXrp()).isFalse();
    assertThat(flags.tfAllowXrp()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  /**
   * Computes the combined expected flag value based on the specified flag inputs.
   *
   * @param tfRequireDestTag  A boolean indicating whether the "Require Destination Tag" flag should be set.
   * @param tfOptionalDestTag A boolean indicating whether the "Optional Destination Tag" flag should be set.
   * @param tfRequireAuth     A boolean indicating whether the "Require Authorization" flag should be set.
   * @param tfOptionalAuth    A boolean indicating whether the "Optional Authorization" flag should be set.
   * @param tfDisallowXrp     A boolean indicating whether the "Disallow XRP" flag should be set.
   * @param tfAllowXrp        A boolean indicating whether the "Allow XRP" flag should be set.
   * @param tfInnerBatchTxn   A boolean indicating whether the "Inner Batch Transaction" flag should be set.
   *
   * @return A long value representing the combined flags calculated from the input parameters.
   */
  private long getExpectedFlags(
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp,
    boolean tfInnerBatchTxn
  ) {
    return (AccountSetTransactionFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfRequireDestTag ? AccountSetTransactionFlags.REQUIRE_DEST_TAG.getValue() : 0L) |
      (tfOptionalDestTag ? AccountSetTransactionFlags.OPTIONAL_DEST_TAG.getValue() : 0L) |
      (tfRequireAuth ? AccountSetTransactionFlags.REQUIRE_AUTH.getValue() : 0L) |
      (tfOptionalAuth ? AccountSetTransactionFlags.OPTIONAL_AUTH.getValue() : 0L) |
      (tfDisallowXrp ? AccountSetTransactionFlags.DISALLOW_XRP.getValue() : 0L) |
      (tfAllowXrp ? AccountSetTransactionFlags.ALLOW_XRP.getValue() : 0L) |
      (tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN.getValue() : 0L);
  }


}
