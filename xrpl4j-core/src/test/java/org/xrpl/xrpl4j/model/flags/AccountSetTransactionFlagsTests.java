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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AccountSetTransactionFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(6);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp
  ) {
    AccountSetTransactionFlags.Builder builder = AccountSetTransactionFlags.builder();

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
      tfAllowXrp
    );
    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp
  ) {
    long expectedFlags = getExpectedFlags(
      tfRequireDestTag,
      tfOptionalDestTag,
      tfRequireAuth,
      tfOptionalAuth,
      tfDisallowXrp,
      tfAllowXrp
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
  }

  private long getExpectedFlags(
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp
  ) {
    return (AccountSetTransactionFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfRequireDestTag ? AccountSetTransactionFlags.REQUIRE_DEST_TAG.getValue() : 0L) |
      (tfOptionalDestTag ? AccountSetTransactionFlags.OPTIONAL_DEST_TAG.getValue() : 0L) |
      (tfRequireAuth ? AccountSetTransactionFlags.REQUIRE_AUTH.getValue() : 0L) |
      (tfOptionalAuth ? AccountSetTransactionFlags.OPTIONAL_AUTH.getValue() : 0L) |
      (tfDisallowXrp ? AccountSetTransactionFlags.DISALLOW_XRP.getValue() : 0L) |
      (tfAllowXrp ? AccountSetTransactionFlags.ALLOW_XRP.getValue() : 0L);
  }
}
