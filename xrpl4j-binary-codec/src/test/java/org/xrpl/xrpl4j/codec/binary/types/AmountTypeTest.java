package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.stream.Stream;

class AmountTypeTest extends BaseSerializerTypeTest {

  private final static AmountType codec = new AmountType();

  private static Stream<Arguments> dataDrivenFixtures() throws IOException {
    return dataDrivenFixturesForType(codec);
  }

  @Override
  SerializedType getType() {
    return codec;
  }

  @Test
  void decodeXrpAmount() {
    assertThat(codec.fromHex("4000000000000064").toJson().asText()).isEqualTo("100");
    assertThat(codec.fromHex("416345785D8A0000").toJson().asText()).isEqualTo("100000000000000000");
  }

  @Test
  void encodeXrpAmount() {
    assertThat(codec.fromJson("100").toHex()).isEqualTo("4000000000000064");
    assertThat(codec.fromJson("100000000000000000").toHex()).isEqualTo("416345785D8A0000");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJson("416345785D8A0001"));
  }

  @Test
  void encodeCurrencyAmount() {
    assertThat(codec.fromJson("{\"value\":\"0\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("800000000000000000000000000000000000000055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF44");

    assertThat(codec.fromJson("{\"value\":\"1\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("D4838D7EA4C6800000000000000000000000000055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF44");

    assertThat(codec.fromJson("{\"value\":\"2\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("D4871AFD498D000000000000000000000000000055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF44");

    assertThat(codec.fromJson("{\"value\":\"-2\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("94871AFD498D000000000000000000000000000055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF44");

    assertThat(codec.fromJson("{\"value\":\"2.1\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("D48775F05A07400000000000000000000000000055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF44");
  }

  @Test
  void decodeCurrencyAmount() {
    assertThat(
        codec.fromHex("D48775F05A07400000000000000000000000000000000000000000000000000000000000000000000000000000000000")
            .toJson().toString())
        .isEqualTo("{\"currency\":\"XRP\",\"value\":\"2.1\",\"issuer\":\"rrrrrrrrrrrrrrrrrrrrrhoLvTp\"}");
  }

  @Test
  void decodeNegativeCurrencyAmount() {
    assertThat(
        codec.fromHex("94838D7EA4C6800000000000000000000000000055534400000000000000000000000000000000000000000000000001")
            .toJson().toString())
        .isEqualTo("{\"currency\":\"USD\",\"value\":\"-1\",\"issuer\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\"}");
  }

  @Test
  void encodeZeroCurrencyAmount() {
    String json = "{\"currency\":\"USD\",\"value\":\"0.0\",\"issuer\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\"}";
    String hex = "800000000000000000000000000000000000000055534400000000000000000000000000000000000000000000000001";
    assertThat(codec.fromJson(json).toHex()).isEqualTo(hex);
  }

  @Test
  void encodeLargeCurrencyAmount() {
    String json = "{\"currency\":\"USD\",\"value\":\"1111111111111111.0\",\"issuer\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\"}";
    String hex = "D843F28CB71571C700000000000000000000000055534400000000000000000000000000000000000000000000000001";
    assertThat(codec.fromJson(json).toHex()).isEqualTo(hex);
  }

}
