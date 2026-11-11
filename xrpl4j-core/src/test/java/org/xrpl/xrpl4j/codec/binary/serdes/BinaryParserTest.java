package org.xrpl.xrpl4j.codec.binary.serdes;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.codec.binary.types.BlobType;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link BinaryParser}, focused on variable-length (VL) prefix decoding.
 *
 * <p>Per the XRPL binary format, a VL prefix is 1, 2, or 3 bytes:
 * <ul>
 *   <li>lengths 0..192: one byte, {@code b1 = length}</li>
 *   <li>lengths 193..12480: two bytes, {@code length = 193 + (b1 - 193) * 256 + b2}</li>
 *   <li>lengths 12481..918744: three bytes, {@code length = 12481 + (b1 - 241) * 65536 + b2 * 256 + b3}</li>
 * </ul>
 *
 * @see <a href="https://xrpl.org/docs/references/protocol/binary-format#length-prefixing">Length Prefixing</a>
 */
class BinaryParserTest {

  private static final int MAX_ONE_BYTE_LENGTH = 192;
  private static final int MIN_TWO_BYTE_LENGTH = 193;
  private static final int MAX_TWO_BYTE_LENGTH = 12480;
  private static final int MIN_THREE_BYTE_LENGTH = 12481;
  private static final int MAX_THREE_BYTE_LENGTH = 918744;

  /**
   * Decodes hand-built VL headers taken straight from the spec formulas, independently of
   * {@link BinarySerializer}, so an encoder/decoder pair that is consistently wrong cannot mask a defect.
   */
  @ParameterizedTest(name = "header {0} decodes to length {1}")
  @CsvSource({
    // one byte
    "00, 0",
    "01, 1",
    "C0, 192",
    // two bytes: 193 + (b1 - 193) * 256 + b2
    "C100, 193",
    "C101, 194",
    "C1FF, 448",
    "C200, 449",
    "EFFF, 12224",
    "F000, 12225",
    "F0FE, 12479",
    "F0FF, 12480",
    // three bytes: 12481 + (b1 - 241) * 65536 + b2 * 256 + b3
    "F10000, 12481",
    "F10001, 12482",
    "F1FFFF, 78016",
    "F20000, 78017",
    "FED417, 918744"
  })
  void readVariableLengthLengthFromSpecVectors(String headerHex, int expectedLength) {
    BinaryParser parser = new BinaryParser(headerHex);
    assertThat(parser.readVariableLengthLength()).isEqualTo(expectedLength);
    assertThat(parser.hasMore()).as("the whole header, and only the header, is consumed").isFalse();
  }

  @Test
  void readVariableLengthLengthRejectsReservedFirstByte() {
    assertThatThrownBy(() -> new BinaryParser("FF0000").readVariableLengthLength())
      .isInstanceOf(Error.class)
      .hasMessageContaining("Invalid variable length indicator");
  }

  /**
   * Decodes every possible two-byte VL header (first byte 193..240, second byte 0..255) against the spec formula.
   * This is the range that was mis-decoded before the fix for issue #805.
   */
  @Test
  void readVariableLengthLengthForEveryTwoByteHeader() {
    for (int b1 = MIN_TWO_BYTE_LENGTH; b1 <= 240; b1++) {
      for (int b2 = 0; b2 <= 255; b2++) {
        String headerHex = String.format("%02X%02X", b1, b2);
        int expectedLength = MIN_TWO_BYTE_LENGTH + (b1 - MIN_TWO_BYTE_LENGTH) * 256 + b2;
        BinaryParser parser = new BinaryParser(headerHex);
        assertThat(parser.readVariableLengthLength()).as("header %s", headerHex).isEqualTo(expectedLength);
        assertThat(parser.hasMore()).as("header %s fully consumed", headerHex).isFalse();
      }
    }
  }

