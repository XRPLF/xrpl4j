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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class NfTokenCreateOfferFlagsTests  extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfSellToken,
    boolean tfInnerBatchTxn
  ) {
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.builder()
      .tfSellToken(tfSellToken)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfSellToken, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfSellToken,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(tfSellToken, tfInnerBatchTxn);
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfSellNfToken()).isEqualTo(tfSellToken);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testEmptyFlags() {
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfSellNfToken()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfSellToken,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.builder()
      .tfSellToken(tfSellToken)
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
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{\n" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testInnerBatchTxn() {
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfSellNfToken()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  private long getExpectedFlags(
    boolean tfSellToken,
    boolean tfInnerBatchTxn
  ) {
    return (TransactionFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfSellToken ? NfTokenCreateOfferFlags.SELL_NFTOKEN.getValue() : 0L) |
      (tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}
