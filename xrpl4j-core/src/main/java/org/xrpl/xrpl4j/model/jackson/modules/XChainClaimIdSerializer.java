package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link VoteWeight}s.
 */
public class XChainClaimIdSerializer extends StdScalarSerializer<XChainClaimId> {

  /**
   * No-args constructor.
   */
  public XChainClaimIdSerializer() {
    super(XChainClaimId.class, false);
  }

  @Override
  public void serialize(XChainClaimId claimId, JsonGenerator gen, SerializerProvider provider) throws IOException {
    // sfXChainClaimID is a STUInt64, which in JSON is represented as a hex-encoded String.
    gen.writeString(claimId.value().toString(16));
  }
}
