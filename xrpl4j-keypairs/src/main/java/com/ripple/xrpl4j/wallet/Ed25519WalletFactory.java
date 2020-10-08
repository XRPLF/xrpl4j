package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.keypairs.Ed25519KeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPair;

import java.util.Objects;

public class Ed25519WalletFactory implements WalletFactory {

  private Ed25519KeyPairService keyPairService;
  private boolean isTest;

  public Ed25519WalletFactory(boolean isTest) {
    this.keyPairService = new Ed25519KeyPairService();
    this.isTest = isTest;
  }

  public Ed25519WalletFactory(final Ed25519KeyPairService keyPairService, boolean isTest) {
    this.keyPairService = Objects.requireNonNull(keyPairService);
    this.isTest = isTest;
  }

  @Override
  public SeedWalletGenerationResult generateRandomWallet() {
    String seed = keyPairService.generateSeed();
    Wallet wallet = this.fromSeed(seed);

    return SeedWalletGenerationResult.builder()
      .seed(seed)
      .wallet(wallet)
      .build();
  }

  @Override
  public Wallet fromSeed(String seed) {
    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    return this.fromKeyPair(keyPair);
  }

  @Override
  public Wallet fromKeyPair(KeyPair keyPair) {
    return Wallet.builder()
      .publicKey(keyPair.publicKey())
      .privateKey(keyPair.privateKey())
      .test(isTest)
      .build();
  }
}
