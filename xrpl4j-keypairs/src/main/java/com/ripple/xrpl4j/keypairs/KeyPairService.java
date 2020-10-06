package com.ripple.xrpl4j.keypairs;

import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

public interface KeyPairService {

  KeyPair deriveKeyPair(UnsignedByteArray seed);

  String sign(String message, String privateKey);

  boolean verify(String message, String signature, String publicKey);
}
