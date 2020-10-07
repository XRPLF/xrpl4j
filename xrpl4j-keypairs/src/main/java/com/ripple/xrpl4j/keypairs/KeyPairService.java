package com.ripple.xrpl4j.keypairs;

import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

public interface KeyPairService {

  KeyPair deriveKeyPair(UnsignedByteArray seed);

  KeyPair deriveKeyPair(String seed);

  String sign(UnsignedByteArray message, String privateKey);

  String sign(String message, String privateKey);

  boolean verify(UnsignedByteArray message, String signature, String publicKey);

  boolean verify(String message, String signature, String publicKey);

  String deriveAddress(String publicKey);

  String deriveAddress(UnsignedByteArray publicKey);

}
