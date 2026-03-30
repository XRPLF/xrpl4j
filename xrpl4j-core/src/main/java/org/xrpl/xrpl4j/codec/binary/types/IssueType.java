package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IssueType extends SerializedType<IssueType> {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
  private static final int MPT_WIDTH = 44; // 20 (issuer account) + 20 (no account marker) + 4 (sequence)
  private static final AccountIdType NO_ACCOUNT =
    new AccountIdType(UnsignedByteArray.fromHex("0000000000000000000000000000000000000001"));

  public IssueType() {
    this(UnsignedByteArray.ofSize(20));
  }

  public IssueType(UnsignedByteArray bytes) {
    super(bytes);
  }

  @Override
  public IssueType fromJson(JsonNode node) throws JsonProcessingException {
    if (!node.isObject()) {
      throw new IllegalArgumentException("node is not an object");
    }

    Issue issue = objectMapper.treeToValue(node, Issue.class);

    // Handle MPT issue
    if (issue.mptIssuanceId().isPresent()) {
      String mptIssuanceId = issue.mptIssuanceId().get().asText();
      if (mptIssuanceId.length() != 48) {
        throw new IllegalArgumentException("Invalid mpt_issuance_id length: expected 48 characters, received " +
          mptIssuanceId.length() + " characters.");
      }

      // MPT issuance ID in JSON: 4 bytes sequence + 20 bytes issuer account
      // MPT issuance ID in binary: 20 bytes issuer account + 20 bytes no-account marker + 4 bytes sequence
      // Note: The 4-byte sequence uses different byte order in JSON vs binary representations
      byte[] mptBytes = UnsignedByteArray.fromHex(mptIssuanceId).toByteArray();
      byte[] sequenceFromJson = new byte[4];
      byte[] issuerAccount = new byte[20];

      System.arraycopy(mptBytes, 0, sequenceFromJson, 0, 4);
      System.arraycopy(mptBytes, 4, issuerAccount, 0, 20);

      // Convert sequence from JSON byte order to binary byte order
      ByteBuffer jsonBuffer = ByteBuffer.wrap(sequenceFromJson);
      jsonBuffer.order(ByteOrder.BIG_ENDIAN);
      int sequence = jsonBuffer.getInt();

      byte[] sequenceForBinary = new byte[4];
      ByteBuffer binaryBuffer = ByteBuffer.wrap(sequenceForBinary);
      binaryBuffer.order(ByteOrder.LITTLE_ENDIAN);
      binaryBuffer.putInt(sequence);

      UnsignedByteArray result = UnsignedByteArray.of(issuerAccount)
        .append(NO_ACCOUNT.value())
        .append(UnsignedByteArray.of(sequenceForBinary));

      return new IssueType(result);
    }

    // Handle XRP or IOU issue
    UnsignedByteArray byteArray = new CurrencyType().fromJson(issue.currency().get()).value();
    issue.issuer().ifPresent(
      issuer -> byteArray.append(new AccountIdType().fromJson(issuer).value())
    );

    return new IssueType(byteArray);
  }

  @Override
  public IssueType fromParser(BinaryParser parser) {
    // Read first 20 bytes (currency or issuer account for MPT)
    UnsignedByteArray currencyOrAccount = parser.read(20);
    CurrencyType currency = new CurrencyType(currencyOrAccount);

    // Check if XRP
    if (currency.toJson().asText().equals("XRP")) {
      return new IssueType(currencyOrAccount);
    }

    // Read next 20 bytes (issuer account)
    AccountIdType issuerAccountId = new AccountIdType().fromParser(parser);

    // Check if this is an MPT (issuer is the no-account marker)
    if (NO_ACCOUNT.toHex().equals(issuerAccountId.toHex())) {
      // Read 4-byte sequence number
      UnsignedByteArray sequence = parser.read(4);
      return new IssueType(currencyOrAccount.append(issuerAccountId.value()).append(sequence));
    }

    // IOU case
    return new IssueType(currencyOrAccount.append(issuerAccountId.value()));
  }

  @Override
  public JsonNode toJson() {
    // Check if this is an MPT issue (44 bytes)
    if (this.value().length() == MPT_WIDTH) {
      byte[] bytes = this.value().toByteArray();
      byte[] issuerAccount = new byte[20];
      byte[] sequenceFromBinary = new byte[4];

      System.arraycopy(bytes, 0, issuerAccount, 0, 20);
      System.arraycopy(bytes, 40, sequenceFromBinary, 0, 4);

      // Verify no-account marker
      byte[] noAccount = new byte[20];
      System.arraycopy(bytes, 20, noAccount, 0, 20);
      if (!NO_ACCOUNT.toHex().equals(UnsignedByteArray.of(noAccount).hexValue())) {
        throw new IllegalStateException("Invalid MPT Issue encoding: no-account marker mismatch.");
      }

      // Convert sequence from binary byte order to JSON byte order
      ByteBuffer binaryBuffer = ByteBuffer.wrap(sequenceFromBinary);
      binaryBuffer.order(ByteOrder.LITTLE_ENDIAN);
      int sequence = binaryBuffer.getInt();

      byte[] sequenceForJson = new byte[4];
      ByteBuffer jsonBuffer = ByteBuffer.wrap(sequenceForJson);
      jsonBuffer.order(ByteOrder.BIG_ENDIAN);
      jsonBuffer.putInt(sequence);

      // Construct mpt_issuance_id for JSON: sequence + issuer account
      UnsignedByteArray mptIssuanceId = UnsignedByteArray.of(sequenceForJson)
        .append(UnsignedByteArray.of(issuerAccount));

      ImmutableIssue.Builder builder = Issue.builder();
      builder.mptIssuanceId(objectMapper.valueToTree(mptIssuanceId.hexValue()));
      return objectMapper.valueToTree(builder.build());
    }

    // Handle XRP or IOU issue
    BinaryParser parser = new BinaryParser(this.toHex());
    JsonNode currency = new CurrencyType().fromParser(parser).toJson();

    ImmutableIssue.Builder builder = Issue.builder();
    builder.currency(currency);
    if (!currency.asText().equals("XRP")) {
      JsonNode issuer = new AccountIdType().fromParser(parser).toJson();
      builder.issuer(issuer);
    }

    return objectMapper.valueToTree(builder.build());
  }
}
