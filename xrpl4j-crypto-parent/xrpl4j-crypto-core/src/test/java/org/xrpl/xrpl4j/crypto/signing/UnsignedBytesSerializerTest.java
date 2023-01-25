package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.signing.UnsignedByteArraySerializer;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit test for {@link UnsignedByteArraySerializer}.
 *
 * @deprecated This will move to the xrpl4j-core module.
 */
@Deprecated
class UnsignedBytesSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(UnsignedByteArray.class, new UnsignedByteArraySerializer());
    this.objectMapper = ObjectMapperFactory.create().registerModule(simpleModule);
  }

  @Test
  void serialize() throws JsonProcessingException {
    UnsignedByteArray unsignedByteArray = UnsignedByteArray.fromHex("ABCDEF1234567890");
    String serializedValue = objectMapper.writeValueAsString(unsignedByteArray);
    assertThat(serializedValue).isEqualTo("\"ABCDEF1234567890\"");
  }
}