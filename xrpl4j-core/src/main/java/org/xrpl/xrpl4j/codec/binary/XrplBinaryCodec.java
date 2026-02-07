package org.xrpl.xrpl4j.codec.binary;

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
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.definitions.DefinitionsService;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInstance;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;
import org.xrpl.xrpl4j.codec.binary.types.AccountIdType;
import org.xrpl.xrpl4j.codec.binary.types.STObjectType;
import org.xrpl.xrpl4j.codec.binary.types.UInt64Type;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class XrplBinaryCodec {

  public static final String TRX_SIGNATURE_PREFIX = "53545800";

  public static final String TRX_MULTI_SIGNATURE_PREFIX = "534D5400";

  public static final String PAYMENT_CHANNEL_CLAIM_SIGNATURE_PREFIX = "434C4D00";
  public static final String BATCH_SIGNATURE_PREFIX = "42434800"; // "BCH\0" per XLS-0056

  public static final String CHANNEL_FIELD_NAME = "Channel";
  public static final String AMOUNT_FIELD_NAME = "Amount";

  private static final DefinitionsService DEFINITIONS_SERVICE = DefinitionsService.getInstance();
  private static final ObjectMapper BINARY_CODEC_OBJECT_MAPPER = BinaryCodecObjectMapperFactory.getObjectMapper();

  private static final XrplBinaryCodec INSTANCE = new XrplBinaryCodec();

  /**
   * Get a singleton instance of {@link XrplBinaryCodec}.
   *
   * @return A singleton instance of {@link XrplBinaryCodec}.
   */
  public static XrplBinaryCodec getInstance() {
    return INSTANCE;
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string.
   *
   * @param json A {@link String} containing JSON to be encoded.
   *
   * @return A {@link String} containing the hex-encoded representation of {@code json}.
   *
   * @throws JsonProcessingException if {@code json} is not valid JSON.
   */
  public String encode(String json) throws JsonProcessingException {
    Objects.requireNonNull(json);
    JsonNode node = BINARY_CODEC_OBJECT_MAPPER.readTree(json);
    return encode(node);
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string.
   *
   * @param jsonNode A {@link JsonNode} containing JSON to be encoded.
   *
   * @return A {@link String} containing the hex-encoded representation of {@code jsonNode}.
   */
  private String encode(final JsonNode jsonNode) {
    Objects.requireNonNull(jsonNode);
    UnsignedByteArray byteList = UnsignedByteArray.empty();
    new STObjectType().fromJson(jsonNode).toBytesSink(byteList);
    return byteList.hexValue();
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string.
   *
   * @param json String containing JSON to be encoded.
   *
   * @return hex encoded representations
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encodeForSigning(String json) throws JsonProcessingException {
    JsonNode node = BINARY_CODEC_OBJECT_MAPPER.readTree(json);
    return TRX_SIGNATURE_PREFIX + encode(removeNonSigningFields(node));
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for signing purposes.
   *
   * @param json         A {@link String} containing JSON to be encoded.
   * @param xrpAccountId A {@link String} containing the XRPL AccountId.
   *
   * @return hex encoded representations
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encodeForMultiSigning(String json, String xrpAccountId) throws JsonProcessingException {
    JsonNode node = BINARY_CODEC_OBJECT_MAPPER.readTree(json);
    if (!node.isObject()) {
      throw new IllegalArgumentException("JSON object required for signing");
    }
    // any existing signing keys should not also be signed
    ((ObjectNode) node).set("SigningPubKey", new TextNode(""));
    String suffix = new AccountIdType().fromJson(new TextNode(xrpAccountId)).toHex();
    return TRX_MULTI_SIGNATURE_PREFIX + encode(removeNonSigningFields(node)) + suffix;
  }

  /**
   * Encodes a {@link Batch} transaction to canonical XRPL binary as a hex string for signing purposes. Note that this
   * function slightly diverges from the pattern of the other encodeForSigning functions because the bytes to be signed
   * for a Batch transaction are not simply the canonical binary representation of the JSON. Instead, we have distinct
   * portions of the Batch transaction that are signed. Also, unlike {@link #encodeForSigning(String)}, which accepts
   * JSON and then checks for JsonNode values, this implementation instead accepts a well-typed Java object and operates
   * on that, for safety and correctness.
   *
   * @param batch A {@link Batch} containing JSON to be encoded.
   *
   * @return hex encoded representations
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public UnsignedByteArray encodeForBatchInnerSigning(Batch batch) throws JsonProcessingException {
    Objects.requireNonNull(batch);
    try {
      // Start with batch prefix (0x42434800 = "BCH\0")
      UnsignedByteArray signableBytes = UnsignedByteArray.fromHex(XrplBinaryCodec.BATCH_SIGNATURE_PREFIX);

      // Add flags (4 bytes, big-endian)
      HashingUtils.addUInt32(signableBytes, (int) batch.flags().getValue());

      // Add count of inner transactions (4 bytes, big-endian)
      HashingUtils.addUInt32(signableBytes, batch.rawTransactions().size());

      // Add each inner transaction ID (32 bytes each)
      for (RawTransactionWrapper wrapper : batch.rawTransactions()) {
        final UnsignedByteArray transactionId = computeInnerBatchTransactionId(wrapper);
        signableBytes.append(transactionId);
      }

      return signableBytes;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Encode a {@link Batch} for multi-signing by a specific signer. This is used when a multi-sig account acts as a
   * BatchSigner with nested Signers. Per rippled's checkBatchMultiSign, this uses batch serialization (serializeBatch)
   * followed by appending the signer's account ID (finishMultiSigningData).
   *
   * @param batch         The {@link Batch} to encode.
   * @param signerAddress The address of the signer (will be appended as account ID suffix).
   *
   * @return An {@link UnsignedByteArray} containing the batch serialization with account ID suffix.
   *
   * @throws JsonProcessingException if there is an error processing the JSON.
   */
  public UnsignedByteArray encodeForBatchInnerMultiSigning(Batch batch, Address signerAddress)
    throws JsonProcessingException {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(signerAddress);

    // Start with batch serialization (HashPrefix::batch + flags + count + tx IDs)
    UnsignedByteArray batchBytes = encodeForBatchInnerSigning(batch);

    // Create a copy to avoid mutating the original (since UnsignedByteArray.append() mutates)
    UnsignedByteArray result = UnsignedByteArray.of(batchBytes.toByteArray());

    // Append the signer's account ID (like finishMultiSigningData does in rippled)
    String accountIdHex = new AccountIdType().fromJson(new TextNode(signerAddress.value())).toHex();
    result.append(UnsignedByteArray.fromHex(accountIdHex));

    return result;
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for signing payment channel claims. The only JSON fields
   * which will be encoded are "Channel" and "Amount".
   *
   * @param json String containing JSON to be encoded.
   *
   * @return The binary encoded JSON in hexadecimal form.
   *
   * @throws JsonProcessingException If the JSON is not valid.
   */
  public String encodeForSigningClaim(String json) throws JsonProcessingException {
    JsonNode node = BINARY_CODEC_OBJECT_MAPPER.readTree(json);
    if (!node.isObject()) {
      throw new IllegalArgumentException("JSON object required for signing");
    }

    if (!node.has("Channel") || !node.has("Amount")) {
      throw new IllegalArgumentException("Unsigned claims must have Channel and Amount fields.");
    }
    UnsignedByteArray channel = UnsignedByteArray.fromHex(node.get(CHANNEL_FIELD_NAME).asText());
    UnsignedByteArray amount = UnsignedByteArray.of(
      new UInt64Type(UnsignedLong.valueOf(node.get(AMOUNT_FIELD_NAME).asText())).toBytes()
    );

    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    byteArray.append(channel);
    byteArray.append(amount);
    return PAYMENT_CHANNEL_CLAIM_SIGNATURE_PREFIX + byteArray.hexValue();
  }

  /**
   * Decodes canonical XRPL binary hex encoded transaction string to JSON.
   *
   * @param encodedTransaction A {@link String} value, in hex, to decode to JSON.
   *
   * @return A JSON {@link String} representing the decoded encodedTransaction.
   */
  public String decode(String encodedTransaction) {
    final String nonSignPrefixHex;
    if (encodedTransaction.startsWith(TRX_SIGNATURE_PREFIX)) {
      nonSignPrefixHex = encodedTransaction.substring(TRX_SIGNATURE_PREFIX.length());
    } else if (encodedTransaction.startsWith(TRX_MULTI_SIGNATURE_PREFIX)) {
      // The suffix is always a Hash160, which is 160 bits/20 bytes, which is 40 HEX chars.
      final int suffixLength = 40;
      nonSignPrefixHex = encodedTransaction.substring(
        TRX_MULTI_SIGNATURE_PREFIX.length(), encodedTransaction.length() - suffixLength
      );
    } else {
      nonSignPrefixHex = encodedTransaction;
    }
    return new BinaryParser(nonSignPrefixHex).readType(STObjectType.class)
      .toJson()
      .toString();
  }

  /**
   * Creates a deep copy of the given node, removing any fields that are not flagged as signing fields according to the
   * definition metadata.
   *
   * @param node A {@link JsonNode} to remove signing fields from.
   *
   * @return A {@link JsonNode} containing a deep clone of {@code node}, with non-signing fields omitted.
   */
  private JsonNode removeNonSigningFields(JsonNode node) {
    if (!node.isObject()) {
      // signing field filtering is based on JSON field names
      // since JSON objects contain field names, no further processing has to be
      // done on non-objects like arrays or simple values.
      return node;
    }

    Map<String, JsonNode> signingFields = Lists.newArrayList(node.fieldNames())
      .stream()
      .filter(this::isSigningField)
      .collect(Collectors.toMap(Function.identity(), node::get));

    return new ObjectNode(BINARY_CODEC_OBJECT_MAPPER.getNodeFactory(), signingFields);
  }

  private Boolean isSigningField(String fieldName) {
    return DEFINITIONS_SERVICE.getFieldInstance(fieldName).map(FieldInstance::isSigningField).orElse(false);
  }

  /**
   * Computes the transaction ID for an unsigned inner transaction.
   *
   * @param rawTransactionWrapper A {@link RawTransactionWrapper} with an unsigned inner transaction, used for Batch.
   *
   * @return A {@link Hash256} containing the transaction's transaction ID.
   */
  private UnsignedByteArray computeInnerBatchTransactionId(final RawTransactionWrapper rawTransactionWrapper)
    throws JsonProcessingException {
    Objects.requireNonNull(rawTransactionWrapper);

    String txJson = BINARY_CODEC_OBJECT_MAPPER.writeValueAsString(rawTransactionWrapper.rawTransaction());
    // Parse the JSON and ensure SigningPubKey is set to empty string for inner transactions
    JsonNode txNode = removeNonSigningFields(BINARY_CODEC_OBJECT_MAPPER.readTree(txJson));
    if (txNode.isObject()) {
      final ObjectNode objNode = (ObjectNode) txNode;
      // Fix Flags field if it's serialized as an object instead of a number
      // NOTE: Once https://github.com/XRPLF/xrpl4j/issues/649 is implemented, this line can be removed.
      final JsonNode flagsNode = objNode.get("Flags");
      if (flagsNode != null && flagsNode.isObject() && flagsNode.has("value")) {
        objNode.put("Flags", flagsNode.get("value").asLong());
      }
    }
    final String txHex = this.encode(txNode);
    final UnsignedByteArray txBytes = UnsignedByteArray.fromHex(
      SignedTransaction.SIGNED_TRANSACTION_HASH_PREFIX + txHex
    );
    return HashingUtils.sha512Half(txBytes);
  }
}
