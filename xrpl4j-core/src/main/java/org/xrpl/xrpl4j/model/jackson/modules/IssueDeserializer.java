package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link Issue} that dispatches to the correct subtype
 * ({@link XrpIssue}, {@link IouIssue}, or {@link MptIssue}) based on the JSON fields present.
 */
public class IssueDeserializer extends StdDeserializer<Issue> {

  /**
   * No-args constructor.
   */
  public IssueDeserializer() {
    super(Issue.class);
  }

  @Override
  public Issue deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = mapper.readTree(jsonParser);

    if (node.has("mpt_issuance_id")) {
      return mapper.treeToValue(node, MptIssue.class);
    } else if (node.has("currency")) {
      String currency = node.get("currency").asText();
      if ("XRP".equals(currency)) {
        return XrpIssue.XRP;
      }
      return mapper.treeToValue(node, IouIssue.class);
    }

    throw new IOException("Cannot deserialize Issue: must contain 'currency' or 'mpt_issuance_id' field");
  }
}
