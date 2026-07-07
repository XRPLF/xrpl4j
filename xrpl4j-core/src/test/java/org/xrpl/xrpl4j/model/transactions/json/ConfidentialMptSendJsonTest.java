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
import org.xrpl.xrpl4j.model.transactions.Commitment;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.EncryptedAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.ZkProof;

/**
 * JSON serialization/deserialization tests for {@link ConfidentialMptSend}.
 */
public class ConfidentialMptSendJsonTest extends AbstractJsonTest {

  // Valid 66-byte (132 hex character) ElGamal ciphertexts, shared by both tests.
  private static final EncryptedAmount SENDER_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("AB", 66));
  private static final EncryptedAmount DESTINATION_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("CD", 66));
  private static final EncryptedAmount ISSUER_ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("EF", 66));
  // A valid 946-byte (1892 hex character) send ZKProof, shared by both tests.
  private static final ZkProof ZK_PROOF = ZkProof.of(Strings.repeat("34", 946));
  // Valid 33-byte (66 hex character) Pedersen commitments, shared by both tests.
  private static final Commitment AMOUNT_COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final Commitment BALANCE_COMMITMENT = Commitment.of(Strings.repeat("03", 33));

  @Test
  public void testJsonWithAllFields() throws JSONException, JsonProcessingException {
    ConfidentialMptSend send = ConfidentialMptSend.builder()
      .account(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .destination(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .senderEncryptedAmount(SENDER_ENCRYPTED_AMOUNT)
      .destinationEncryptedAmount(DESTINATION_ENCRYPTED_AMOUNT)
      .issuerEncryptedAmount(ISSUER_ENCRYPTED_AMOUNT)
      .auditorEncryptedAmount(EncryptedAmount.of(Strings.repeat("11", 66)))
      .zkProof(ZK_PROOF)
      .amountCommitment(AMOUNT_COMMITMENT)
      .balanceCommitment(BALANCE_COMMITMENT)
      .addCredentialIds(Hash256.of(Strings.repeat("AB", 32)))
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTSend\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\",\n" +
      "  \"Destination\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "  \"DestinationTag\" : 13,\n" +
      "  \"SenderEncryptedAmount\" : \"" + SENDER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"DestinationEncryptedAmount\" : \"" + DESTINATION_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"AuditorEncryptedAmount\" : \"" + Strings.repeat("11", 66) + "\",\n" +
      "  \"ZKProof\" : \"" + ZK_PROOF.value() + "\",\n" +
      "  \"AmountCommitment\" : \"" + AMOUNT_COMMITMENT.value() + "\",\n" +
      "  \"BalanceCommitment\" : \"" + BALANCE_COMMITMENT.value() + "\",\n" +
      "  \"CredentialIDs\" : [ \"" + Strings.repeat("AB", 32) + "\" ]\n" +
      "}";

    assertCanSerializeAndDeserialize(send, json);
  }

  @Test
  public void testJsonWithRequiredFieldsOnly() throws JSONException, JsonProcessingException {
    ConfidentialMptSend send = ConfidentialMptSend.builder()
      .account(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .destination(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .senderEncryptedAmount(SENDER_ENCRYPTED_AMOUNT)
      .destinationEncryptedAmount(DESTINATION_ENCRYPTED_AMOUNT)
      .issuerEncryptedAmount(ISSUER_ENCRYPTED_AMOUNT)
      .zkProof(ZK_PROOF)
      .amountCommitment(AMOUNT_COMMITMENT)
      .balanceCommitment(BALANCE_COMMITMENT)
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTSend\",\n" +
      "  \"Fee\" : \"135\",\n" +
      "  \"Sequence\" : 377,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\",\n" +
      "  \"Destination\" : \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "  \"SenderEncryptedAmount\" : \"" + SENDER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"DestinationEncryptedAmount\" : \"" + DESTINATION_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT.value() + "\",\n" +
      "  \"ZKProof\" : \"" + ZK_PROOF.value() + "\",\n" +
      "  \"AmountCommitment\" : \"" + AMOUNT_COMMITMENT.value() + "\",\n" +
      "  \"BalanceCommitment\" : \"" + BALANCE_COMMITMENT.value() + "\"\n" +
      "}";

    assertCanSerializeAndDeserialize(send, json);
  }
}
