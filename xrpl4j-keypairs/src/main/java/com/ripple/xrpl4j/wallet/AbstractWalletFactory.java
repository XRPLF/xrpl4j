package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.keypairs.KeyPair;
import com.ripple.xrpl4j.keypairs.KeyPairService;

public abstract class AbstractWalletFactory implements WalletFactory {

  protected KeyPairService keyPairService;
  protected AddressCodec addressCodec;
  protected boolean isTest;

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
    String classicAddress = keyPairService.deriveAddress(keyPair.publicKey());
    return Wallet.builder()
      .privateKey(keyPair.privateKey())
      .publicKey(keyPair.publicKey())
      .isTest(isTest)
      .classicAddress(classicAddress)
      .xAddress(addressCodec.classicAddressToXAddress(classicAddress, isTest))
      .build();
  }

}
