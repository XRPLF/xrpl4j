package org.xrpl.xrpl4j.model.jackson.modules.amount;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.jackson.modules.TokenAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.amount.IouTokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.TokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.XrpTokenAmount;

/**
 * Unit tests for {@link TokenAmountDeserializer} — the polymorphic dispatcher that chooses {@link XrpTokenAmount},
 * {@link IouTokenAmount}, or {@link MptTokenAmount} based on wire shape.
 */
class TokenAmountDeserializerTest {

  private static final String ISSUER = "rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV";
  private static final String ISSUANCE_ID = "00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  // -------------------------------------------------------------------------
  // XRP dispatch — bare JSON string
  // -------------------------------------------------------------------------

  @Test
  void deserializesXrpFromString() throws JsonProcessingException {
    TokenAmount amount = objectMapper.readValue("\"1000000\"", TokenAmount.class);
    assertThat(amount).isInstanceOf(XrpTokenAmount.class);
    XrpTokenAmount xrpTokenAmount = (XrpTokenAmount) amount;
    assertThat(xrpTokenAmount.amount().value()).isEqualTo("1000000");
    assertThat(xrpTokenAmount.amount().isNegative()).isFalse();
  }

  @Test
  void deserializesNegativeXrpFromString() throws JsonProcessingException {
    // Negative XRP drop values appear in transaction metadata
    TokenAmount tokenAmount = objectMapper.readValue("\"-500\"", TokenAmount.class);
    assertThat(tokenAmount).isInstanceOf(XrpTokenAmount.class);
    assertThat(((XrpTokenAmount) tokenAmount).amount().isNegative()).isTrue();
    assertThat(((XrpTokenAmount) tokenAmount).amount().unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  @Test
  void deserializesZeroXrpFromString() throws JsonProcessingException {
    TokenAmount amount = objectMapper.readValue("\"0\"", TokenAmount.class);
    assertThat(amount).isInstanceOf(XrpTokenAmount.class);
  }

  // -------------------------------------------------------------------------
  // IOU dispatch — object with currency + issuer fields
  // -------------------------------------------------------------------------

  @Test
  void deserializesIouFromObject() throws JsonProcessingException {
    String json = "{\"value\":\"100.50\",\"currency\":\"USD\",\"issuer\":\"" + ISSUER + "\"}";
    TokenAmount amount = objectMapper.readValue(json, TokenAmount.class);
    assertThat(amount).isInstanceOf(IouTokenAmount.class);
    IouTokenAmount iou = (IouTokenAmount) amount;
    assertThat(iou.amount().value()).isEqualTo("100.50");
    assertThat(iou.currency()).isEqualTo("USD");
    assertThat(iou.issuer()).isEqualTo(Address.of(ISSUER));
  }

  @Test
  void deserializesNegativeIouFromObject() throws JsonProcessingException {
    String json = "{\"value\":\"-50.5\",\"currency\":\"EUR\",\"issuer\":\"" + ISSUER + "\"}";
    TokenAmount tokenAmount = objectMapper.readValue(json, TokenAmount.class);
    assertThat(tokenAmount).isInstanceOf(IouTokenAmount.class);
    assertThat(((IouTokenAmount) tokenAmount).amount().isNegative()).isTrue();
  }

  @Test
  void deserializesIouScientificNotation() throws JsonProcessingException {
    String json = "{\"value\":\"1.23e10\",\"currency\":\"USD\",\"issuer\":\"" + ISSUER + "\"}";
    TokenAmount amount = objectMapper.readValue(json, TokenAmount.class);
    assertThat(amount).isInstanceOf(IouTokenAmount.class);
    assertThat(((IouTokenAmount) amount).amount().value()).isEqualTo("1.23e10");
  }

  // -------------------------------------------------------------------------
  // MPT dispatch — object with mpt_issuance_id field
  // -------------------------------------------------------------------------

  @Test
  void deserializesMptFromObject() throws JsonProcessingException {
    String json = "{\"value\":\"1000\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}";
    TokenAmount amount = objectMapper.readValue(json, TokenAmount.class);
    assertThat(amount).isInstanceOf(MptTokenAmount.class);
    MptTokenAmount mpt = (MptTokenAmount) amount;
    assertThat(mpt.amount().value()).isEqualTo("1000");
    assertThat(mpt.mptIssuanceId()).isEqualTo(MpTokenIssuanceId.of(ISSUANCE_ID));
  }

  @Test
  void deserializesNegativeMptFromObject() throws JsonProcessingException {
    // Negative MPT values appear in transaction metadata
    String json = "{\"value\":\"-500\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}";
    TokenAmount amount = objectMapper.readValue(json, TokenAmount.class);
    assertThat(amount).isInstanceOf(MptTokenAmount.class);
    MptTokenAmount mpt = (MptTokenAmount) amount;
    assertThat(mpt.isNegative()).isTrue();
    assertThat(mpt.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }
}
