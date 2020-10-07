package com.ripple.xrpl4j.keypairs;

import static java.util.Arrays.copyOfRange;

import com.google.common.hash.Hashing;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

public class HashUtils {

  static UnsignedByteArray sha512Half(UnsignedByteArray bytes) {
    return sha512Half(bytes.toByteArray());
  }

  static UnsignedByteArray sha512Half(byte[] bytes) {
    return UnsignedByteArray.of(copyOfRange(Hashing.sha512().hashBytes(bytes).asBytes(), 0, 32));
  }

  static UnsignedByteArray computePublicKeyHash(UnsignedByteArray publicKey) {
    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    return UnsignedByteArray.of(Ripemd160.getHash(sha256));
  }

}
