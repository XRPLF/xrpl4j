package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link OracleDocumentId}.
 */
public class OracleDocumentIdTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    OracleDocumentId count = OracleDocumentId.of(UnsignedInteger.ONE);
    assertThat(count.toString()).isEqualTo("1");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    OracleDocumentId count = OracleDocumentId.of(UnsignedInteger.ONE);
    OracleDocumentIdWrapper wrapper = OracleDocumentIdWrapper.of(count);

    String json = "{\"value\": 1}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    OracleDocumentIdWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    OracleDocumentIdWrapper deserialized = objectMapper.readValue(
      serialized, OracleDocumentIdWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Immutable
  @JsonSerialize(as = ImmutableOracleDocumentIdWrapper.class)
  @JsonDeserialize(as = ImmutableOracleDocumentIdWrapper.class)
  interface OracleDocumentIdWrapper {

    static OracleDocumentIdWrapper of(OracleDocumentId value) {
      return ImmutableOracleDocumentIdWrapper.builder().value(value).build();
    }

    OracleDocumentId value();

  }
}
