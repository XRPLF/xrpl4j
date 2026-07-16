package org.xrpl.xrpl4j.model.transactions.json;

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
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * JSON serialization/deserialization tests for {@link ConfidentialMptConvert}.
 */
public class ConfidentialMptConvertJsonTest extends AbstractJsonTest {

  // Valid 66-byte (132 hex character) ElGamal ciphertexts, shared by both tests.
  private static final EncryptedAmount HOLDER_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("AB", 66));
  private static final EncryptedAmount ISSUER_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("CD", 66));
  // A valid 32-byte (64 hex character) blinding factor, shared by both tests.
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("12", 32));

  @Test
  public void testJsonWithEncryptionKeyAndProof() throws JSONException, JsonProcessingException {
    ConfidentialMptConvert convert = ConfidentialMptConvert.builder()
      .account(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .mptAmount(MpTokenNumericAmount.of(1000))
      // A valid 33-byte (66 hex character) compressed EC public key.
      .holderEncryptionKey(
        PublicKey.fromBase16EncodedPublicKey("028D7500BFCD792B487E4E51664037AB543E76CEBACF0E7E17AD4B83057E1F2B30")
      )
      .holderEncryptedAmount(HOLDER_ENCRYPTED_AMOUNT)
      .issuerEncryptedAmount(ISSUER_ENCRYPTED_AMOUNT)
      // A valid 66-byte (132 hex character) auditor ciphertext.
      .auditorEncryptedAmount(EncryptedAmount.of(Strings.repeat("EF", 66)))
      .blindingFactor(BLINDING_FACTOR)
      // A valid 64-byte (128 hex character) Schnorr proof of knowledge.
      .zkProof(ConfidentialMptConvertProof.fromHex(Strings.repeat("34", 64)))
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTConvert\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\",\n" +
      "  \"MPTAmount\" : \"1000\",\n" +
      "  \"HolderEncryptionKey\" : \"028D7500BFCD792B487E4E51664037AB543E76CEBACF0E7E17AD4B83057E1F2B30\",\n" +
      "  \"HolderEncryptedAmount\" : \"" + HOLDER_ENCRYPTED_AMOUNT.hexValue() + "\",\n" +
      "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT.hexValue() + "\",\n" +
      "  \"AuditorEncryptedAmount\" : \"" + Strings.repeat("EF", 66) + "\",\n" +
      "  \"BlindingFactor\" : \"" + BLINDING_FACTOR.hexValue() + "\",\n" +
      "  \"ZKProof\" : \"" + Strings.repeat("34", 64) + "\"\n" +
      "}";

    assertCanSerializeAndDeserialize(convert, json);
  }

  @Test
  public void testJsonWithoutEncryptionKeyAndProof() throws JSONException, JsonProcessingException {
    ConfidentialMptConvert convert = ConfidentialMptConvert.builder()
      .account(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .mptAmount(MpTokenNumericAmount.of(1000))
      .holderEncryptedAmount(HOLDER_ENCRYPTED_AMOUNT)
      .issuerEncryptedAmount(ISSUER_ENCRYPTED_AMOUNT)
      .blindingFactor(BLINDING_FACTOR)
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTConvert\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\",\n" +
      "  \"MPTAmount\" : \"1000\",\n" +
      "  \"HolderEncryptedAmount\" : \"" + HOLDER_ENCRYPTED_AMOUNT.hexValue() + "\",\n" +
      "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT.hexValue() + "\",\n" +
      "  \"BlindingFactor\" : \"" + BLINDING_FACTOR.hexValue() + "\"\n" +
      "}";

    assertCanSerializeAndDeserialize(convert, json);
  }
}
