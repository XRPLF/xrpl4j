package org.xrpl.xrpl4j.wallet;

import org.xrpl.xrpl4j.keypairs.KeyPair;

/**
 * Interface for constructing XRPL wallets.
 */
public interface WalletFactory {

  /**
   * Generate a {@link Wallet} by generating a random seed and deriving the public/private keys and XRPL
   * address from it.
   *
   * @param isTest
   * @return A {@link SeedWalletGenerationResult}, which contains the seed that was generated,
   * as well as the {@link Wallet}.
   */
  SeedWalletGenerationResult randomWallet(boolean isTest);

  /**
   * Generate a {@link Wallet} from a Base58Check encoded seed value.
   *
   * @param seed   A Base58Check encoded 16 byte seed value.
   * @param isTest
   * @return The {@link Wallet} derived from the seed.
   */
  Wallet fromSeed(String seed, boolean isTest);

  /**
   * Generate a {@link Wallet} from a {@link KeyPair}.
   *
   * @param keyPair The {@link KeyPair} containing the private and public keys used to generate the {@link Wallet}.
   * @param isTest
   * @return The {@link Wallet} derived from the keyPair.
   */
  Wallet fromKeyPair(KeyPair keyPair, boolean isTest);

}
