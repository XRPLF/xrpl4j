package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.codec.binary.definitions.DefinitionsService;
import org.xrpl.xrpl4j.codec.fixtures.FixtureUtils;
import org.xrpl.xrpl4j.codec.fixtures.data.ValueTest;

import java.io.IOException;
import java.util.stream.Stream;

abstract class BaseSerializerTypeTest {

  protected static Stream<Arguments> dataDrivenFixturesForType(SerializedType serializedType) throws IOException {
    return FixtureUtils.getDataDrivenFixtures().valuesTests()
      .stream()
      .filter(fixture -> fixture.type().equals(SerializedType.getNameByType(serializedType)))
      .map(Arguments::of);
  }

  abstract SerializedType getType();

  @ParameterizedTest
  @MethodSource("dataDrivenFixtures")
  void fixtureTests(ValueTest fixture) throws IOException {
    SerializedType<?> serializedType = getType();
    JsonNode value = getValue(fixture);
    if (fixture.error() != null) {
      assertThrows(Exception.class, () -> serializedType.fromJson(value));
    } else {
      SerializedType<?> serialized = serializedType.fromJson(value);
      if (fixture.type().equals("Amount")) {
        AmountType amountType = (AmountType) serialized;
        // Special-case for zero-value IOU amounts.
        if (
          !amountType.toJson().has("mpt_issuance_id") && // <-- Not MPT
          amountType.toJson().has("value") && // <-- Is IOU
            (
              amountType.toJson().get("value").asText().equals("0") ||
              amountType.toJson().get("value").asText().equals("0.0")
            )
        ) {
          // An apparent bug in AmountType always sets the negative/positive boolean to `negative` when the amount
          // values are `0` or `0.0`, so this special case must exist in order for tests to pass. Once
          // https://github.com/XRPLF/xrpl4j/issues/610 is fixed, then this block can be removed and all tests should
          // adhere to the single fixture check.
          assertThat(amountType.isPositive()).isFalse();
        } else {  // <-- XRP or MPT
          // XRP Note: In theory XRP should never be negative, however the binary codec supports negative and positive
          // zero-value XRP amounts.
          // MPT Note: MPT code honors the sign-bit in STAmount and also honorts the leading negative-sign in front
          // of any zeros.
          assertThat(amountType.isPositive()).isEqualTo(!fixture.isNegative());
        }
        assertThat(amountType.isNative()).isEqualTo(fixture.isNative());
      }
      assertThat(serialized.toHex()).isEqualTo(fixture.expectedHex());
    }
  }

  private JsonNode getValue(ValueTest test) {
    return DefinitionsService.getInstance()
      .mapFieldSpecialization(test.typeSpecializationField(), test.testJson().asText())
      .map(val -> val.toString())
      .map(TextNode::new)
      .map(JsonNode.class::cast)
      .orElse(test.testJson());
  }

}
