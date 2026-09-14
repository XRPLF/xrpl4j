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
import com.google.common.collect.Sets;
import com.jayway.jsonassert.JsonAssert;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.Arrays;

/**
 * Unit tests for {@link CounterpartySignature}.
 */
class CounterpartySignatureTest {

  private static final PublicKey PUBLIC_KEY1 = PublicKey.fromBase16EncodedPublicKey(
    "EDE57F52F5C23803AC0FBADBB92917EF21BA0874145D7126CDDE56A2938A39C15F"
  );
  private static final Address SIGNER1_ADDRESS = PUBLIC_KEY1.deriveAddress();

  private static final PublicKey PUBLIC_KEY2 = PublicKey.fromBase16EncodedPublicKey(
    "ED17036166A5354D564A86F5D1E43A08C350F6B0A817249158B6F8F23DA861A940"
  );
  private static final Address SIGNER2_ADDRESS = PUBLIC_KEY2.deriveAddress();

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

  // /////////////////
  // of(PublicKey, Signature) factory
  // /////////////////

  @Test
  void testOfSingleSign() {
    CounterpartySignature cs = CounterpartySignature.of(PUBLIC_KEY1, SIGNATURE1);

    assertThat(cs.signingPublicKey()).isPresent().contains(PUBLIC_KEY1);
    assertThat(cs.transactionSignature()).isPresent().contains(SIGNATURE1);
    assertThat(cs.signers()).isEmpty();
    assertThat(cs.sortedSigners()).isFalse();
  }

  // /////////////////
  // of(Set<Signer>) factory
  // /////////////////

  @Test
  void testOfMultiSign() {
    Signer signer1 = Signer.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    Signer signer2 = Signer.builder()
      .signingPublicKey(PUBLIC_KEY2)
      .transactionSignature(SIGNATURE2)
      .build();

    CounterpartySignature cs = CounterpartySignature.of(Sets.newHashSet(signer1, signer2));

    assertThat(cs.signingPublicKey()).isEmpty();
    assertThat(cs.transactionSignature()).isEmpty();
    assertThat(cs.signers()).hasSize(2);
    assertThat(cs.sortedSigners()).isTrue();
  }

  // /////////////////
  // Builder with direct signing
  // /////////////////

  @Test
  void testBuilderWithDirectSigning() {
    CounterpartySignature cs = CounterpartySignature.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    assertThat(cs.signingPublicKey()).isPresent().contains(PUBLIC_KEY1);
    assertThat(cs.transactionSignature()).isPresent().contains(SIGNATURE1);
    assertThat(cs.signers()).isEmpty();
    assertThat(cs.sortedSigners()).isFalse();
  }

  // /////////////////
  // Builder with multi-sig
  // /////////////////

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

    CounterpartySignature cs = CounterpartySignature.builder()
      .signers(Arrays.asList(
        SignerWrapper.of(signer1),
        SignerWrapper.of(signer2)
      ))
      .build();

