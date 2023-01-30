package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link VoteWeight}s.
 */
public class VoteWeightDeserializer extends StdDeserializer<VoteWeight> {

  /**
   * No-args constructor.
   */
  public VoteWeightDeserializer() {
    super(VoteWeight.class);
  }

  @Override
  public VoteWeight deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return VoteWeight.of(UnsignedInteger.valueOf(jsonParser.getLongValue()));
  }
}
