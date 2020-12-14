package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

class Vector256TypeTest {

  public static final String VALUE1 =
    "42426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369F97ABF044EE";
  public static final String VALUE2 =
    "4C97EBA926031A7CF7D7B36FDE3ED66DDA5421192D63DE53FFB46E43B9DC8373";
  private static final String JSON = "[\"" + VALUE1 + "\",\"" + VALUE2 + "\"]";
  private static final String HEX = VALUE1 + VALUE2;
  private final Vector256Type codec = new Vector256Type();

  @Test
  void decode() {
    BinaryParser parser = new BinaryParser(HEX);
    assertThat(codec.fromParser(parser, 32));
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(JSON).toHex()).isEqualTo(HEX);
  }

}