    assertThat(cs.signingPublicKey()).isEmpty();
    assertThat(cs.transactionSignature()).isEmpty();
    assertThat(cs.signers()).hasSize(2);
    assertThat(cs.sortedSigners()).isTrue();
  }

  // /////////////////
  // Validation
  // /////////////////

  @Test
  void testValidationFailsWithNoSigningFields() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      CounterpartySignature.builder().build()
    );

    assertThat(exception.getMessage())
      .contains("CounterpartySignature must have either (SigningPubKey and TxnSignature) or non-empty Signers array");
  }

  @Test
  void testValidationFailsWithBothDirectSigningAndMultiSig() {
    Signer signer = Signer.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      CounterpartySignature.builder()
        .signingPublicKey(PUBLIC_KEY1)
        .transactionSignature(SIGNATURE1)
        .addSigners(SignerWrapper.of(signer))
        .build()
    );

    assertThat(exception.getMessage())
      .contains("CounterpartySignature cannot have both direct signing fields and Signers array");
  }

  @Test
  void testValidationFailsWithOnlySigningPubKey() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      CounterpartySignature.builder()
        .signingPublicKey(PUBLIC_KEY1)
        .build()
    );

    assertThat(exception.getMessage())
      .contains("CounterpartySignature must have both SigningPubKey and TxnSignature, or neither");
  }

  @Test
  void testValidationFailsWithOnlyTxnSignature() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      CounterpartySignature.builder()
        .transactionSignature(SIGNATURE1)
        .build()
    );

    assertThat(exception.getMessage())
      .contains("CounterpartySignature must have both SigningPubKey and TxnSignature, or neither");
  }

  @Test
  void testValidationFailsWithSigningPubKeyAndSigners() {
    Signer signer = Signer.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      CounterpartySignature.builder()
        .signingPublicKey(PUBLIC_KEY1)
        .addSigners(SignerWrapper.of(signer))
        .build()
    );

    assertThat(exception.getMessage())
      .contains("CounterpartySignature must have both SigningPubKey and TxnSignature, or neither");
  }

  // /////////////////
  // Signer sorting
  // /////////////////

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
    CounterpartySignature cs = CounterpartySignature.builder()
      .signers(Arrays.asList(
        SignerWrapper.of(signer2),  // r9LqNeG6qHxjeUocjvVki2XR35weJ9mZgQ
        SignerWrapper.of(signer3),  // r9zRhGr7b6xPekLvT6wP4qNdWMryaumZS7
        SignerWrapper.of(signer1),  // rs5zPPKr7pgkvu4xFBdum1FcKHJeSRL7JP
        SignerWrapper.of(signer5),  // rDTXLQ7ZKZVKz33zJbHjgVShjsBnqMBhmN
        SignerWrapper.of(signer4)   // rUJBVHUDm6FXWseGj5oA6iv1enUzDGtMYo
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
    assertThat(cs.signers()).hasSize(5);
    assertThat(cs.signers().get(0).signer().account()).isEqualTo(signer1.account());
    assertThat(cs.signers().get(1).signer().account()).isEqualTo(signer2.account());
    assertThat(cs.signers().get(2).signer().account()).isEqualTo(signer3.account());
    assertThat(cs.signers().get(3).signer().account()).isEqualTo(signer4.account());
    assertThat(cs.signers().get(4).signer().account()).isEqualTo(signer5.account());
    assertThat(cs.sortedSigners()).isTrue();
  }

  // /////////////////
  // JSON serialization
  // /////////////////

  @Test
  void testJsonSerializationWithDirectSigning() throws JsonProcessingException, JSONException {
    CounterpartySignature cs = CounterpartySignature.builder()
      .signingPublicKey(PUBLIC_KEY1)
      .transactionSignature(SIGNATURE1)
      .build();

    String expectedJson = String.format(
      "{\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}",
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value()
    );

    assertSerializesAndDeserializes(cs, expectedJson);
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

    CounterpartySignature cs = CounterpartySignature.builder()
      .signers(Arrays.asList(
        SignerWrapper.of(signer1),
        SignerWrapper.of(signer2)
      ))
      .build();

    String expectedJson = String.format(
      "{\"Signers\":[" +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}," +
        "{\"Signer\":{\"Account\":\"%s\"," +
        "\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}}" +
        "]}",
      SIGNER1_ADDRESS.value(),
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value(),
      SIGNER2_ADDRESS.value(),
      PUBLIC_KEY2.base16Value(),
      SIGNATURE2.base16Value()
    );

    assertSerializesAndDeserializes(cs, expectedJson);
  }

  @Test
  void testJsonDeserializeAndSerialize() throws JsonProcessingException {
    String json = String.format(
      "{\"SigningPubKey\":\"%s\"," +
        "\"TxnSignature\":\"%s\"}",
      PUBLIC_KEY1.base16Value(),
      SIGNATURE1.base16Value()
    );

    CounterpartySignature deserialized = objectMapper.readValue(json, CounterpartySignature.class);

    assertThat(deserialized.signingPublicKey()).isPresent().contains(PUBLIC_KEY1);
    assertThat(deserialized.transactionSignature()).isPresent().contains(SIGNATURE1);
    assertThat(deserialized.signers()).isEmpty();

    String serialized = objectMapper.writeValueAsString(deserialized);
    JsonAssert.with(serialized).assertNotNull("$.SigningPubKey");
    JsonAssert.with(serialized).assertNotNull("$.TxnSignature");

    CounterpartySignature roundTrip = objectMapper.readValue(serialized, CounterpartySignature.class);
    assertThat(roundTrip).isEqualTo(deserialized);
  }

  private void assertSerializesAndDeserializes(
    CounterpartySignature counterpartySignature,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(counterpartySignature);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    CounterpartySignature deserialized = objectMapper.readValue(serialized, CounterpartySignature.class);
    assertThat(deserialized).isEqualTo(counterpartySignature);
  }
}
