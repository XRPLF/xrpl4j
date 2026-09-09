package org.xrpl.xrpl4j.model.transactions;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.api.AssertionsForClassTypes;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigDecimal;

/**
 * Unit tests for {@link TransferFee}.
 */
public class TransferFeeTest {

  private static final TransferFee TRANSFER_FEE = TransferFee.of(UnsignedInteger.ONE);

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> TransferFee.of(null));
  }

  @Test
  void testEquality() {
    AssertionsForClassTypes.assertThat(TRANSFER_FEE).isEqualTo(TRANSFER_FEE);
    AssertionsForClassTypes.assertThat(TRANSFER_FEE).isNotEqualTo(new Object());
    AssertionsForClassTypes.assertThat(TRANSFER_FEE.equals(null)).isFalse();
  }

  @Test
  void ofPercent() {
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(0)).value()).isEqualTo(UnsignedInteger.valueOf(0));
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(0.000)).value()).isEqualTo(UnsignedInteger.valueOf(0));
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(49.999)).value()).isEqualTo(UnsignedInteger.valueOf(49_999));
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(49.99)).value()).isEqualTo(UnsignedInteger.valueOf(49_990));
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(49.9)).value()).isEqualTo(UnsignedInteger.valueOf(49_900));
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(50)).value()).isEqualTo(UnsignedInteger.valueOf(50_000));
    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(50.000)).value()).isEqualTo(UnsignedInteger.valueOf(50_000));
  }

  @Test
  void ofPercentWithNull() {
    assertThatThrownBy(() -> TransferFee.ofPercent(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void transferFeeEquality() {
    assertThat(TransferFee.of(UnsignedInteger.ONE)).isEqualTo(TransferFee.of(UnsignedInteger.ONE));
    assertThat(TransferFee.of(UnsignedInteger.valueOf(10)))
      .isEqualTo(TransferFee.of(UnsignedInteger.valueOf(10)));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(49.99)))
      .isEqualTo(TransferFee.ofPercent(BigDecimal.valueOf(49.99)));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(49.9)))
      .isEqualTo(TransferFee.ofPercent(BigDecimal.valueOf(49.90)));

    assertThat(TransferFee.ofPercent(BigDecimal.valueOf(49.9)).value())
      .isEqualTo(UnsignedInteger.valueOf(49900));
  }

  @Test
  public void percentValueIncorrectFormat() {
    assertThatThrownBy(
      () -> TransferFee.ofPercent(BigDecimal.valueOf(25.2929))
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Percent value should have a maximum of 3 decimal places.");
  }

  @Test
  public void validateBounds() {
    assertDoesNotThrow(() -> TransferFee.of(UnsignedInteger.valueOf(49999)));
    assertDoesNotThrow(() -> TransferFee.ofPercent(BigDecimal.valueOf(49.999)));
    assertDoesNotThrow(() -> TransferFee.of(UnsignedInteger.valueOf(50000)));
    assertDoesNotThrow(() -> TransferFee.ofPercent(BigDecimal.valueOf(50.000)));

    assertThatThrownBy(() -> TransferFee.of(UnsignedInteger.valueOf(50001)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("TransferFee should be in the range 0 to 50000.");
    assertThatThrownBy(() -> TransferFee.ofPercent(BigDecimal.valueOf(50.001)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("TransferFee should be in the range 0 to 50000.");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    TransferFee transferFee = TransferFee.of(UnsignedInteger.valueOf(1000));
    TransferFeeWrapper wrapper = TransferFeeWrapper.of(transferFee);

    String json = "{\"transferFee\": 1000}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    TransferFeeWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    TransferFeeWrapper deserialized = objectMapper.readValue(
      serialized, TransferFeeWrapper.class
    );
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTransferFeeWrapper.class)
  @JsonDeserialize(as = ImmutableTransferFeeWrapper.class)
  interface TransferFeeWrapper {

    static TransferFeeWrapper of(TransferFee transferFee) {
      return ImmutableTransferFeeWrapper.builder().transferFee(transferFee).build();
    }

    TransferFee transferFee();

  }
}
