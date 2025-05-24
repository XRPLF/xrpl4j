package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Custom Jackson deserializer for {@link VoteWeight}s.
 */
public class XChainClaimIdDeserializer extends StdDeserializer<XChainClaimId> {

  /**
   * No-args constructor.
   */
  public XChainClaimIdDeserializer() {
    super(XChainClaimId.class);
  }

  @Override
  public XChainClaimId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    // sfXChainClaimID is a STUInt64, which in JSON is represented as a hex-encoded String.
    return XChainClaimId.of(UnsignedLong.valueOf(jsonParser.getText(), 16));
  }
}
