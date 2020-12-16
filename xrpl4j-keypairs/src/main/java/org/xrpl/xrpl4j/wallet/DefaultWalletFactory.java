package org.xrpl.xrpl4j.wallet;

import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Default implementation of {@link WalletFactory}.
 */
public class DefaultWalletFactory implements WalletFactory {

  private static final WalletFactory INSTANCE = new DefaultWalletFactory(
      DefaultKeyPairService.getInstance(),
      AddressCodec.getInstance()
  );

  private final KeyPairService keyPairService;
  private final AddressCodec addressCodec;

  /**
   * Construct a {@link DefaultWalletFactory} from a {@link KeyPairService} and an {@link AddressCodec}.
   *
   * @param keyPairService A {@link KeyPairService}.
   * @param addressCodec   An {@link AddressCodec}.
   */
  public DefaultWalletFactory(KeyPairService keyPairService, AddressCodec addressCodec) {
    this.keyPairService = keyPairService;
    this.addressCodec = addressCodec;
  }

  /**
   * Get a JVM wide {@link WalletFactory} instance.
   *
   * @return A static {@link DefaultWalletFactory} instance.
   */
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
