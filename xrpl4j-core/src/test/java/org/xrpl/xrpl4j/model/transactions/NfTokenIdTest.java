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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link NfTokenId}.
 */
public class NfTokenIdTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  private final NfTokenId NF_TOKEN_ID_ZERO = NfTokenId.of(
    "0000000000000000000000000000000000000000000000000000000000000000");

  private final NfTokenId NF_TOKEN_ID_UPPER = NfTokenId.of(
    "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");

  private final NfTokenId NF_TOKEN_ID_LOWER = NfTokenId.of(
    "000b013a95f14b0044f78a264e41713c64b5f89242540ee208c3098e00000d65");

  @Test
  public void testOfNull() {
    assertThrows(NullPointerException.class, () -> NfTokenId.of(null));
  }

  @Test
  public void nfTokenEquality() {
    assertThat(NF_TOKEN_ID_UPPER).isEqualTo(NF_TOKEN_ID_UPPER);
    assertThat(NF_TOKEN_ID_ZERO.equals(null)).isFalse();
    assertThat(NF_TOKEN_ID_ZERO).isNotEqualTo(new Object());
  }

  @Test
  public void testToString() {
    String tokenId = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65";
    NfTokenId nfTokenId = NfTokenId.of(tokenId);
    assertThat(nfTokenId.toString()).isEqualTo(tokenId);
  }

  @Test
  public void nfTokenHashcode() {
    assertThat(NF_TOKEN_ID_UPPER.hashCode()).isEqualTo(NF_TOKEN_ID_UPPER.hashCode());

    // Verify case-insensitive hashCode
    assertThat(NF_TOKEN_ID_UPPER.hashCode()).isEqualTo(NF_TOKEN_ID_LOWER.hashCode());
  }

  @Test
  public void testValidateLength() {
    // Should not throw exception for 64-character string
    assertThat(NF_TOKEN_ID_UPPER.value()).hasSize(64);
  }

  @Test
  public void testInvalidLength() {
    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D6"),
      "TokenId must be 64 characters long."
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D6500"),
      "TokenId must be 64 characters long."
    );
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException, JSONException {
    NfTokenIdWrapper wrapper = ImmutableNfTokenIdWrapper.builder()
      .value(NF_TOKEN_ID_UPPER)
      .build();
    assertSerializesAndDeserializes(wrapper,
      "{\"value\":\"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\"}");
  }

  private void assertSerializesAndDeserializes(NfTokenIdWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    NfTokenIdWrapper deserialized = objectMapper.readValue(serialized, NfTokenIdWrapper.class);
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableNfTokenIdWrapper.class)
  @JsonDeserialize(as = ImmutableNfTokenIdWrapper.class)
  interface NfTokenIdWrapper {

    NfTokenId value();
  }
}
