package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AccountRootFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(9);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfDefaultRipple,
    boolean lsfDepositAuth,
    boolean lsfDisableMaster,
    boolean lsfDisallowXrp,
    boolean lsfGlobalFreeze,
    boolean lsfNoFreeze,
    boolean lsfPasswordSpent,
    boolean lsfRequireAuth,
    boolean lsfRequireDestTag
  ) {
    long expectedFlags = (lsfDefaultRipple ? AccountRootFlags.DEFAULT_RIPPLE.getValue() : 0L) |
      (lsfDepositAuth ? AccountRootFlags.DEPOSIT_AUTH.getValue() : 0L) |
      (lsfDisableMaster ? AccountRootFlags.DISABLE_MASTER.getValue() : 0L) |
      (lsfDisallowXrp ? AccountRootFlags.DISALLOW_XRP.getValue() : 0L) |
      (lsfGlobalFreeze ? AccountRootFlags.GLOBAL_FREEZE.getValue() : 0L) |
      (lsfNoFreeze ? AccountRootFlags.NO_FREEZE.getValue() : 0L) |
      (lsfPasswordSpent ? AccountRootFlags.PASSWORD_SPENT.getValue() : 0L) |
      (lsfRequireAuth ? AccountRootFlags.REQUIRE_AUTH.getValue() : 0L) |
      (lsfRequireDestTag ? AccountRootFlags.REQUIRE_DEST_TAG.getValue() : 0L);
    AccountRootFlags flags = AccountRootFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);

    assertThat(flags.lsfDefaultRipple()).isEqualTo(lsfDefaultRipple);
    assertThat(flags.lsfDepositAuth()).isEqualTo(lsfDepositAuth);
    assertThat(flags.lsfDisableMaster()).isEqualTo(lsfDisableMaster);
    assertThat(flags.lsfDisallowXrp()).isEqualTo(lsfDisallowXrp);
    assertThat(flags.lsfGlobalFreeze()).isEqualTo(lsfGlobalFreeze);
    assertThat(flags.lsfNoFreeze()).isEqualTo(lsfNoFreeze);
    assertThat(flags.lsfPasswordSpent()).isEqualTo(lsfPasswordSpent);
    assertThat(flags.lsfRequireAuth()).isEqualTo(lsfRequireAuth);
    assertThat(flags.lsfRequireDestTag()).isEqualTo(lsfRequireDestTag);
  }
}
