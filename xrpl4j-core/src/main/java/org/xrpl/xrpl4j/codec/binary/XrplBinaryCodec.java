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

  // Role-specific signing prefixes introduced by rippled's fixCleanup3_4_0 amendment (PR #8162). Each role signs a
  // distinct prefix so a signature made for one role cannot be replayed in another. These signatures are only valid
  // on a network where fixCleanup3_4_0 is enabled.
  public static final String COUNTERPARTY_SIGNATURE_PREFIX = "43505400"; // "CPT\0" - CounterpartySignature single-sign
  public static final String COUNTERPARTY_MULTI_SIGNATURE_PREFIX = "43504D00"; // "CPM\0" - CounterpartySignature multi
  public static final String SPONSOR_SIGNATURE_PREFIX = "53504E00"; // "SPN\0" - SponsorSignature single-sign
  public static final String SPONSOR_MULTI_SIGNATURE_PREFIX = "53504D00"; // "SPM\0" - SponsorSignature multi-sign

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
    return encodeForSigning(json, TRX_SIGNATURE_PREFIX);
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string, prepending the supplied single-signing {@code prefix}. Only
   * the 4-byte prefix distinguishes the role-specific signing payloads; the encoded signing fields are otherwise
   * identical.
   *
   * @param json   String containing JSON to be encoded.
   * @param prefix The hex-encoded 4-byte signing prefix to prepend (e.g. {@link #TRX_SIGNATURE_PREFIX}).
   *
   * @return hex encoded representation
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  private String encodeForSigning(String json, String prefix) throws JsonProcessingException {
    JsonNode node = BINARY_CODEC_OBJECT_MAPPER.readTree(json);
    return prefix + encode(removeNonSigningFields(node));
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for signing a {@code CounterpartySignature} (e.g. the
   * lender's signature on a {@code LoanSet}). Identical to {@link #encodeForSigning(String)} except that the payload is
   * bound to the counterparty role via the {@code CPT\0} prefix, so the resulting signature cannot be replayed as any
   * other role. Requires the {@code fixCleanup3_4_0} amendment (rippled PR #8162).
   *
   * @param json String containing JSON to be encoded.
   *
   * @return hex encoded representation
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encodeForSigningCounterparty(String json) throws JsonProcessingException {
    return encodeForSigning(json, COUNTERPARTY_SIGNATURE_PREFIX);
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for signing a {@code SponsorSignature}. Identical to
   * {@link #encodeForSigning(String)} except that the payload is bound to the sponsor role via the {@code SPN\0}
   * prefix, so the resulting signature cannot be replayed as any other role. Requires the {@code fixCleanup3_4_0}
   * amendment (rippled PR #8162).
   *
   * @param json String containing JSON to be encoded.
   *
   * @return hex encoded representation
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encodeForSigningSponsor(String json) throws JsonProcessingException {
    return encodeForSigning(json, SPONSOR_SIGNATURE_PREFIX);
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
   * <p>Per XLS-0056 V1_1, the payload is: {@code HashPrefix::Batch} + outer {@code Account} + sequence + {@code Flags}
   * + count + inner tx IDs, followed by the {@code batchSignerAddress} as the per-signer suffix.</p>
   *
   * @param batch              A {@link Batch} to be encoded.
   * @param batchSignerAddress The {@link Address} of the BatchSigner entry (appended as a per-signer suffix).
   *
   * @return An {@link UnsignedByteArray} with the signable bytes.
   */
  public UnsignedByteArray encodeForBatchInnerSigning(Batch batch, Address batchSignerAddress) {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(batchSignerAddress);
    try {
      UnsignedByteArray signableBytes = buildBatchSigningPayload(batch);

      // Append BatchSigner's own account ID as per-signer suffix (V1_1 single-sign suffix)
      String signerAccountIdHex = new AccountIdType().fromJson(new TextNode(batchSignerAddress.value())).toHex();
      signableBytes.append(UnsignedByteArray.fromHex(signerAccountIdHex));

      return signableBytes;
    } catch (JsonProcessingException e) {
      // Test Coverage Note: this catch block is for defensive error handling and is otherwise challenging to test
      // in a unit test without mocking static fields or using reflection to create malformed objects, which would
      // not be representative of real usage scenarios. In practice, JsonProcessingException should never be thrown
      // during normal operation with valid objects, which immutables typically will enforce.
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Encode a {@link Batch} for multi-signing by a specific signer. This is used when a multi-sig account acts as a
   * BatchSigner with nested Signers. Per XLS-0056 V1_1 / rippled's {@code checkBatchMultiSign}, the payload is the base
   * batch serialization followed by {@code batchSignerAddress} then {@code nestedSignerAddress} (i.e.
   * {@code finishMultiSigningData(batchSignerAddress, nestedSignerAddress)}).
   *
   * @param batch               The {@link Batch} to encode.
   * @param batchSignerAddress  The {@link Address} of the BatchSigner entry (outer multi-sig account).
   * @param nestedSignerAddress The {@link Address} of the individual signer within the BatchSigner's Signers list.
   *
   * @return An {@link UnsignedByteArray} containing the batch serialization with both account ID suffixes.
   */
  public UnsignedByteArray encodeForBatchInnerMultiSigning(
    final Batch batch, final Address batchSignerAddress, final Address nestedSignerAddress
  ) {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(batchSignerAddress);
    Objects.requireNonNull(nestedSignerAddress);
    try {
      UnsignedByteArray result = buildBatchSigningPayload(batch);

      // Append batchSignerAddress + nestedSignerAddress (finishMultiSigningData per V1_1)
      String batchSignerIdHex = new AccountIdType().fromJson(new TextNode(batchSignerAddress.value())).toHex();
      result.append(UnsignedByteArray.fromHex(batchSignerIdHex));

      String nestedSignerIdHex = new AccountIdType().fromJson(new TextNode(nestedSignerAddress.value())).toHex();
      result.append(UnsignedByteArray.fromHex(nestedSignerIdHex));

      return result;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Builds the base batch signing payload (items 1–6 from XLS-0056 V1_1 §2.1.3.2), shared by both single-sign and
   * multi-sign paths: {@code HashPrefix::Batch} + outer {@code Account} + sequence + {@code Flags} + count + inner tx
   * IDs.
   */
  private UnsignedByteArray buildBatchSigningPayload(Batch batch) throws JsonProcessingException {
    // Start with batch prefix (0x42434800 = "BCH\0")
    UnsignedByteArray signableBytes = UnsignedByteArray.fromHex(XrplBinaryCodec.BATCH_SIGNATURE_PREFIX);

    // Add outer account ID (20 bytes)
    String accountIdHex = new AccountIdType().fromJson(new TextNode(batch.account().value())).toHex();
    signableBytes.append(UnsignedByteArray.fromHex(accountIdHex));

    // Add sequence value (4 bytes): TicketSequence if Sequence==0, else Sequence
    final int sequenceValue = batch.sequence().longValue() == 0L ?
      batch.ticketSequence().map(ts -> ts.intValue()).orElse(0) :
      batch.sequence().intValue();
    HashingUtils.addUInt32(signableBytes, sequenceValue);

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
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for a counterparty multi-signature (e.g. a multi-signing
   * lender on a {@code LoanSet}). The payload is bound to the counterparty role via the {@code CPM\0} prefix, so the
   * resulting signature cannot be replayed as any other role. Requires the {@code fixCleanup3_4_0} amendment (rippled
   * PR #8162).
   *
   * <p>Like sponsor multi-signing (and unlike {@link #encodeForMultiSigning(String, String)}), this preserves the
   * first-party signer's {@code SigningPubKey} in the signed data rather than clearing it.</p>
   *
   * @param json         A {@link String} containing JSON to be encoded.
   * @param xrpAccountId A {@link String} containing the XRPL AccountId of the signer.
   *
   * @return hex encoded representation
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encodeForMultiSigningCounterparty(String json, String xrpAccountId) throws JsonProcessingException {
    return encodeForMultiSigningWithSigningPubKey(json, xrpAccountId, COUNTERPARTY_MULTI_SIGNATURE_PREFIX);
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for a sponsor multi-signature. The payload is bound to the
   * sponsor role via the {@code SPM\0} prefix, so the resulting signature cannot be replayed as any other role.
   * Requires the {@code fixCleanup3_4_0} amendment (rippled PR #8162).
   *
   * <p>Like counterparty multi-signing (and unlike {@link #encodeForMultiSigning(String, String)}), this preserves the
   * first-party signer's {@code SigningPubKey} in the signed data rather than clearing it.</p>
   *
   * @param json         A {@link String} containing JSON to be encoded.
   * @param xrpAccountId A {@link String} containing the XRPL AccountId of the signer.
   *
   * @return hex encoded representation
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encodeForMultiSigningSponsor(String json, String xrpAccountId) throws JsonProcessingException {
    return encodeForMultiSigningWithSigningPubKey(json, xrpAccountId, SPONSOR_MULTI_SIGNATURE_PREFIX);
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string for multi-signing purposes, preserving the existing
   * {@code SigningPubKey} field and prepending the supplied multi-signing {@code prefix}. Unlike
   * {@link #encodeForMultiSigning(String, String)}, this does <b>not</b> clear the {@code SigningPubKey} field, which
   * is necessary when a co-signer (counterparty or sponsor) multi-signs a transaction where the first-party signer's
   * {@code SigningPubKey} must remain intact in the signed data. Only the 4-byte prefix distinguishes the
   * role-specific payloads; the encoded signing fields and account ID suffix are otherwise identical.
   *
   * @param json         A {@link String} containing JSON to be encoded.
   * @param xrpAccountId A {@link String} containing the XRPL AccountId of the signer.
   * @param prefix       The hex-encoded 4-byte multi-signing prefix to prepend.
   *
   * @return hex encoded representation
   *
   * @throws JsonProcessingException if JSON is not valid.
   */
  private String encodeForMultiSigningWithSigningPubKey(
    String json, String xrpAccountId, String prefix
  ) throws JsonProcessingException {
    JsonNode node = BINARY_CODEC_OBJECT_MAPPER.readTree(json);
    if (!node.isObject()) {
      throw new IllegalArgumentException("JSON object required for signing");
    }
    // NOTE: Unlike encodeForMultiSigning, we do NOT clear SigningPubKey here.
    String suffix = new AccountIdType().fromJson(new TextNode(xrpAccountId)).toHex();
    return prefix + encode(removeNonSigningFields(node)) + suffix;
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
    if (isSingleSignPrefix(encodedTransaction)) {
      nonSignPrefixHex = encodedTransaction.substring(TRX_SIGNATURE_PREFIX.length());
    } else if (isMultiSignPrefix(encodedTransaction)) {
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

  // Every signing prefix is a 4-byte (8 hex char) value with no suffix on single-sign payloads, so they can be
  // stripped uniformly. The counterparty/sponsor role prefixes (fixCleanup3_4_0, PR #8162) are handled alongside the
  // plain transaction prefix.
  private boolean isSingleSignPrefix(String encodedTransaction) {
    return encodedTransaction.startsWith(TRX_SIGNATURE_PREFIX) ||
      encodedTransaction.startsWith(COUNTERPARTY_SIGNATURE_PREFIX) ||
      encodedTransaction.startsWith(SPONSOR_SIGNATURE_PREFIX);
  }

  // Multi-sign payloads carry a trailing 20-byte signer account ID suffix regardless of role prefix.
  private boolean isMultiSignPrefix(String encodedTransaction) {
    return encodedTransaction.startsWith(TRX_MULTI_SIGNATURE_PREFIX) ||
      encodedTransaction.startsWith(COUNTERPARTY_MULTI_SIGNATURE_PREFIX) ||
      encodedTransaction.startsWith(SPONSOR_MULTI_SIGNATURE_PREFIX);
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
