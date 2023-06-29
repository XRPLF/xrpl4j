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

public class NfTokenFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("data")
  @SuppressWarnings("AbbreviationAsWordInName")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfBurnable,
    boolean lsfOnlyXrp,
    boolean lsfTrustLine,
    boolean lsfTransferable
  ) {
    long expectedFlags = (lsfBurnable ? NfTokenFlags.BURNABLE.getValue() : 0L) |
      (lsfOnlyXrp ? NfTokenFlags.ONLY_XRP.getValue() : 0L) |
      (lsfTrustLine ? NfTokenFlags.TRUST_LINE.getValue() : 0L) |
      (lsfTransferable ? NfTokenFlags.TRANSFERABLE.getValue() : 0L);
    Flags flagsFromFlags = NfTokenFlags.of(
      (lsfBurnable ? NfTokenFlags.BURNABLE : NfTokenFlags.UNSET),
      (lsfOnlyXrp ? NfTokenFlags.ONLY_XRP : NfTokenFlags.UNSET),
      (lsfTrustLine ? NfTokenFlags.TRUST_LINE : NfTokenFlags.UNSET),
      (lsfTransferable ? NfTokenFlags.TRANSFERABLE : NfTokenFlags.UNSET)
    );
    assertThat(flagsFromFlags.getValue()).isEqualTo(expectedFlags);

    NfTokenFlags flagsFromLong = NfTokenFlags.of(expectedFlags);

    assertThat(flagsFromLong.getValue()).isEqualTo(expectedFlags);

    assertThat(flagsFromLong.lsfBurnable()).isEqualTo(lsfBurnable);
    assertThat(flagsFromLong.lsfOnlyXrp()).isEqualTo(lsfOnlyXrp);
    assertThat(flagsFromLong.lsfTrustLine()).isEqualTo(lsfTrustLine);
    assertThat(flagsFromLong.lsfTransferable()).isEqualTo(lsfTransferable);
  }
}
