package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link NfTokenId}s.
 */
public class NfTokenIdSerializer extends StdScalarSerializer<NfTokenId> {
  /**
   * No-args constructor.
   */
  public NfTokenIdSerializer() {
    super(NfTokenId.class, false);
  }

  @Override
  public void serialize(NfTokenId tokenId, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(tokenId.value());
  }
}
