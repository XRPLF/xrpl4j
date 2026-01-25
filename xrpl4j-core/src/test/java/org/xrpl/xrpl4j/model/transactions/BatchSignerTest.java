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
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import java.util.Collections;

/**
 * Unit tests for {@link BatchSigner}.
 */
class BatchSignerTest {

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

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void testBuilderWithDirectSigning() {
    BatchSigner batchSigner = BatchSigner.builder()
      .account(SIGNER1_ADDRESS)
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    assertThat(batchSigner.account()).isEqualTo(SIGNER1_ADDRESS);
    assertThat(batchSigner.signingPublicKey()).isPresent();
    assertThat(batchSigner.signingPublicKey().get()).isEqualTo(PUBLIC_KEY1);
    assertThat(batchSigner.transactionSignature()).isPresent();
    assertThat(batchSigner.transactionSignature().get()).isEqualTo(SIGNATURE1);
    assertThat(batchSigner.signers()).isEmpty();
    assertThat(batchSigner.signers()).isEmpty();
    assertThat(batchSigner.sortedSigners()).isFalse();
  }

  @Test
  void testBuilderWithMultiSig() {
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

    assertThat(batchSigner.account()).isEqualTo(ACCOUNT1);
    assertThat(batchSigner.signingPublicKey()).isEmpty();
    assertThat(batchSigner.transactionSignature()).isEmpty();
    assertThat(batchSigner.signers()).hasSize(2);
    assertThat(batchSigner.signers()).hasSize(2);
    assertThat(batchSigner.sortedSigners()).isTrue();
  }

