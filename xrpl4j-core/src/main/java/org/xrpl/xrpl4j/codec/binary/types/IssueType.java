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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;
import org.xrpl.xrpl4j.model.AddressConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

public class IssueType extends SerializedType<IssueType> {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
  private static final int MPT_WIDTH = 44; // 20 (issuer account) + 20 (no account marker) + 4 (sequence)
  private static final AccountIdType NO_ACCOUNT =
    new AccountIdType(UnsignedByteArray.fromHex("0000000000000000000000000000000000000001"));

  private static final UnsignedByteArray ACCOUNT_ONE = AddressCodec.getInstance().decodeAccountId(
    AddressConstants.ACCOUNT_ONE);

  private static final Pattern MPT_ISSUANCE_ID_HEX_PATTERN = Pattern.compile("^[0-9A-Fa-f]{48}$");

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

    if (node.has("mpt_issuance_id")) {
      // mpt_issuance_id (192-bit integer): sequence (4 bytes big-endian) || issuer (20 bytes) = 24 bytes
      // Binary encoded representation: issuer (20 bytes) || ACCOUNT_ONE (20 bytes) || sequence (4 bytes) = 44 bytes
      String mptIssuanceId = node.get("mpt_issuance_id").asText();

      if (!MPT_ISSUANCE_ID_HEX_PATTERN.matcher(mptIssuanceId).matches()) {
        throw new IllegalArgumentException(
          "mpt_issuance_id must be a 48-character hexadecimal string, but was: " + mptIssuanceId
        );
      }

      UnsignedByteArray mptIssuanceIdBytes = UnsignedByteArray.fromHex(mptIssuanceId);

      UnsignedByteArray sequenceBE = mptIssuanceIdBytes.slice(0, 4);
      UnsignedByteArray issuerBytes = mptIssuanceIdBytes.slice(4, 24);

      // Convert sequence from big-endian to little-endian
      UnsignedByteArray sequenceLE = sequenceBE.reverse();

      UnsignedByteArray byteArray = issuerBytes.append(ACCOUNT_ONE).append(sequenceLE);
      return new IssueType(byteArray);
    }

    UnsignedByteArray byteArray = new CurrencyType().fromJson(node.get("currency")).value();
    if (node.has("issuer")) {
      byteArray.append(new AccountIdType().fromJson(node.get("issuer")).value());
    }

    return new IssueType(byteArray);
  }

  @Override
  public IssueType fromParser(BinaryParser parser) {
    CurrencyType currencyOrIssuer = new CurrencyType().fromParser(parser);
    if (currencyOrIssuer.toJson().asText().equals("XRP")) {
      return new IssueType(currencyOrIssuer.value());
    }

    AccountIdType accountId = new AccountIdType().fromParser(parser);

    if (accountId.value().equals(ACCOUNT_ONE)) {
      // MPT: issuer (20 bytes) + ACCOUNT_ONE (20 bytes) + sequence (4 bytes) = 44 bytes total
      UnsignedByteArray sequenceBytes = parser.read(4);
      return new IssueType(currencyOrIssuer.value().append(accountId.value()).append(sequenceBytes));
    }

    // IOU: currency + issuer
    return new IssueType(currencyOrIssuer.value().append(accountId.value()));
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

      ObjectNode result = objectMapper.createObjectNode();
      result.put("mpt_issuance_id", mptIssuanceId.hexValue());
      return result;
    }

    // Handle XRP or IOU issue
    BinaryParser parser = new BinaryParser(this.toHex());
    CurrencyType currency = new CurrencyType().fromParser(parser);
    JsonNode currencyJson = currency.toJson();

    // XRP
    if (currencyJson.asText().equals("XRP")) {
      ObjectNode result = objectMapper.createObjectNode();
      result.set("currency", currencyJson);
      return result;
    }

    AccountIdType accountId = new AccountIdType().fromParser(parser);

    // MPT
    if (accountId.value().equals(ACCOUNT_ONE)) {
      // Convert sequence from little-endian to big-endian
      UnsignedByteArray sequenceLE = parser.read(4);
      UnsignedByteArray sequenceBE = sequenceLE.reverse();

      // Currency bytes hold the issuer when it's an MPT
      String mptIssuanceIdHex = sequenceBE.append(currency.value()).hexValue();
      ObjectNode result = objectMapper.createObjectNode();
      result.put("mpt_issuance_id", mptIssuanceIdHex);
      return result;
    }

    // IOU
    ObjectNode result = objectMapper.createObjectNode();
    result.set("currency", currencyJson);
    result.set("issuer", accountId.toJson());
    return result;
  }
}
