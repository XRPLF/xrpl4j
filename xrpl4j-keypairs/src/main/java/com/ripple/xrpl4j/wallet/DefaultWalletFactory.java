package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPair;
import com.ripple.xrpl4j.keypairs.KeyPairService;

public class DefaultWalletFactory implements WalletFactory {

  private static final WalletFactory INSTANCE = new DefaultWalletFactory(DefaultKeyPairService.getInstance(), AddressCodec.getInstance());

  private KeyPairService keyPairService;
  private AddressCodec addressCodec;

  public DefaultWalletFactory(KeyPairService keyPairService, AddressCodec addressCodec) {
    this.keyPairService = keyPairService;
    this.addressCodec = addressCodec;
  }

  public static WalletFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SeedWalletGenerationResult randomWallet(boolean isTest) {
    String seed = keyPairService.generateSeed();
    Wallet wallet = this.fromSeed(seed, isTest);

    return SeedWalletGenerationResult.builder()
      .seed(seed)
      .wallet(wallet)
      .build();
  }

  @Override
  public Wallet fromSeed(String seed, boolean isTest) {
    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    return this.fromKeyPair(keyPair, isTest);
  }

  @Override
  public Wallet fromKeyPair(KeyPair keyPair, boolean isTest) {
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
