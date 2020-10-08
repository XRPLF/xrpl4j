package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.keypairs.Ed25519KeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;

/**
 * A {@link WalletFactory} which uses the ED25519 algorithm to generate {@link Wallet}s.
 */
public class Ed25519WalletFactory extends AbstractWalletFactory {

  public static final Ed25519WalletFactory INSTANCE = new Ed25519WalletFactory(Ed25519KeyPairService.getInstance());

  public static Ed25519WalletFactory getInstance() {
    return INSTANCE;
  }

  Ed25519WalletFactory(KeyPairService keyPairService) {
    this.keyPairService = keyPairService;
  }
}
