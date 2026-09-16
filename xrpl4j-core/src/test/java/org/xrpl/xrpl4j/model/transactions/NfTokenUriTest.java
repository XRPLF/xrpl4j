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
 * Unit tests for {@link NfTokenUri}.
 */
public class NfTokenUriTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void nfTokenUriEquality() {

    final String hexUri =
      "697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634646675796" +
        "C71616266336F636C67747179353566627A6469";
    final NfTokenUri hexUriObject = NfTokenUri.of(hexUri);

    final String plaintextUri = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";
    final NfTokenUri plainTextNftokenUri = NfTokenUri.ofPlainText(plaintextUri);

    final String webUrl = "https://example.com";
    final NfTokenUri webUrlNfTokenUri = NfTokenUri.ofPlainText(webUrl);

    assertThat(hexUriObject).isEqualTo(hexUriObject);
    assertThat(plainTextNftokenUri).isEqualTo(plainTextNftokenUri);
    assertThat(hexUriObject).isEqualTo(plainTextNftokenUri);
    assertThat(plainTextNftokenUri).isEqualTo(hexUriObject);

    assertThat(webUrlNfTokenUri).isNotEqualTo(hexUri);
    assertThat(webUrlNfTokenUri).isNotEqualTo(plaintextUri);
    assertThat(hexUri).isNotEqualTo(webUrlNfTokenUri);
    assertThat(plaintextUri).isNotEqualTo(webUrlNfTokenUri);

    assertThat(hexUriObject.equals(null)).isFalse();
    Assertions.assertThat(hexUriObject).isNotEqualTo(new Object());
  }

  @Test
  public void testOfPlainTextWithNull() {
    assertThatThrownBy(() -> NfTokenUri.ofPlainText(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException, JSONException {
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");
    NfTokenUriWrapper wrapper = ImmutableNfTokenUriWrapper.builder()
      .value(uri)
      .build();
    assertSerializesAndDeserializes(wrapper,
      "{\"value\":\"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E663464" +
        "6675796C71616266336F636C67747179353566627A6469\"}");
  }

  private void assertSerializesAndDeserializes(NfTokenUriWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    NfTokenUriWrapper deserialized = objectMapper.readValue(serialized, NfTokenUriWrapper.class);
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableNfTokenUriWrapper.class)
  @JsonDeserialize(as = ImmutableNfTokenUriWrapper.class)
  interface NfTokenUriWrapper {

    NfTokenUri value();
  }
}
