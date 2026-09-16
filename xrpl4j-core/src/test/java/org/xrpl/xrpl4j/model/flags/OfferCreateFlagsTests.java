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

public class OfferCreateFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(6);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell,
    boolean tfHybrid,
    boolean tfInnerBatchTxn
  ) {
    OfferCreateFlags flags = OfferCreateFlags.builder()
      .tfPassive(tfPassive)
      .tfImmediateOrCancel(tfImmediateOrCancel)
      .tfFillOrKill(tfFillOrKill)
      .tfSell(tfSell)
      .tfHybrid(tfHybrid)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfPassive, tfImmediateOrCancel, tfFillOrKill, tfSell, tfHybrid, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell,
    boolean tfHybrid,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(
      tfPassive, tfImmediateOrCancel, tfFillOrKill, tfSell, tfHybrid, tfInnerBatchTxn
    );
    OfferCreateFlags flags = OfferCreateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfPassive()).isEqualTo(tfPassive);
    assertThat(flags.tfImmediateOrCancel()).isEqualTo(tfImmediateOrCancel);
    assertThat(flags.tfFillOrKill()).isEqualTo(tfFillOrKill);
    assertThat(flags.tfSell()).isEqualTo(tfSell);
    assertThat(flags.tfHybrid()).isEqualTo(tfHybrid);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testEmptyFlags() {
    OfferCreateFlags flags = OfferCreateFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfPassive()).isFalse();
    assertThat(flags.tfImmediateOrCancel()).isFalse();
    assertThat(flags.tfFillOrKill()).isFalse();
    assertThat(flags.tfSell()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfHybrid()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell,
    boolean tfHybrid,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    OfferCreateFlags flags = OfferCreateFlags.builder()
      .tfPassive(tfPassive)
      .tfImmediateOrCancel(tfImmediateOrCancel)
      .tfFillOrKill(tfFillOrKill)
      .tfSell(tfSell)
      .tfHybrid(tfHybrid)
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
    OfferCreateFlags flags = OfferCreateFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testInnerBatchTxn() {
    OfferCreateFlags flags = OfferCreateFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfPassive()).isFalse();
    assertThat(flags.tfImmediateOrCancel()).isFalse();
    assertThat(flags.tfFillOrKill()).isFalse();
    assertThat(flags.tfSell()).isFalse();
    assertThat(flags.tfHybrid()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  private long getExpectedFlags(
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell,
    boolean tfHybrid,
    boolean tfInnerBatchTxn
  ) {
    return (OfferCreateFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfPassive ? OfferCreateFlags.PASSIVE.getValue() : 0L) |
      (tfImmediateOrCancel ? OfferCreateFlags.IMMEDIATE_OR_CANCEL.getValue() : 0L) |
      (tfFillOrKill ? OfferCreateFlags.FILL_OR_KILL.getValue() : 0L) |
      (tfSell ? OfferCreateFlags.SELL.getValue() : 0L) |
      (tfHybrid ? OfferCreateFlags.HYBRID.getValue() : 0L) |
      (tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}
