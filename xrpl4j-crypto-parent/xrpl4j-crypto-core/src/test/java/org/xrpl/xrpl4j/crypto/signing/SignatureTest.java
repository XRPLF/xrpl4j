package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class SignatureTest {

  protected final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void serializeSignature() throws JsonProcessingException {

    String hexSign = "E2ACD61C90D93433402B1F704DA38DF72876B6788C2C05B3196E14BC711AECFF14A7D6276439A1" +
      "98D8B4880EE2DB544CF351A8CE231B3340F42F9BF1EDBF5104";

    Signature signature = Signature.builder()
      .value(UnsignedByteArray.fromHex(hexSign))
      .build();
    String stringJson = objectMapper.writeValueAsString(signature);

    assertThat(stringJson).isEqualTo("\"" + hexSign + "\"");

    Signature deserialized = objectMapper.readValue(stringJson, Signature.class);

    assertThat(signature).isEqualTo(deserialized);

    WrappedSignature wrappedSignature = WrappedSignature.builder()
      .signature(signature)
      .build();
    String serializedWrappedSignature = objectMapper.writeValueAsString(wrappedSignature);

    String serialized = "{\"signature\":\"E2ACD61C90D93433402B1F704DA38DF72876B6788C2C05B3196E14BC711AECFF14A7D627643" +
      "9A198D8B4880EE2DB544CF351A8CE231B3340F42F9BF1EDBF5104\"}";
    assertThat(serializedWrappedSignature).isEqualTo(serialized);

    WrappedSignature deserializedWrappedSignature = objectMapper.readValue(
      serializedWrappedSignature, WrappedSignature.class
    );
    assertThat(deserializedWrappedSignature).isEqualTo(wrappedSignature);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableWrappedSignature.class)
  @JsonDeserialize(as = ImmutableWrappedSignature.class)
  interface WrappedSignature {

    static ImmutableWrappedSignature.Builder builder() {
      return ImmutableWrappedSignature.builder();
    }

    Signature signature();
  }
}
