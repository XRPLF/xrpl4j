package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import com.jayway.jsonassert.JsonAssert;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link BatchSignerWrapper}.
 */
class BatchSignerWrapperTest {

  private ObjectMapper objectMapper;

  private static final Address ACCOUNT1 = Address.of("rDpXrnLYA4n3AC2s9z9QxSbEUHrXt5xMVk");

  private static final PublicKey PUBLIC_KEY1 = PublicKey.fromBase16EncodedPublicKey(
    "EDE57F52F5C23803AC0FBADBB92917EF21BA0874145D7126CDDE56A2938A39C15F"
  );
  private static final Address SIGNER1_ADDRESS = Address.of("rs5zPPKr7pgkvu4xFBdum1FcKHJeSRL7JP");

  private static final PublicKey PUBLIC_KEY2 = PublicKey.fromBase16EncodedPublicKey(
    "ED17036166A5354D564A86F5D1E43A08C350F6B0A817249158B6F8F23DA861A940"
  );
  private static final Address SIGNER2_ADDRESS = Address.of("rUJBVHUDm6FXWseGj5oA6iv1enUzDGtMYo");

  private static final Signature SIGNATURE1 = Signature.fromBase16(
    "7A2FFB36E0C3C5F79534AF782D0CB5CBF0EA834F856B4A9F3F0DFA894C1B53C3" +
      "13EC9F5FD68304AE4AD5CFF7F22EAE27E59B00C98208FC785749A568AD43D506"
  );

  private static final Signature SIGNATURE2 = Signature.fromBase16(
    "8A2FFB36E0C3C5F79534AF782D0CB5CBF0EA834F856B4A9F3F0DFA894C1B53C3" +
      "13EC9F5FD68304AE4AD5CFF7F22EAE27E59B00C98208FC785749A568AD43D506"
  );

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void testBuilderWithDirectSigning() {
    BatchSigner batchSigner = BatchSigner.builder()
      .account(ACCOUNT1)
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    BatchSignerWrapper wrapper = BatchSignerWrapper.of(batchSigner);

    assertThat(wrapper.batchSigner()).isEqualTo(batchSigner);
    assertThat(wrapper.batchSigner().account()).isEqualTo(ACCOUNT1);
    assertThat(wrapper.batchSigner().signingPublicKey()).isPresent();
    assertThat(wrapper.batchSigner().transactionSignature()).isPresent();
    assertThat(wrapper.batchSigner().signers()).isEmpty();
    assertThat(wrapper.batchSigner().sortedSigners()).isFalse();
  }

  @Test
  void testBuilderWithMultiSig() {
    Signer signer1 = Signer.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    Signer signer2 = Signer.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE2)
      .build();

    BatchSigner batchSigner = BatchSigner.builder()
      .account(ACCOUNT1)
      .signers(java.util.Arrays.asList(
        SignerWrapper.of(signer1),
        SignerWrapper.of(signer2)
      ))
      .build();

    BatchSignerWrapper wrapper = BatchSignerWrapper.of(batchSigner);

    assertThat(wrapper.batchSigner()).isEqualTo(batchSigner);
    assertThat(wrapper.batchSigner().account()).isEqualTo(ACCOUNT1);
    assertThat(wrapper.batchSigner().signingPublicKey()).isEmpty();
    assertThat(wrapper.batchSigner().transactionSignature()).isEmpty();
    assertThat(wrapper.batchSigner().signers()).hasSize(2);
    assertThat(wrapper.batchSigner().sortedSigners()).isTrue();
  }

  @Test
  void testOf() {
    BatchSigner batchSigner = BatchSigner.builder()
      .account(SIGNER1_ADDRESS)
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    BatchSignerWrapper wrapper = BatchSignerWrapper.of(batchSigner);

    assertThat(wrapper.batchSigner()).isEqualTo(batchSigner);
  }

  @Test
  void testJsonSerializationWithDirectSigning() throws JsonProcessingException, JSONException {
    BatchSigner batchSigner = BatchSigner.builder()
      .account(SIGNER1_ADDRESS)
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    BatchSignerWrapper wrapper = BatchSignerWrapper.of(batchSigner);

    String expectedJson = String.format(
      "{\"BatchSigner\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}",
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value()
    );

    assertSerializesAndDeserializes(wrapper, expectedJson);
  }


  @Test
  void testJsonSerializationWithMultiSig() throws JsonProcessingException, JSONException {
    Signer signer1 = Signer.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    Signer signer2 = Signer.builder()
      .signingPublicKey(PUBLIC_KEY2)
      .transactionSignature(SIGNATURE2)
      .build();

    BatchSigner batchSigner = BatchSigner.builder()
      .account(ACCOUNT1)
      .signers(java.util.Arrays.asList(
        SignerWrapper.of(signer1),
        SignerWrapper.of(signer2)
      ))
      .build();

    BatchSignerWrapper wrapper = BatchSignerWrapper.of(batchSigner);

    String expectedJson = String.format(
      "{\"BatchSigner\":{\"Account\":\"%s\"," +
        "\"Signers\":[" +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}," +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}" +
        "]}}",
      ACCOUNT1.value(),
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value(),
      SIGNER2_ADDRESS.value(),
      PUBLIC_KEY2.base16Value(),
      SIGNATURE2.base16Value()
    );

    assertSerializesAndDeserializes(wrapper, expectedJson);
  }

  @Test
  void testJsonDeserializeAndSerialize() throws JsonProcessingException {
    String json = String.format(
      "{\"BatchSigner\":{\"Account\":\"%s\"," +
        "\"Signers\":[" +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}," +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}" +
        "]}}",
      ACCOUNT1.value(),
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value(),
      SIGNER2_ADDRESS.value(),
      PUBLIC_KEY2.base16Value(),
      SIGNATURE2.base16Value()
    );

    BatchSignerWrapper deserialized = objectMapper.readValue(json, BatchSignerWrapper.class);

    assertThat(deserialized.batchSigner().account()).isEqualTo(ACCOUNT1);
    assertThat(deserialized.batchSigner().signers()).hasSize(2);
    assertThat(deserialized.batchSigner().signingPublicKey()).isEmpty();
    assertThat(deserialized.batchSigner().transactionSignature()).isEmpty();

    String serialized = objectMapper.writeValueAsString(deserialized);
    JsonAssert.with(serialized).assertNotNull("$.BatchSigner");
    JsonAssert.with(serialized).assertNotNull("$.BatchSigner.Account");
    JsonAssert.with(serialized).assertNotNull("$.BatchSigner.Signers");

    BatchSignerWrapper roundTrip = objectMapper.readValue(serialized, BatchSignerWrapper.class);
    assertThat(roundTrip).isEqualTo(deserialized);
  }

  private void assertSerializesAndDeserializes(
    BatchSignerWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    BatchSignerWrapper deserialized = objectMapper.readValue(serialized, BatchSignerWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }
}
