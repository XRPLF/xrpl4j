package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.modules.UnsignedByteArrayDeserializer;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit test for {@link UnsignedByteArrayDeserializer}.
 *
 * @deprecated This will move to the xrpl4j-core module.
 */
@Deprecated
class UnsignedBytesDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(UnsignedByteArray.class, new UnsignedByteArrayDeserializer());
    this.objectMapper = ObjectMapperFactory.create().registerModule(simpleModule);
  }

  @Test
  void deserialize() throws JsonProcessingException {
    UnsignedByteArray unsignedByteArray = objectMapper.readValue("\"ABCDEF1234567890\"", UnsignedByteArray.class);
    assertThat(unsignedByteArray).isEqualTo(UnsignedByteArray.fromHex("ABCDEF1234567890"));
  }
}