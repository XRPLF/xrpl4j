package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCount;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link VoteWeight}s.
 */
public class XChainCountSerializer extends StdScalarSerializer<XChainCount> {

  /**
   * No-args constructor.
   */
  public XChainCountSerializer() {
    super(XChainCount.class, false);
  }

  @Override
  public void serialize(XChainCount count, JsonGenerator gen, SerializerProvider provider) throws IOException {
    // sfXChainAccountCreateCount and sfXChainAccountClaimCount are STUInt64s, which in JSON is represented as a
    // hex-encoded String.
    gen.writeString(count.value().toString(16));
  }
}