  /**
   * Round-trips payloads through {@link BinarySerializer} and back: every one-byte length, a sample of every 2-byte
   * and 3-byte length, and all the boundaries. Each payload is followed by a sentinel byte so the test proves the
   * parser consumed exactly the advertised number of bytes, not merely that it returned the right length.
   */
  @Test
  void roundTripSampledLengths() {
    IntStream.rangeClosed(0, MAX_ONE_BYTE_LENGTH).forEach(this::assertRoundTrip);
    for (int i = MIN_TWO_BYTE_LENGTH; i <= MAX_TWO_BYTE_LENGTH; i += 61) {
      assertRoundTrip(i);
    }
    for (int i = MIN_THREE_BYTE_LENGTH; i <= 200_000; i += 7_919) {
      assertRoundTrip(i);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {
    MAX_ONE_BYTE_LENGTH, MIN_TWO_BYTE_LENGTH, MAX_TWO_BYTE_LENGTH,
    MIN_THREE_BYTE_LENGTH, MIN_THREE_BYTE_LENGTH + 1, 65535, 65536, 78016, 78017, MAX_THREE_BYTE_LENGTH
  })
  void roundTripBoundaryLengths(int length) {
    assertRoundTrip(length);
  }

  /**
   * End-to-end: a transaction whose MemoData needs a two- or three-byte VL prefix must survive
   * {@link XrplBinaryCodec#encode(String)} followed by {@link XrplBinaryCodec#decode(String)}.
   */
  @ParameterizedTest
  @ValueSource(ints = {MAX_ONE_BYTE_LENGTH, MIN_TWO_BYTE_LENGTH, 1000, MAX_TWO_BYTE_LENGTH, MIN_THREE_BYTE_LENGTH})
  void transactionWithLongMemoRoundTripsThroughCodec(int memoLength) throws Exception {
    String memoHex = BaseEncoding.base16().encode(randomBytes(memoLength));
    String json = "{" +
      "\"TransactionType\":\"Payment\"," +
      "\"Account\":\"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\"," +
      "\"Destination\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\"," +
      "\"Amount\":\"1000\"," +
      "\"Fee\":\"12\"," +
      "\"Sequence\":1," +
      "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
      "\"Memos\":[{\"Memo\":{\"MemoData\":\"" + memoHex + "\"}}]" +
      "}";

    XrplBinaryCodec codec = XrplBinaryCodec.getInstance();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode decoded = objectMapper.readTree(codec.decode(codec.encode(json)));

    assertThat(decoded.at("/Memos/0/Memo/MemoData").asText()).isEqualTo(memoHex);
    assertThat(decoded.get("Account").asText()).isEqualTo("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK");
    assertThat(decoded.get("Destination").asText()).isEqualTo("rrrrrrrrrrrrrrrrrrrrBZbvji");
    assertThat(decoded.get("Fee").asText()).isEqualTo("12");
  }

  private void assertRoundTrip(int length) {
    byte[] payload = randomBytes(length);
    UnsignedByteArray sink = UnsignedByteArray.empty();
    new BinarySerializer(sink).writeLengthEncoded(new BlobType(UnsignedByteArray.of(payload)));
    String expectedPayloadHex = UnsignedByteArray.of(payload).hexValue();
    String sentinelHex = "5A";

    BinaryParser parser = new BinaryParser(sink.hexValue() + sentinelHex);

    assertThat(parser.readVariableLengthLength()).as("decoded length for %d", length).isEqualTo(length);
    if (length > 0) {
      assertThat(parser.read(length).hexValue()).as("payload for %d", length).isEqualTo(expectedPayloadHex);
    }
    assertThat(parser.readUInt8().intValue()).as("cursor must land on the sentinel for %d", length).isEqualTo(0x5A);
    assertThat(parser.hasMore()).as("nothing after the sentinel for %d", length).isFalse();
  }

  private static byte[] randomBytes(int length) {
    byte[] bytes = new byte[length];
    new Random(length).nextBytes(bytes); // seeded by length so failures reproduce deterministically
    return bytes;
  }
}
