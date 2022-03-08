package org.xrpl.xrpl4j.keypairs;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EcDsaSignatureTest {

  @Test
  public void contructBasicSign() {
    /**
     * DER format
     * [
     * 48, its a seq
     * 68, length of the seq/len of array after this point
     * 2, first in the seq
     * 32, the first integer is 32 bytes long
     * r: 8, 102, -56, -15, 111, 21, 0, 64, -118, -30, 27, 64, 86, 40, -100, 23, -117, -54,
     * 100, -103, 55, -36, 53, -83, -83, 96, 24, 77, 99, -49, 74, 6,
     * 2, second is about to begin
     * 32, integer coming next is 32 bytes long
     * s: 120, 76, -73, 11, -93, -1, 79, -50, -45, 1, 39, 92, 108, -19, 6, -16, -41, 99, 109,
     * -58, -66, 6, 89, -24, -61, -91, -50, -118, -15, -34, 1, -43
     * ]
     */
    int[] mySign = {
      48,
      68,
      2,
      32,
      8, 102, -56, -15, 111, 21, 0, 64, -118, -30, 27, 64, 86, 40, -100, 23, -117, -54, 100,
      -103, 55, -36, 53, -83, -83, 96, 24, 77, 99, -49, 74, 6,
      2,
      32,
      120, 76, -73, 11, -93, -1, 79, -50, -45, 1, 39, 92, 108, -19, 6, -16, -41, 99, 109, -58,
      -66, 6, 89, -24, -61, -91, -50, -118, -15, -34, 1, -43};
    EcDsaSignature.fromDer(toByte(mySign));
  }

  @Test
  public void incorrectLengthOfR() {
    int[] mySign = {
      48,
      68,
      2,
      34,
      8, 102, -56, -15, 111, 21, 0, 64, -118, -30, 27, 64, 86, 40, -100, 23, -117, -54, 100, -103,
      55, -36, 53, -83, -83, 96, 24, 77, 99, -49, 74, 6, 120, 76,
      2,
      30,
      -73, 11, -93, -1, 79, -50, -45, 1, 39, 92, 108, -19, 6, -16, -41, 99, 109, -58, -66, 6, 89,
      -24, -61, -91, -50, -118, -15, -34, 1, -43};
    assertThrows(IllegalArgumentException.class, () -> EcDsaSignature.fromDer(toByte(mySign)),
      "r is the wrong length.");
  }

  @Test
  public void incorrectLengthOfS() {
    int[] mySign = {
      48,
      68,
      2,
      30,
      -73, 11, -93, -1, 79, -50, -45, 1, 39, 92, 108, -19, 6, -16, -41, 99, 109, -58, -66, 6, 89, -24,
      -61, -91, -50, -118, -15, -34, 1, -43,
      2,
      34,
      8, 102, -56, -15, 111, 21, 0, 64, -118, -30, 27, 64, 86, 40, -100, 23, -117, -54, 100, -103, 55,
      -36, 53, -83, -83, 96, 24, 77, 99, -49, 74, 6, 120, 76};
    assertThrows(IllegalArgumentException.class, () -> EcDsaSignature.fromDer(toByte(mySign)),
      "s is the wrong length.");
  }

  @Test
  public  void valueZeroForR() {
    int[] mySign = { 48, 6, 2, 1, 0, 2, 1, 1};
    assertThrows(IllegalArgumentException.class, () -> EcDsaSignature.fromDer(toByte(mySign)),
      "r cannot be 0.");
  }

  @Test
  public  void valueZeroForS() {
    int[] mySign = { 48, 6, 2, 1, 1, 2, 1, 0};
    assertThrows(IllegalArgumentException.class, () -> EcDsaSignature.fromDer(toByte(mySign)),
      "s cannot be 0.");
  }

  @Test
  public void negativeValues() {
    int[] mySign = {
      48,
      68,
      2,
      32,
      // the number 128 is to indicate that 128 & 0x80 will result to 1
      // but the implementation takes only positive values hence we cannot
      // reproduce negative values for both r and s.
      128, 50, 50, -112, -54, -39, -67, -46, 95, -117, -66, 59, -14, -65, -23, 127, 50, 50, -112,
      -54, -39, -67, -46, 95, -117, -66, 59, -14, -65, -23, 127, 50,
      2,
      32,
      8, 102, -56, -15, 111, 21, 0, 64, -118, -30, 27, 64, 86, 40, -100, 23, -117, -54, 100, -103, 55,
      -36, 53, -83, -83, 96, 24, 77, 99, -49, 74, 6};
    EcDsaSignature.fromDer(toByte(mySign));
  }


  private byte[] toByte(int[] data) {

    byte[] bytes = new byte[data.length];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) data[i];
    }
    return bytes;
  }
}
