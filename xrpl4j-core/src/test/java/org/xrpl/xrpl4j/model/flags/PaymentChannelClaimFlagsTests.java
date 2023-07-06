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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class PaymentChannelClaimFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfRenew,
    boolean tfClose
  ) {
    PaymentChannelClaimFlags flags = PaymentChannelClaimFlags.builder()
      .tfRenew(tfRenew)
      .tfClose(tfClose)
      .build();

    assertThat(flags.getValue()).isEqualTo(getExpectedFlags(tfRenew, tfClose));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfRenew,
    boolean tfClose
  ) {
    long expectedFlags = getExpectedFlags(tfRenew, tfClose);
    PaymentChannelClaimFlags flags = PaymentChannelClaimFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfRenew()).isEqualTo(tfRenew);
    assertThat(flags.tfClose()).isEqualTo(tfClose);
  }

  @Test
  void testEmptyFlags() {
    PaymentChannelClaimFlags flags = PaymentChannelClaimFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfRenew()).isFalse();
    assertThat(flags.tfClose()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfRenew,
    boolean tfClose
  ) throws JSONException, JsonProcessingException {
    PaymentChannelClaimFlags flags = PaymentChannelClaimFlags.builder()
      .tfRenew(tfRenew)
      .tfClose(tfClose)
      .build();

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());


    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson() throws JSONException, JsonProcessingException {
    PaymentChannelClaimFlags flags = PaymentChannelClaimFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{\n" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  private long getExpectedFlags(boolean tfRenew, boolean tfClose) {
    return (PaymentChannelClaimFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfRenew ? PaymentChannelClaimFlags.RENEW.getValue() : 0L) |
      (tfClose ? PaymentChannelClaimFlags.CLOSE.getValue() : 0L);
  }
}
