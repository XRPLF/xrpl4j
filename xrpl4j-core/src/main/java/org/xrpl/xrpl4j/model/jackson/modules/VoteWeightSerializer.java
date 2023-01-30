package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link VoteWeight}s.
 */
public class VoteWeightSerializer extends StdScalarSerializer<VoteWeight> {

  /**
   * No-args constructor.
   */
  public VoteWeightSerializer() {
    super(VoteWeight.class, false);
  }

  @Override
  public void serialize(VoteWeight voteWeight, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeNumber(voteWeight.value().longValue());
  }
}
