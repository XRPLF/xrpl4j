

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

@SuppressWarnings("abbreviationaswordinname")
public class NfTokenMintFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(5);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable,
    boolean tfInnerBatchTxn
  ) {
    NfTokenMintFlags flags = NfTokenMintFlags.builder()
      .tfBurnable(tfBurnable)
      .tfOnlyXRP(tfOnlyXRP)
      .tfTrustLine(tfTrustLine)
      .tfTransferable(tfTransferable)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable, tfInnerBatchTxn);
    NfTokenMintFlags flags = NfTokenMintFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfBurnable()).isEqualTo(tfBurnable);
    assertThat(flags.tfOnlyXRP()).isEqualTo(tfOnlyXRP);
    assertThat(flags.tfTrustLine()).isEqualTo(tfTrustLine);
    assertThat(flags.tfTransferable()).isEqualTo(tfTransferable);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testEmptyFlags() {
    NfTokenMintFlags flags = NfTokenMintFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfBurnable()).isFalse();
    assertThat(flags.tfOnlyXRP()).isFalse();
    assertThat(flags.tfTrustLine()).isFalse();
    assertThat(flags.tfTransferable()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    long expectedFlags = getExpectedFlags(tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable, tfInnerBatchTxn);
    NfTokenMintFlags flags = NfTokenMintFlags.of(expectedFlags);

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());


    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson() throws JSONException, JsonProcessingException {
    NfTokenMintFlags flags = NfTokenMintFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{\n" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testInnerBatchTxn() {
    NfTokenMintFlags flags = NfTokenMintFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfBurnable()).isFalse();
    assertThat(flags.tfOnlyXRP()).isFalse();
    assertThat(flags.tfTrustLine()).isFalse();
    assertThat(flags.tfTransferable()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  private long getExpectedFlags(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable,
    boolean tfInnerBatchTxn
  ) {
    return (NfTokenMintFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfBurnable ? NfTokenMintFlags.BURNABLE.getValue() : 0L) |
      (tfOnlyXRP ? NfTokenMintFlags.ONLY_XRP.getValue() : 0L) |
      (tfTrustLine ? NfTokenMintFlags.TRUSTLINE.getValue() : 0L) |
      (tfTransferable ? NfTokenMintFlags.TRANSFERABLE.getValue() : 0L) |
      (tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}
