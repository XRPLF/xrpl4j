package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ripple.xrpl4j.codec.binary.definitions.DefinitionsService;
import com.ripple.xrpl4j.codec.fixtures.FixtureUtils;
import com.ripple.xrpl4j.codec.fixtures.data.ValueTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

abstract class BaseSerializerTypeTest {

  protected static Stream<Arguments> dataDrivenFixturesForType(SerializedType serializedType) throws IOException {
    return FixtureUtils.getDataDrivenFixtures().valuesTests()
        .stream()
        .filter(fixture -> fixture.type().equals(SerializedType.getNameByType(serializedType)))
        .map(Arguments::of);
  }

  abstract SerializedType getType();

  @ParameterizedTest
  @MethodSource("dataDrivenFixtures")
  void fixtureTests(ValueTest fixture) throws IOException {
    SerializedType serializedType = getType();
    JsonNode value = getValue(fixture);
    if (fixture.error() != null) {
      Assertions.assertThrows(Exception.class, () -> serializedType.fromJSON(value));
    } else {
      assertThat(serializedType.fromJSON(value).toHex()).isEqualTo(fixture.expectedHex());
    }
  }

  private JsonNode getValue(ValueTest test) {
    return DefinitionsService.getInstance().mapFieldSpecialization(test.typeSpecializationField(), test.testJson().asText())
        .map(val -> val.toString())
        .map(TextNode::new)
        .map(JsonNode.class::cast)
        .orElse(test.testJson());
  }

}
