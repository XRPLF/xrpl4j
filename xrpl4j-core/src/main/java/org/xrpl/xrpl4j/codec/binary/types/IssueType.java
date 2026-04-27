package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;
import org.xrpl.xrpl4j.model.AddressConstants;

import java.util.regex.Pattern;

public class IssueType extends SerializedType<IssueType> {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

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

    Issue issue = objectMapper.treeToValue(node, Issue.class);

    if (issue.mptIssuanceId().isPresent()) {
      // mpt_issuance_id (192-bit integer): sequence (4 bytes big-endian) || issuer (20 bytes) = 24 bytes
      // Binary encoded representation: issuer (20 bytes) || ACCOUNT_ONE (20 bytes) || sequence (4 bytes) = 44 bytes
      String mptIssuanceId = issue.mptIssuanceId().get().asText();

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

    UnsignedByteArray byteArray = new CurrencyType().fromJson(issue.currency().get()).value();
    issue.issuer().ifPresent(
      issuer -> byteArray.append(new AccountIdType().fromJson(issuer).value())
    );

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
    BinaryParser parser = new BinaryParser(this.toHex());
    CurrencyType currency = new CurrencyType().fromParser(parser);
    JsonNode currencyJson = currency.toJson();

    // XRP
    if (currencyJson.asText().equals("XRP")) {
      return objectMapper.valueToTree(
        Issue.builder().currency(currencyJson).build()
      );
    }

    AccountIdType accountId = new AccountIdType().fromParser(parser);

    // MPT
    if (accountId.value().equals(ACCOUNT_ONE)) {
      // Convert sequence from little-endian to big-endian
      UnsignedByteArray sequenceLE = parser.read(4);
      UnsignedByteArray sequenceBE = sequenceLE.reverse();

      // Currency bytes hold the issuer when it's an MPT
      String mptIssuanceIdHex = sequenceBE.append(currency.value()).hexValue();
      return objectMapper.valueToTree(
        Issue.builder().mptIssuanceId(new TextNode(mptIssuanceIdHex)).build()
      );
    }

    // IOU
    return objectMapper.valueToTree(
      Issue.builder().currency(currencyJson).issuer(accountId.toJson()).build()
    );
  }
}
