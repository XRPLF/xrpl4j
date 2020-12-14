package org.xrpl.xrpl4j.codec.binary;

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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class XrplBinaryCodec {

  public static final String TRX_SIGNATURE_PREFIX = "53545800";

  public static final String TRX_MULTI_SIGNATURE_PREFIX = "534D5400";

  public static final String PAYMENT_CHANNEL_CLAIM_SIGNATURE_PREFIX = "434C4D00";
  public static final String CHANNEL_FIELD_NAME = "Channel";
  public static final String AMOUNT_FIELD_NAME = "Amount";

  private static final DefinitionsService definitionsService = DefinitionsService.getInstance();
  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

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
    JsonNode node = BinaryCodecObjectMapperFactory.getObjectMapper().readTree(json);
    return encode(node);
  }

  /**
   * Encodes JSON to canonical XRPL binary as a hex string.
   *
   * @param jsonNode A {@link JsonNode} containing JSON to be encoded.
   *
   * @return A {@link String} containing the hex-encoded representation of {@code jsonNode}.
   *
   * @throws JsonProcessingException if {@code jsonNode} is not valid JSON.
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
    JsonNode node = BinaryCodecObjectMapperFactory.getObjectMapper().readTree(json);
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
    JsonNode node = BinaryCodecObjectMapperFactory.getObjectMapper().readTree(json);
    if (!node.isObject()) {
      throw new IllegalArgumentException("JSON object required for signing");
    }
    // any existing signing keys should not also be signed
    ((ObjectNode) node).set("SigningPubKey", new TextNode(""));
    String suffix = new AccountIdType().fromJson(new TextNode(xrpAccountId)).toHex();
    return TRX_MULTI_SIGNATURE_PREFIX + encode(removeNonSigningFields(node)) + suffix;
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
    JsonNode node = BinaryCodecObjectMapperFactory.getObjectMapper().readTree(json);
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
   * Decodes canonical XRPL binary hex string to JSON.
   *
   * @param hex A {@link String} value to decode.
   *
   * @return A {@link String} representing the decoded hex.
   */
  public String decode(String hex) {
    return new BinaryParser(hex).readType(STObjectType.class)
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

    return new ObjectNode(objectMapper.getNodeFactory(), signingFields);
  }

  private Boolean isSigningField(String fieldName) {
    return definitionsService.getFieldInstance(fieldName).map(FieldInstance::isSigningField).orElse(false);
  }


}
