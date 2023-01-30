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

public class OfferCreateFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    OfferCreateFlags flags = OfferCreateFlags.builder()
      .tfPassive(tfPassive)
      .tfImmediateOrCancel(tfImmediateOrCancel)
      .tfFillOrKill(tfFillOrKill)
      .tfSell(tfSell)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfPassive, tfImmediateOrCancel, tfFillOrKill, tfSell));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    long expectedFlags = getExpectedFlags(tfPassive, tfImmediateOrCancel, tfFillOrKill, tfSell);
    OfferCreateFlags flags = OfferCreateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfPassive()).isEqualTo(tfPassive);
    assertThat(flags.tfImmediateOrCancel()).isEqualTo(tfImmediateOrCancel);
    assertThat(flags.tfFillOrKill()).isEqualTo(tfFillOrKill);
    assertThat(flags.tfSell()).isEqualTo(tfSell);
  }

  private long getExpectedFlags(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    return (OfferCreateFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfPassive ? OfferCreateFlags.PASSIVE.getValue() : 0L) |
      (tfImmediateOrCancel ? OfferCreateFlags.IMMEDIATE_OR_CANCEL.getValue() : 0L) |
      (tfFillOrKill ? OfferCreateFlags.FILL_OR_KILL.getValue() : 0L) |
      (tfSell ? OfferCreateFlags.SELL.getValue() : 0L);
  }
}
