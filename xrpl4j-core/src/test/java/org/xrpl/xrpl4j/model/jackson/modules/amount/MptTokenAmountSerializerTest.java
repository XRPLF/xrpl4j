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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.jackson.modules.MptTokenAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;

/**
 * Unit tests for {@link MptTokenAmountSerializer}.
 */
class MptTokenAmountSerializerTest {

  private static final String ISSUANCE_ID = "00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void serializesMptObject() throws JsonProcessingException, JSONException {
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(1000L)))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"1000\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }

  @Test
  void serializesZeroValue() throws JsonProcessingException, JSONException {
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.ZERO))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"0\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }

  @Test
  void serializesNegativeValue() throws JsonProcessingException, JSONException {
    // Negative values appear in transaction metadata
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(500L), true))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"-500\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }

  @Test
  void serializesConvenienceBuilder() throws JsonProcessingException, JSONException {
    // Verify the UnsignedLong convenience factory produces the same wire format
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.valueOf(1000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"1000\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }
}
