package org.xrpl.xrpl4j.codec.fixtures;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.xrpl.xrpl4j.codec.fixtures.codec.CodecFixtures;
import org.xrpl.xrpl4j.codec.fixtures.data.DataDrivenFixtures;

import java.io.File;
import java.io.IOException;

public class FixtureUtils {

  private static ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new GuavaModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static CodecFixtures getCodecFixtures() throws IOException {
    return objectMapper.readerFor(CodecFixtures.class)
      .readValue(new File("src/test/resources/codec-fixtures.json"));
  }


  public static DataDrivenFixtures getDataDrivenFixtures() throws IOException {
    return objectMapper.readerFor(DataDrivenFixtures.class)
      .readValue(new File("src/test/resources/data-driven-tests.json"));
  }

}
