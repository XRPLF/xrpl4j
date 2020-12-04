package org.xrpl.xrpl4j.codec.binary.types;

import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.stream.Stream;

class UInt8TypeTest extends BaseSerializerTypeTest {

  private final static UInt8Type codec = new UInt8Type();

  private static Stream<Arguments> dataDrivenFixtures() throws IOException {
    return dataDrivenFixturesForType(codec);
  }

  @Override
  SerializedType getType() {
    return codec;
  }

}
