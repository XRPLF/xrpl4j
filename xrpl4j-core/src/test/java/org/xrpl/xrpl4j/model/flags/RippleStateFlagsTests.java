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

public class RippleStateFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(8);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfLowReserve,
    boolean lsfHighReserve,
    boolean lsfLowAuth,
    boolean lsfHighAuth,
    boolean lsfLowNoRipple,
    boolean lsfHighNoRipple,
    boolean lsfLowFreeze,
    boolean lsfHighFreeze
  ) {
    long expectedFlags = getExpectedFlags(
      lsfLowReserve,
      lsfHighReserve,
      lsfLowAuth,
      lsfHighAuth,
      lsfLowNoRipple,
      lsfHighNoRipple,
      lsfLowFreeze,
      lsfHighFreeze
    );
    Flags.RippleStateFlags flags = Flags.RippleStateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfLowReserve()).isEqualTo(lsfLowReserve);
    assertThat(flags.lsfHighReserve()).isEqualTo(lsfHighReserve);
    assertThat(flags.lsfLowAuth()).isEqualTo(lsfLowAuth);
    assertThat(flags.lsfHighAuth()).isEqualTo(lsfHighAuth);
    assertThat(flags.lsfLowNoRipple()).isEqualTo(lsfLowNoRipple);
    assertThat(flags.lsfHighNoRipple()).isEqualTo(lsfHighNoRipple);
    assertThat(flags.lsfLowFreeze()).isEqualTo(lsfLowFreeze);
    assertThat(flags.lsfHighFreeze()).isEqualTo(lsfHighFreeze);
  }

  protected long getExpectedFlags(
    boolean lsfLowReserve,
    boolean lsfHighReserve,
    boolean lsfLowAuth,
    boolean lsfHighAuth,
    boolean lsfLowNoRipple,
    boolean lsfHighNoRipple,
    boolean lsfLowFreeze,
    boolean lsfHighFreeze
  ) {
    return (lsfLowReserve ? Flags.RippleStateFlags.LOW_RESERVE.getValue() : 0L) |
      (lsfHighReserve ? Flags.RippleStateFlags.HIGH_RESERVE.getValue() : 0L) |
      (lsfLowAuth ? Flags.RippleStateFlags.LOW_AUTH.getValue() : 0L) |
      (lsfHighAuth ? Flags.RippleStateFlags.HIGH_AUTH.getValue() : 0L) |
      (lsfLowNoRipple ? Flags.RippleStateFlags.LOW_NO_RIPPLE.getValue() : 0L) |
      (lsfHighNoRipple ? Flags.RippleStateFlags.HIGH_NO_RIPPLE.getValue() : 0L) |
      (lsfLowFreeze ? Flags.RippleStateFlags.LOW_FREEZE.getValue() : 0L) |
      (lsfHighFreeze ? Flags.RippleStateFlags.HIGH_FREEZE.getValue() : 0L);
  }
}
