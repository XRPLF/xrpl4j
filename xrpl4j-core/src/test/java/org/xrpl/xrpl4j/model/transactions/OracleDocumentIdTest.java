package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.api.Assertions;
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

  private final OracleDocumentId ORACLE_DOCUMENT_ID = OracleDocumentId.of(UnsignedInteger.ONE);

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> OracleDocumentId.of(null));
  }

  @Test
  void testEquality() {
    assertThat(ORACLE_DOCUMENT_ID).isEqualTo(ORACLE_DOCUMENT_ID);
    assertThat(ORACLE_DOCUMENT_ID).isNotEqualTo(new Object());
    assertThat(ORACLE_DOCUMENT_ID.equals(null)).isFalse();
  }

  @Test
  void testToString() {
    assertThat(ORACLE_DOCUMENT_ID.toString()).isEqualTo("1");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    OracleDocumentIdWrapper wrapper = OracleDocumentIdWrapper.of(ORACLE_DOCUMENT_ID);

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
