package org.xrpl.xrpl4j.codec.binary;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.types.Hop;

/**
 * Unit tests for {@link BinaryCodecObjectMapperFactory}. These pin the mapper's effective configuration so that
 * refactoring how it is built cannot silently change the JSON the binary codec reads and writes.
 */
class BinaryCodecObjectMapperFactoryTest {

  private final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  @Test
  void defaultPropertyInclusionIsNonEmptyForValuesAndContent() {
    assertThat(objectMapper.getSerializationConfig().getDefaultPropertyInclusion())
      .isEqualTo(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
  }

  @Test
  void featuresAreConfigured() {
    assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isTrue();
    assertThat(objectMapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)).isTrue();
    assertThat(objectMapper.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)).isTrue();
    assertThat(objectMapper.getFactory().isEnabled(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature()))
      .isFalse();
  }

  @Test
  void emptyOptionalsAreOmitted() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(Hop.builder().build())).isEqualTo("{}");
  }

  @Test
  void unknownPropertiesAreRejected() {
    assertThatThrownBy(() -> objectMapper.readValue("{\"unknown\":1}", Hop.class))
      .isInstanceOf(UnrecognizedPropertyException.class);
  }
}
