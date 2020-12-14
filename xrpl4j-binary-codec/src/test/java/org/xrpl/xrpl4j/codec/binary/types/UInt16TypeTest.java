package org.xrpl.xrpl4j.codec.binary.types;

import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.stream.Stream;

class UInt16TypeTest extends BaseSerializerTypeTest {

  private static final UInt16Type codec = new UInt16Type();

  private static Stream<Arguments> dataDrivenFixtures() throws IOException {
    return dataDrivenFixturesForType(codec);
  }

  @Override
  SerializedType getType() {
    return codec;
  }

}
