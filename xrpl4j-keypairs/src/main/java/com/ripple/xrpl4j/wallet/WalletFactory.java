package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.keypairs.KeyPair;

public interface WalletFactory {

  SeedWalletGenerationResult generateRandomWallet();

  Wallet fromSeed(String seed);

  Wallet fromKeyPair(KeyPair keyPair);

}
