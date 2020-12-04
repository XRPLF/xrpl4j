package org.xrpl.xrpl4j.codec.addresses;

public enum Version {

  ED25519_SEED(new int[] {0x01, 0xE1, 0x4B}),
  FAMILY_SEED(new int[] {0x21}),
  ACCOUNT_ID(new int[] {0}),
  NODE_PUBLIC(new int[] {0x1C}),
  ACCOUNT_PUBLIC_KEY(new int[] {0x23});

  private int[] values;

  Version(int[] values) {
    this.values = values;
  }

  public int[] getValues() {
    return values;
  }

  public byte[] getValuesAsBytes() {
    byte[] bytes = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      bytes[i] = (byte) values[i];
    }
    return bytes;
  }
}
