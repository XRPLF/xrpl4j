package org.xrpl.xrpl4j.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link HashingUtils}.
 */
class HashingUtilsTest {

  ///////////////////
  // sha512Half(UnsignedByteArray)
  ///////////////////

  @Test
  void sha512HalfWithNullUnsignedByteArray() {
    assertThrows(NullPointerException.class, () -> HashingUtils.sha512Half((UnsignedByteArray) null));
  }

  @Test
  void sha512HalfWithEmptyUnsignedByteArray() {
    UnsignedByteArray empty = UnsignedByteArray.empty();
    UnsignedByteArray result = HashingUtils.sha512Half(empty);

    // Verify result is exactly 32 bytes
    assertThat(result.length()).isEqualTo(32);

    // Verify it matches the expected SHA-512 half hash of empty input
    byte[] expectedHash = Hashing.sha512().hashBytes(new byte[0]).asBytes();
    byte[] expectedHalf = new byte[32];
    System.arraycopy(expectedHash, 0, expectedHalf, 0, 32);
    assertThat(result.toByteArray()).isEqualTo(expectedHalf);
  }

  @Test
  void sha512HalfWithKnownInput() {
    // Test with a known input
    UnsignedByteArray input = UnsignedByteArray.fromHex("ABCDEF1234567890");
    UnsignedByteArray result = HashingUtils.sha512Half(input);

    // Verify result is exactly 32 bytes
    assertThat(result.length()).isEqualTo(32);

    // Verify it matches the expected SHA-512 half hash
    byte[] expectedHash = Hashing.sha512().hashBytes(input.toByteArray()).asBytes();
    byte[] expectedHalf = new byte[32];
    System.arraycopy(expectedHash, 0, expectedHalf, 0, 32);
    assertThat(result.toByteArray()).isEqualTo(expectedHalf);
  }

  @Test
  void sha512HalfActualHashVerification() {
    // Test with "test" string
    UnsignedByteArray input = UnsignedByteArray.of("test".getBytes());
    UnsignedByteArray result = HashingUtils.sha512Half(input);

    // Verify result is exactly 32 bytes
    assertThat(result.length()).isEqualTo(32);

    // Compute expected value using Guava directly
    byte[] fullHash = Hashing.sha512().hashBytes("test".getBytes()).asBytes();
    assertThat(fullHash.length).isEqualTo(64); // SHA-512 produces 64 bytes

    // Verify we got the first 32 bytes
    byte[] expectedFirstHalf = new byte[32];
    System.arraycopy(fullHash, 0, expectedFirstHalf, 0, 32);
    assertThat(result.toByteArray()).isEqualTo(expectedFirstHalf);
  }

  ///////////////////
  // sha512Half(byte[])
  ///////////////////

  @Test
  void sha512HalfWithNullByteArray() {
    assertThrows(NullPointerException.class, () -> HashingUtils.sha512Half((byte[]) null));
  }

  @Test
  void sha512HalfWithEmptyByteArray() {
    byte[] empty = new byte[0];
    UnsignedByteArray result = HashingUtils.sha512Half(empty);

    // Verify result is exactly 32 bytes
    assertThat(result.length()).isEqualTo(32);

    // Verify it matches the expected SHA-512 half hash of empty input
    byte[] expectedHash = Hashing.sha512().hashBytes(empty).asBytes();
    byte[] expectedHalf = new byte[32];
    System.arraycopy(expectedHash, 0, expectedHalf, 0, 32);
    assertThat(result.toByteArray()).isEqualTo(expectedHalf);
  }

  @Test
  void sha512HalfByteArrayWithKnownInput() {
    // Test with a known input
    byte[] input = new byte[] {(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12, 0x34, 0x56, 0x78, (byte) 0x90};
    UnsignedByteArray result = HashingUtils.sha512Half(input);

    // Verify result is exactly 32 bytes
    assertThat(result.length()).isEqualTo(32);

    // Verify it matches the expected SHA-512 half hash
    byte[] expectedHash = Hashing.sha512().hashBytes(input).asBytes();
    byte[] expectedHalf = new byte[32];
    System.arraycopy(expectedHash, 0, expectedHalf, 0, 32);
    assertThat(result.toByteArray()).isEqualTo(expectedHalf);
  }

  @Test
  void sha512HalfByteArrayActualHashVerification() {
    // Test with "hello world" string
    byte[] input = "hello world".getBytes();
    UnsignedByteArray result = HashingUtils.sha512Half(input);

    // Verify result is exactly 32 bytes
    assertThat(result.length()).isEqualTo(32);

    // Compute expected value using Guava directly
    byte[] fullHash = Hashing.sha512().hashBytes(input).asBytes();
    assertThat(fullHash.length).isEqualTo(64); // SHA-512 produces 64 bytes

    // Verify we got the first 32 bytes
    byte[] expectedFirstHalf = new byte[32];
    System.arraycopy(fullHash, 0, expectedFirstHalf, 0, 32);
    assertThat(result.toByteArray()).isEqualTo(expectedFirstHalf);
  }

  @Test
  void sha512HalfConsistencyBetweenOverloads() {
    // Verify both overloads produce the same result for the same input
    byte[] input = "consistency test".getBytes();
    UnsignedByteArray inputAsUnsignedByteArray = UnsignedByteArray.of(input);

    UnsignedByteArray result1 = HashingUtils.sha512Half(input);
    UnsignedByteArray result2 = HashingUtils.sha512Half(inputAsUnsignedByteArray);

    assertThat(result1).isEqualTo(result2);
    assertThat(result1.hexValue()).isEqualTo(result2.hexValue());
  }

  ///////////////////
  // addUInt32
  ///////////////////

  @Test
  void addUInt32WithNullUnsignedByteArray() {
    assertThrows(NullPointerException.class, () -> HashingUtils.addUInt32(null, 123));
  }

  @Test
  void addUInt32WithNullInteger() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();
    assertThrows(NullPointerException.class, () -> HashingUtils.addUInt32(bytes, null));
  }

