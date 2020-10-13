package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.keypairs.Secp256k1KeyPairService;

/**
 * A {@link WalletFactory} which uses the ECDSA algorithm with the secp2561k curve to generate {@link Wallet}s.
 */
public class Secp256k1WalletFactory extends AbstractWalletFactory {

  private static final Secp256k1WalletFactory INSTANCE = new Secp256k1WalletFactory(Secp256k1KeyPairService.getInstance());

  public static Secp256k1WalletFactory getInstance() {
    return INSTANCE;
  }

  Secp256k1WalletFactory(KeyPairService keyPairService) {
    this.keyPairService = keyPairService;
  }
}
