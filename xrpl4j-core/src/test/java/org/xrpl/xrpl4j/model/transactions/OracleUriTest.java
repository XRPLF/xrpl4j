package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

public class OracleUriTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    OracleUri count = OracleUri.of("ABCD");
    assertThat(count.toString()).isEqualTo("ABCD");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    OracleUri count = OracleUri.of("ABCD");
    OracleUriWrapper wrapper = OracleUriWrapper.of(count);

    String json = "{\"value\": \"ABCD\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    OracleUriWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    OracleUriWrapper deserialized = objectMapper.readValue(
      serialized, OracleUriWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Immutable
  @JsonSerialize(as = ImmutableOracleUriWrapper.class)
  @JsonDeserialize(as = ImmutableOracleUriWrapper.class)
  interface OracleUriWrapper {

    static OracleUriWrapper of(OracleUri value) {
      return ImmutableOracleUriWrapper.builder().value(value).build();
    }

    OracleUri value();

  }
}
