package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.codec.fixtures.FixtureUtils;
import com.ripple.xrpl4j.codec.fixtures.codec.CodecFixtures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DefaultBinaryCodecTest {

  private static CodecFixtures fixtures;

  private DefaultBinaryCodec codec = new DefaultBinaryCodec();

  @BeforeAll
  public static void loadFixtures() throws IOException {
    fixtures = FixtureUtils.getCodecFixtures();
  }

  @Test
  void decode() {
    // FIXME
//    fixtures.transactions().forEach(fixture -> {
//      assertThat(codec.decode(fixture.binary())).isEqualTo(fixture.json().toString());
//    });
  }

  @Test
  void encode() {
    // FIXME
//    fixtures.transactions().forEach(fixture -> {
//      assertThat(codec.encode(fixture.json().toString())).isEqualTo(fixture.binary());
//    });
  }
}