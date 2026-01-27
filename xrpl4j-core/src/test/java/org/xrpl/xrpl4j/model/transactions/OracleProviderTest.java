package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value.Immutable;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class OracleProviderTest {

  private final OracleProvider ORACLE_PROVIDER = OracleProvider.of("ABCD");

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> OracleProvider.of(null));
  }

  @Test
  void testEquality() {
    assertThat(ORACLE_PROVIDER).isEqualTo(ORACLE_PROVIDER);
    assertThat(ORACLE_PROVIDER).isNotEqualTo(new Object());
    assertThat(ORACLE_PROVIDER.equals(null)).isFalse();
  }

  @Test
  void testToString() {
    assertThat(ORACLE_PROVIDER.toString()).isEqualTo("ABCD");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    OracleProviderWrapper wrapper = OracleProviderWrapper.of(ORACLE_PROVIDER);

    String json = "{\"value\": \"ABCD\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    OracleProviderWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    OracleProviderWrapper deserialized = objectMapper.readValue(
      serialized, OracleProviderWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Immutable
  @JsonSerialize(as = ImmutableOracleProviderWrapper.class)
  @JsonDeserialize(as = ImmutableOracleProviderWrapper.class)
  interface OracleProviderWrapper {

    static OracleProviderWrapper of(OracleProvider value) {
      return ImmutableOracleProviderWrapper.builder().value(value).build();
    }

    OracleProvider value();

  }
}
