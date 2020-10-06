package com.ripple.xrpl4j.keypairs;

import static java.util.Arrays.copyOfRange;

import com.google.common.hash.Hashing;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

public class HashUtils {

  static UnsignedByteArray sha512Half(UnsignedByteArray bytes) {
    return UnsignedByteArray.of(copyOfRange(Hashing.sha512().hashBytes(bytes.toByteArray()).asBytes(), 0, 32));
  }

}
