package org.xrpl.xrpl4j.codec.binary.types;

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Base class for XRPL Hash types.
 */
public abstract class HashType<T extends HashType<T>> extends SerializedType<T> {

  private final int width;

  public HashType(final UnsignedByteArray bytes, final int width) {
    super(bytes);
    Preconditions.checkArgument(bytes.length() == width, "Invalid hash length " + bytes.length());
    this.width = width;
  }

  @Override
  public String toString() {
    return this.toHex();
  }

  @Override
  public T fromHex(String hex) {
    return super.fromHex(hex);
  }

  public int getWidth() {
    return width;
  }

  /**
   * Returns four bits at the specified depth within a hash
   *
   * @param depth The depth of the four bits
   *
   * @return The number represented by the four bits
   */
  // TODO: Delete if unused?
  public int nibblet(int depth) {
    int byteIx = depth > 0 ? (depth / 2) | 0 : 0;
    int b = this.value().get(byteIx).asInt();
    if (depth % 2 == 0) {
      b = (b & 0xf0) >>> 4;
    } else {
      b = b & 0x0f;
    }
    return b;
  }

}
