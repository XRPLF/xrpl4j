package com.ripple.xrpl4j.keypairs;

import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.addresses.VersionType;

import java.util.Map;
import java.util.function.Supplier;

public class DefaultKeyPairService extends AbstractKeyPairService {

  private static final KeyPairService INSTANCE = new DefaultKeyPairService();

  private static final Map<VersionType, Supplier<KeyPairService>> serviceMap =
    new ImmutableMap.Builder<VersionType, Supplier<KeyPairService>>()
      .put(VersionType.SECP256K1, Secp256k1KeyPairService::getInstance)
      .put(VersionType.ED25519, Ed25519KeyPairService::getInstance)
      .build();

  private static KeyPairService getKeyPairServiceByType(VersionType type) {
    return serviceMap.get(type).get();
  }

  public static KeyPairService getInstance() {
    return INSTANCE;
  }

  @Override
  public String generateSeed(UnsignedByteArray entropy) {
    return addressCodec.encodeSeed(entropy, VersionType.ED25519);
  }

  @Override
  public KeyPair deriveKeyPair(String seed) {
    return addressCodec.decodeSeed(seed).type()
      .map(type -> DefaultKeyPairService.getKeyPairServiceByType(type).deriveKeyPair(seed))
      .orElseThrow(() -> new IllegalArgumentException("Unsupported seed type."));
  }

  @Override
  public String sign(UnsignedByteArray message, String privateKey) {
    VersionType privateKeyType = privateKey.startsWith("ED") ? VersionType.ED25519 : VersionType.SECP256K1;
    return DefaultKeyPairService.getKeyPairServiceByType(privateKeyType).sign(message, privateKey);
  }

  @Override
  public boolean verify(UnsignedByteArray message, String signature, String publicKey) {
    VersionType publicKeyType = publicKey.startsWith("ED") ? VersionType.ED25519 : VersionType.SECP256K1;
    return DefaultKeyPairService.getKeyPairServiceByType(publicKeyType).verify(message, signature, publicKey);
  }
}