  @Test
  void addUInt32WithZero() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();
    int initialLength = bytes.length();

    UnsignedByteArray result = HashingUtils.addUInt32(bytes, 0);

    // Verify it returns the same instance (mutates in place)
    assertThat(result).isSameAs(bytes);

    // Verify 4 bytes were added
    assertThat(bytes.length()).isEqualTo(initialLength + 4);

    // Verify the bytes are all zeros (big-endian encoding of 0)
    assertThat(bytes.get(0).asInt()).isEqualTo(0);
    assertThat(bytes.get(1).asInt()).isEqualTo(0);
    assertThat(bytes.get(2).asInt()).isEqualTo(0);
    assertThat(bytes.get(3).asInt()).isEqualTo(0);
  }

  @Test
  void addUInt32WithMaxValue() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();

    HashingUtils.addUInt32(bytes, Integer.MAX_VALUE);

    // Verify 4 bytes were added
    assertThat(bytes.length()).isEqualTo(4);

    // Integer.MAX_VALUE = 0x7FFFFFFF in big-endian
    assertThat(bytes.get(0).asInt()).isEqualTo(0x7F);
    assertThat(bytes.get(1).asInt()).isEqualTo(0xFF);
    assertThat(bytes.get(2).asInt()).isEqualTo(0xFF);
    assertThat(bytes.get(3).asInt()).isEqualTo(0xFF);
  }

  @Test
  void addUInt32WithMinValue() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();

    HashingUtils.addUInt32(bytes, Integer.MIN_VALUE);

    // Verify 4 bytes were added
    assertThat(bytes.length()).isEqualTo(4);

    // Integer.MIN_VALUE = 0x80000000 in big-endian (treated as unsigned)
    assertThat(bytes.get(0).asInt()).isEqualTo(0x80);
    assertThat(bytes.get(1).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(2).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(3).asInt()).isEqualTo(0x00);
  }

  @Test
  void addUInt32WithNegativeNumber() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();

    HashingUtils.addUInt32(bytes, -1);

    // Verify 4 bytes were added
    assertThat(bytes.length()).isEqualTo(4);

    // -1 = 0xFFFFFFFF in big-endian (treated as unsigned)
    assertThat(bytes.get(0).asInt()).isEqualTo(0xFF);
    assertThat(bytes.get(1).asInt()).isEqualTo(0xFF);
    assertThat(bytes.get(2).asInt()).isEqualTo(0xFF);
    assertThat(bytes.get(3).asInt()).isEqualTo(0xFF);
  }

  @Test
  void addUInt32BigEndianByteOrderVerification() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();

    // Test with 0x12345678
    HashingUtils.addUInt32(bytes, 0x12345678);

    // Verify big-endian byte order: most significant byte first
    assertThat(bytes.get(0).asInt()).isEqualTo(0x12);
    assertThat(bytes.get(1).asInt()).isEqualTo(0x34);
    assertThat(bytes.get(2).asInt()).isEqualTo(0x56);
    assertThat(bytes.get(3).asInt()).isEqualTo(0x78);
  }

  @Test
  void addUInt32AppendsToExistingBytes() {
    UnsignedByteArray bytes = UnsignedByteArray.of(new byte[] {(byte) 0xAA, (byte) 0xBB});

    HashingUtils.addUInt32(bytes, 0x11223344);

    // Verify original bytes are preserved
    assertThat(bytes.get(0).asInt()).isEqualTo(0xAA);
    assertThat(bytes.get(1).asInt()).isEqualTo(0xBB);

    // Verify new bytes are appended
    assertThat(bytes.get(2).asInt()).isEqualTo(0x11);
    assertThat(bytes.get(3).asInt()).isEqualTo(0x22);
    assertThat(bytes.get(4).asInt()).isEqualTo(0x33);
    assertThat(bytes.get(5).asInt()).isEqualTo(0x44);

    assertThat(bytes.length()).isEqualTo(6);
  }

  @Test
  void addUInt32MultipleIntegers() {
    UnsignedByteArray bytes = UnsignedByteArray.empty();

    // Add multiple integers
    HashingUtils.addUInt32(bytes, 1);
    HashingUtils.addUInt32(bytes, 2);
    HashingUtils.addUInt32(bytes, 3);

    // Verify total length
    assertThat(bytes.length()).isEqualTo(12);

    // Verify first integer (1 = 0x00000001)
    assertThat(bytes.get(0).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(1).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(2).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(3).asInt()).isEqualTo(0x01);

    // Verify second integer (2 = 0x00000002)
    assertThat(bytes.get(4).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(5).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(6).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(7).asInt()).isEqualTo(0x02);

    // Verify third integer (3 = 0x00000003)
    assertThat(bytes.get(8).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(9).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(10).asInt()).isEqualTo(0x00);
    assertThat(bytes.get(11).asInt()).isEqualTo(0x03);
  }
}
