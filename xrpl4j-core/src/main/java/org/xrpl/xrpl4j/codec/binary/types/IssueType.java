package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

public class IssueType extends SerializedType<IssueType> {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  private static final UnsignedByteArray NO_ACCOUNT =
    UnsignedByteArray.fromHex("0000000000000000000000000000000000000001");

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
      // MPT binary format: issuer (20 bytes) + NO_ACCOUNT (20 bytes) + sequence (4 bytes) = 44 bytes
      // MPTID hex is 48 chars (24 bytes): sequence (4 bytes big-endian) + issuer AccountID (20 bytes)
      String mptIdHex = issue.mptIssuanceId().get().asText();
      UnsignedByteArray mptIdBytes = UnsignedByteArray.fromHex(mptIdHex);

      // Extract sequence (bytes 0-4 of MPTID, big-endian) and issuer (bytes 4-24 of MPTID)
      UnsignedByteArray sequenceBE = mptIdBytes.slice(0, 4);
      UnsignedByteArray issuerBytes = mptIdBytes.slice(4, 24);

      // Convert sequence from big-endian (MPTID hex) to little-endian (wire format)
      UnsignedByteArray sequenceLE = sequenceBE.reverse();

      UnsignedByteArray noAccountCopy = NO_ACCOUNT.slice(0, 20);
      UnsignedByteArray byteArray = issuerBytes.append(noAccountCopy).append(sequenceLE);
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
    CurrencyType currency = new CurrencyType().fromParser(parser);
    if (currency.toJson().asText().equals("XRP")) {
      return new IssueType(currency.value());
    }

    // Read the next 20 bytes (account or noAccount sentinel)
    AccountIdType accountId = new AccountIdType().fromParser(parser);

    // Check if this is the noAccount sentinel (indicating MPT)
    if (accountId.value().equals(NO_ACCOUNT)) {
      // MPT: read the 4-byte sequence
      UnsignedByteArray sequenceBytes = parser.read(4);
      return new IssueType(currency.value().append(accountId.value()).append(sequenceBytes));
    }

    // IOU: currency + issuer
    return new IssueType(currency.value().append(accountId.value()));
  }

  @Override
  public JsonNode toJson() {
    BinaryParser parser = new BinaryParser(this.toHex());
    CurrencyType currency = new CurrencyType().fromParser(parser);
    JsonNode currencyJson = currency.toJson();

    if (currencyJson.asText().equals("XRP")) {
      return objectMapper.valueToTree(
        Issue.builder().currency(currencyJson).build()
      );
    }

    // Read the account field
    AccountIdType accountId = new AccountIdType().fromParser(parser);

    if (accountId.value().equals(NO_ACCOUNT)) {
      // MPT: read 4-byte sequence (little-endian on wire), reconstruct MPTID
      UnsignedByteArray sequenceLE = parser.read(4);
      // Convert sequence from little-endian (wire) to big-endian (MPTID hex)
      UnsignedByteArray sequenceBE = sequenceLE.reverse();
      // MPTID = sequence (4 bytes BE) + issuer AccountID (20 bytes)
      // The "currency" bytes hold the issuer when it's an MPT
      UnsignedByteArray mptId = sequenceBE.append(currency.value());
      String mptIdHex = mptId.hexValue();
      return objectMapper.valueToTree(
        Issue.builder().mptIssuanceId(new TextNode(mptIdHex)).build()
      );
    }

    // IOU
    return objectMapper.valueToTree(
      Issue.builder().currency(currencyJson).issuer(accountId.toJson()).build()
    );
  }
}
