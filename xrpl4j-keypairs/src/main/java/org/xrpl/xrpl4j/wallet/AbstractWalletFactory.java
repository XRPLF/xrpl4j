package org.xrpl.xrpl4j.wallet;

import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.transactions.Address;

public abstract class AbstractWalletFactory implements WalletFactory {

  protected KeyPairService keyPairService;
  private AddressCodec addressCodec = AddressCodec.getInstance();

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
    Address classicAddress = keyPairService.deriveAddress(keyPair.publicKey());
    return Wallet.builder()
        .privateKey(keyPair.privateKey())
        .publicKey(keyPair.publicKey())
        .isTest(isTest)
        .classicAddress(classicAddress)
        .xAddress(addressCodec.classicAddressToXAddress(classicAddress.value(), isTest))
        .build();
  }

}
