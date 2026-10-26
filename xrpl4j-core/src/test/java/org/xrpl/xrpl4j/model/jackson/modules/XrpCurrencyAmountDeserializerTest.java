package org.xrpl.xrpl4j.model.jackson.modules;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link XrpCurrencyAmountDeserializer}.
 *
 * <p>These tests guard against non-standard XRP drops amounts (e.g. exponential notation like {@code "1e6"}) being
 * silently coerced by {@link com.fasterxml.jackson.core.JsonParser#getValueAsLong()} into a different drops value
 * than what an application may have already validated against the raw input.</p>
 */
class XrpCurrencyAmountDeserializerTest extends AbstractJsonTest {

  @Test
  void deserializesPlainIntegerDropsString() throws JsonProcessingException {
    XrpCurrencyAmount amount = objectMapper.readValue("\"1000000\"", XrpCurrencyAmount.class);
    assertThat(amount).isEqualTo(XrpCurrencyAmount.ofDrops(1000000));
  }

  @Test
  void deserializesNegativeIntegerDropsString() throws JsonProcessingException {
    XrpCurrencyAmount amount = objectMapper.readValue("\"-1000000\"", XrpCurrencyAmount.class);
    assertThat(amount).isEqualTo(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(1000000), true));
  }

  @Test
  void rejectsExponentialNotation() {
    assertThatThrownBy(() -> objectMapper.readValue("\"1e6\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string")
      .hasMessageContaining("1e6");
  }

  @Test
  void rejectsDecimalAmount() {
    assertThatThrownBy(() -> objectMapper.readValue("\"1.0\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string");
  }

  @Test
  void rejectsHexAmount() {
    assertThatThrownBy(() -> objectMapper.readValue("\"0x1\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string");
  }

  @Test
  void rejectsLeadingPlusSign() {
    assertThatThrownBy(() -> objectMapper.readValue("\"+1000000\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string");
  }

  @Test
  void rejectsWhitespace() {
    assertThatThrownBy(() -> objectMapper.readValue("\" 1000000\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string");
  }

  @Test
  void rejectsEmptyString() {
    assertThatThrownBy(() -> objectMapper.readValue("\"\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string");
  }

  @Test
  void rejectsOutOfRangeAmount() {
    assertThatThrownBy(() -> objectMapper.readValue("\"99999999999999999999\"", XrpCurrencyAmount.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("out of range for a 64-bit integer");
  }

  @Test
  void rejectsNonStandardAmountInPaymentChannelClaim() {
    String json = "{" +
      "  \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"TransactionType\": \"PaymentChannelClaim\"," +
      "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\"," +
      "  \"Balance\": \"1000000\"," +
      "  \"Amount\": \"1e6\"," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertThatThrownBy(() -> objectMapper.readValue(json, PaymentChannelClaim.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("XRP drops amount must be a plain integer string");
  }
}
