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
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.BlindingFactor;
import org.xrpl.xrpl4j.model.transactions.Commitment;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.EncryptedAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.ZkProof;

/**
 * JSON serialization/deserialization tests for {@link ConfidentialMptConvertBack}.
 */
public class ConfidentialMptConvertBackJsonTest extends AbstractJsonTest {

  // Valid 66-byte (132 hex character) ElGamal ciphertexts, shared by both tests.
  private static final EncryptedAmount HOLDER_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("AB", 66));
  private static final EncryptedAmount ISSUER_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("CD", 66));
  // A valid 32-byte (64 hex character) blinding factor, shared by both tests.
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("EF", 32));
  // A valid 33-byte (66 hex character) Pedersen commitment, shared by both tests.
  private static final Commitment BALANCE_COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  // A valid 816-byte (1632 hex character) ConvertBack ZKProof, shared by both tests.
  private static final ZkProof ZK_PROOF = ZkProof.of(Strings.repeat("34", 816));

  @Test
  public void testJsonWithAllFields() throws JSONException, JsonProcessingException {
    EncryptedAmount auditorEncryptedAmount = EncryptedAmount.of(Strings.repeat("11", 66));
    ConfidentialMptConvertBack convertBack = ConfidentialMptConvertBack.builder()
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
      .balanceCommitment(BALANCE_COMMITMENT)
      .zkProof(ZK_PROOF)
      .auditorEncryptedAmount(auditorEncryptedAmount)
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTConvertBack\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\",\n" +
      "  \"MPTAmount\" : \"1000\",\n" +
      "  \"HolderEncryptedAmount\" : \"" + HOLDER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"BlindingFactor\" : \"" + BLINDING_FACTOR.value() + "\",\n" +
      "  \"BalanceCommitment\" : \"" + BALANCE_COMMITMENT.value() + "\",\n" +
      "  \"ZKProof\" : \"" + ZK_PROOF.value() + "\",\n" +
      "  \"AuditorEncryptedAmount\" : \"" + auditorEncryptedAmount.value() + "\"\n" +
      "}";

    assertCanSerializeAndDeserialize(convertBack, json);
  }

  @Test
  public void testJsonWithRequiredFieldsOnly() throws JSONException, JsonProcessingException {
    ConfidentialMptConvertBack convertBack = ConfidentialMptConvertBack.builder()
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
      .balanceCommitment(BALANCE_COMMITMENT)
      .zkProof(ZK_PROOF)
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTConvertBack\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\",\n" +
      "  \"MPTAmount\" : \"1000\",\n" +
      "  \"HolderEncryptedAmount\" : \"" + HOLDER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"BlindingFactor\" : \"" + BLINDING_FACTOR.value() + "\",\n" +
      "  \"BalanceCommitment\" : \"" + BALANCE_COMMITMENT.value() + "\",\n" +
      "  \"ZKProof\" : \"" + ZK_PROOF.value() + "\"\n" +
      "}";

    assertCanSerializeAndDeserialize(convertBack, json);
  }
}
