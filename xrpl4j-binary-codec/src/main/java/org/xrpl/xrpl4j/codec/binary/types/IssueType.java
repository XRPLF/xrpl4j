package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

public class IssueType extends SerializedType<IssueType> {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  public IssueType() {
    this(UnsignedByteArray.ofSize(40));
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

    UnsignedByteArray byteArray = new CurrencyType().fromJson(issue.currency()).value();
    issue.issuer().ifPresent(
      issuer -> byteArray.append(new AccountIdType().fromJson(issuer).value())
    );

    return new IssueType(byteArray);
  }

  @Override
  public IssueType fromParser(BinaryParser parser) {
    CurrencyType currency = new CurrencyType().fromParser(parser);
    if (currency.toJson().toString().equals("XRP")) {
      return new IssueType(currency.value());
    }
    AccountIdType issuer = new AccountIdType().fromParser(parser);
    return new IssueType(currency.value().append(issuer.value()));
  }

  @Override
  public JsonNode toJson() {
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
