package org.xrpl.xrpl4j.crypto.core.wallet;

import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;

/**
 * Interface for constructing XRPL wallets.
 */
public interface WalletFactory {

  /**
   * Generate a {@link Wallet} by generating a random seed and deriving the public/private keys and XRPL address from
   * it.
   *
   * @return A {@link SeedWalletGenerationResult}, which contains the seed that was generated, as well as the {@link
   *   Wallet}.
   */
  SeedWalletGenerationResult randomWallet();

  /**
   * Generate a {@link Wallet} from a Base58Check encoded seed value.
   *
   * @param seed A {@link Seed}, which is a Base58Check encoded 16 byte value.
   *
   * @return The {@link Wallet} derived from the seed.
   */
  Wallet fromSeed(Seed seed);

  /**
   * Generate a {@link Wallet} from a {@link KeyPair}.
   *
   * @param keyPair The {@link KeyPair} containing the private and public keys used to generate the {@link Wallet}.
   *
   * @return The {@link Wallet} derived from the keyPair.
   */
  Wallet fromKeyPair(KeyPair keyPair);

}