  @Test
  void testValidationFailsWithNoSigningFields() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      BatchSigner.builder()
        .account(ACCOUNT1)
        .build()
    );

    assertThat(exception.getMessage())
      .contains(
        "BatchSigner must have either (SigningPubKey and TxnSignature) or non-empty Signers array"
      );
  }

  @Test
  void testValidationFailsWithBothDirectSigningAndMultiSig() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
        BatchSigner.builder()
          .account(ACCOUNT1)
          .signingPublicKey(PUBLIC_KEY1)
          .transactionSignature(SIGNATURE1)
          .signers(Collections.singletonList(SignerWrapper.of(Signer.builder()
              .signingPublicKey(PUBLIC_KEY1)
              .transactionSignature(SIGNATURE1)
              .build()
          )))
          .build()
    );

    assertThat(exception.getMessage())
      .contains("BatchSigner cannot have both direct signing fields and Signers array");
  }

  @Test
  void testValidationFailsWithOnlySigningPublicKey() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      BatchSigner.builder()
        .account(ACCOUNT1)
        .signingPublicKey(PUBLIC_KEY1)
        .build()
    );

    assertThat(exception.getMessage())
      .contains("BatchSigner must have either (SigningPubKey and TxnSignature) or non-empty Signers array");
  }

  @Test
  void testValidationFailsWithOnlyTransactionSignature() {

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      BatchSigner.builder()
        .account(ACCOUNT1)
        .transactionSignature(SIGNATURE1)
        .build()
    );

    assertThat(exception.getMessage())
      .contains("BatchSigner must have either (SigningPubKey and TxnSignature) or non-empty Signers array");
  }

  @Test
  void testSignersSortedByAccountAddress() {
    // Create signers in intentionally unsorted order to verify sorting by decoded account ID

    Signer signer1 = Signer.builder()
      // rs5zPPKr7pgkvu4xFBdum1FcKHJeSRL7JP
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(
        "EDE57F52F5C23803AC0FBADBB92917EF21BA0874145D7126CDDE56A2938A39C15F"
      ))
      .transactionSignature(Signature.fromBase16(
        "7A2FFB36E0C3C5F79534AF782D0CB5CBF0EA834F856B4A9F3F0DFA89" +
          "4C1B53C313EC9F5FD68304AE4AD5CFF7F22EAE27E59B00C98208FC785749A568AD43D506"
      ))
      .build();

    Signer signer2 = Signer.builder()
      // r9LqNeG6qHxjeUocjvVki2XR35weJ9mZgQ
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(
        "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A"
      ))
      .transactionSignature(Signature.fromBase16(
        "C3646313B08EED6AF4392261A31B961F10C66CB733DB7F6CD9EAE9D9A1F8" +
          "622731C46B33BE9FA5B8B23F6AA560B1A3762F2F8159F54D9E65D54F4C3C5A6E9D0E"
      ))
      .build();

    Signer signer3 = Signer.builder()
      // r9zRhGr7b6xPekLvT6wP4qNdWMryaumZS7
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(
        "ED1A7C082846CFF58FF9A892BA4BA2593151CCF1DBA59F37714CC9ED39824AF85F"
      ))
      .transactionSignature(Signature.fromBase16(
        "A1B2C3D4E5F6071829384A5B6C7D8E9F0A1B2C3D4E5F6071829384A5B6C7" +
          "D8E9F0A1B2C3D4E5F6071829384A5B6C7D8E9F0A1B2C3D4E5F6071829384A5B6C7D8E"
      ))
      .build();

    Signer signer4 = Signer.builder()
      // rUJBVHUDm6FXWseGj5oA6iv1enUzDGtMYo
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(
        "ED17036166A5354D564A86F5D1E43A08C350F6B0A817249158B6F8F23DA861A940"
      ))
      .transactionSignature(Signature.fromBase16(
        "BDD4F92CE43E087BB17A19938A2DD70CA73BE980BD5083791F57" +
          "FC02C6C43434ADA52490CFAFC36FC0349A4649DF482927A0B13D9F9437199407FEFE4E354F0F"
      ))
      .build();

    Signer signer5 = Signer.builder()
      // rDTXLQ7ZKZVKz33zJbHjgVShjsBnqMBhmN
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(
        "ED9434799226374926EDA3B54B1B461B4ABF7237962EAE18528FEA67595397FA32"
      ))
      .transactionSignature(Signature.fromBase16(
        "D4E5F6071829384A5B6C7D8E9F0A1B2C3D4E5F6071829384A5B6C7D8E9F0A" +
          "1B2C3D4E5F6071829384A5B6C7D8E9F0A1B2C3D4E5F6071829384A5B6C7D8E9F0A1B"
      ))
      .build();

    // Add signers in intentionally random order
    BatchSigner batchSigner = BatchSigner.builder()
      .account(Address.of("rDpXrnLYA4n3AC2s9z9QxSbEUHrXt5xMVk"))
      .signers(java.util.Arrays.asList(
        SignerWrapper.of(signer2),  // r9LqNeG6qHxjeUocjvVki2XR35weJ9mZgQ
        SignerWrapper.of(signer3),  // r9zRhGr7b6xPekLvT6wP4qNdWMryaumZS7
        SignerWrapper.of(signer1),  // rs5zPPKr7pgkvu4xFBdum1FcKHJeSRL7JP
        SignerWrapper.of(signer5),  // rDTXLQ7ZKZVKz33zJbHjgVShjsBnqMBhmN
        SignerWrapper.of(signer4)  // rUJBVHUDm6FXWseGj5oA6iv1enUzDGtMYo
      ))
      .build();

    // Verify that signers() returns sorted list by decoded account ID (as BigInteger)
    // The sorting is done by decoding each address to its 20-byte account ID, converting to hex,
    // then comparing as BigInteger values in ascending order.
    // Expected sorted order (by hex value of decoded account ID):
    // 1. rs5zPPKr7pgkvu4xFBdum1FcKHJeSRL7JP -> 1DE135997D0269BFB485433A138F36D1F17D39A8
    // 2. r9LqNeG6qHxjeUocjvVki2XR35weJ9mZgQ -> 5B812C9D57731E27A2DA8B1830195F88EF32A3B6
    // 3. r9zRhGr7b6xPekLvT6wP4qNdWMryaumZS7 -> 629CCC144AC8464561F11D8870A57DC376A0D191
    // 4. rUJBVHUDm6FXWseGj5oA6iv1enUzDGtMYo -> 7BE935A57ECB927FA4A25FFAE84E483FCC989BA0
    // 5. rDTXLQ7ZKZVKz33zJbHjgVShjsBnqMBhmN -> 88A5A57C829F40F25EA83385BBDE6C3D8B4CA082
    assertThat(batchSigner.signers()).hasSize(5);
    assertThat(batchSigner.signers().get(0).signer().account()).isEqualTo(signer1.account());
    assertThat(batchSigner.signers().get(1).signer().account()).isEqualTo(signer2.account());
    assertThat(batchSigner.signers().get(2).signer().account()).isEqualTo(signer3.account());
    assertThat(batchSigner.signers().get(3).signer().account()).isEqualTo(signer4.account());
    assertThat(batchSigner.signers().get(4).signer().account()).isEqualTo(signer5.account());
  }

  @Test
  void testJsonSerializationWithDirectSigning() throws JsonProcessingException, JSONException {
    BatchSigner batchSigner = BatchSigner.builder()
      .account(SIGNER1_ADDRESS)
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    String expectedJson = String.format(
      "{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}",
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value()
    );

    assertSerializesAndDeserializes(batchSigner, expectedJson);
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

    String expectedJson = String.format("{\"Account\":\"%s\"," +
        "\"Signers\":[" +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}," +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}" +
        "]}",
      ACCOUNT1.value(),
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value(),
      SIGNER2_ADDRESS.value(),
      PUBLIC_KEY2.base16Value(),
      SIGNATURE2.base16Value()
    );

    assertSerializesAndDeserializes(batchSigner, expectedJson);
  }

  @Test
  void testJsonDeserializeAndSerialize() throws JsonProcessingException {
    String json = String.format("{\"Account\":\"%s\"," +
        "\"Signers\":[" +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}," +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}" +
        "]}",
      ACCOUNT1.value(),
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value(),
      SIGNER2_ADDRESS.value(),
      PUBLIC_KEY2.base16Value(),
      SIGNATURE2.base16Value()
    );

    BatchSigner deserialized = objectMapper.readValue(json, BatchSigner.class);

    assertThat(deserialized.account()).isEqualTo(ACCOUNT1);
    assertThat(deserialized.signers()).hasSize(2);
    assertThat(deserialized.signingPublicKey()).isEmpty();
    assertThat(deserialized.transactionSignature()).isEmpty();
    assertThat(deserialized.sortedSigners()).isTrue();

    String serialized = objectMapper.writeValueAsString(deserialized);
    JsonAssert.with(serialized).assertNotNull("$.Account");
    JsonAssert.with(serialized).assertNotNull("$.Signers");

    BatchSigner roundTrip = objectMapper.readValue(serialized, BatchSigner.class);
    assertThat(roundTrip).isEqualTo(deserialized);
  }

  private void assertSerializesAndDeserializes(
    BatchSigner batchSigner,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(batchSigner);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    BatchSigner deserialized = objectMapper.readValue(serialized, BatchSigner.class);
    assertThat(deserialized).isEqualTo(batchSigner);
  }
}
