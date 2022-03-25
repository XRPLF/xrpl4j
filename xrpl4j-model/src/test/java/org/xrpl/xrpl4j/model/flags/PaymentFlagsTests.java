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

public class PaymentFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfFullyCanonicalSig,
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality
  ) {
    Flags.PaymentFlags flags = Flags.PaymentFlags.builder()
      .tfFullyCanonicalSig(tfFullyCanonicalSig)
      .tfNoDirectRipple(tfNoDirectRipple)
      .tfPartialPayment(tfPartialPayment)
      .tfLimitQuality(tfLimitQuality)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfFullyCanonicalSig, tfNoDirectRipple, tfPartialPayment, tfLimitQuality));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfFullyCanonicalSig,
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality
  ) {
    long expectedFlags = getExpectedFlags(tfFullyCanonicalSig, tfNoDirectRipple, tfPartialPayment, tfLimitQuality);
    Flags.PaymentFlags flags = Flags.PaymentFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfNoDirectRipple()).isEqualTo(tfNoDirectRipple);
    assertThat(flags.tfPartialPayment()).isEqualTo(tfPartialPayment);
    assertThat(flags.tfLimitQuality()).isEqualTo(tfLimitQuality);
  }

  private long getExpectedFlags(
    boolean tfFullyCanonicalSig,
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality
  ) {
    return (tfFullyCanonicalSig ? Flags.PaymentFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
      (tfNoDirectRipple ? Flags.PaymentFlags.NO_DIRECT_RIPPLE.getValue() : 0L) |
      (tfPartialPayment ? Flags.PaymentFlags.PARTIAL_PAYMENT.getValue() : 0L) |
      (tfLimitQuality ? Flags.PaymentFlags.LIMIT_QUALITY.getValue() : 0L);
  }
}
