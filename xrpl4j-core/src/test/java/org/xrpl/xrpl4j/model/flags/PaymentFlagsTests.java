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

public class PaymentFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality,
    boolean tfInnerBatchTxn
  ) {
    PaymentFlags flags = PaymentFlags.builder()
      .tfNoDirectRipple(tfNoDirectRipple)
      .tfPartialPayment(tfPartialPayment)
      .tfLimitQuality(tfLimitQuality)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfNoDirectRipple, tfPartialPayment, tfLimitQuality, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(tfNoDirectRipple, tfPartialPayment, tfLimitQuality, tfInnerBatchTxn);
    PaymentFlags flags = PaymentFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfNoDirectRipple()).isEqualTo(tfNoDirectRipple);
    assertThat(flags.tfPartialPayment()).isEqualTo(tfPartialPayment);
    assertThat(flags.tfLimitQuality()).isEqualTo(tfLimitQuality);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testEmptyFlags() {
    PaymentFlags flags = PaymentFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfNoDirectRipple()).isFalse();
    assertThat(flags.tfPartialPayment()).isFalse();
    assertThat(flags.tfLimitQuality()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    PaymentFlags flags = PaymentFlags.builder()
      .tfNoDirectRipple(tfNoDirectRipple)
      .tfPartialPayment(tfPartialPayment)
      .tfLimitQuality(tfLimitQuality)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());


    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson() throws JSONException, JsonProcessingException {
    PaymentFlags flags = PaymentFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{\n" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testInnerBatchTxn() {
    PaymentFlags flags = PaymentFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfNoDirectRipple()).isFalse();
    assertThat(flags.tfPartialPayment()).isFalse();
    assertThat(flags.tfLimitQuality()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  private long getExpectedFlags(
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality,
    boolean tfInnerBatchTxn
  ) {
    return (PaymentFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfNoDirectRipple ? PaymentFlags.NO_DIRECT_RIPPLE.getValue() : 0L) |
      (tfPartialPayment ? PaymentFlags.PARTIAL_PAYMENT.getValue() : 0L) |
      (tfLimitQuality ? PaymentFlags.LIMIT_QUALITY.getValue() : 0L) |
      (tfInnerBatchTxn ? PaymentFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}
