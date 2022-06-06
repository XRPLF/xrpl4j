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
   * Wallet}.
   * @deprecated This method will be removed in a future release. Prefer {@link #randomWalletEd25519()} or {@link
   * #randomWalletSecp256k1()} instead.
   */
  @Deprecated
  default SeedWalletGenerationResult randomWallet() {
    return randomWalletEd25519();
  }

  /**
   * Generate a {@link Wallet} by generating a random seed and deriving an Ed25519 public/private key pair, XRPL
   * address, and {@link Wallet}.
   *
   * @return A {@link SeedWalletGenerationResult}, which contains the seed that was generated, as well as the {@link
   * Wallet}.
   * @deprecated This method will be removed in a future version. Use {@link Seed#ed25519Seed()} with
   * {@link #fromSeed(Seed)}.
   */
  @Deprecated
  SeedWalletGenerationResult randomWalletEd25519();

  /**
   * Generate a {@link Wallet} by generating a random seed and deriving a Secp256k1 public/private key pair, XRPL
   * address, and {@link Wallet}.
   *
   * @return A {@link SeedWalletGenerationResult}, which contains the seed that was generated, as well as the {@link
   * Wallet}.
   * @deprecated This method will be removed in a future version. Use {@link Seed#secp256k1Seed()} with
   * {@link #fromSeed(Seed)}.
   */
  @Deprecated
  SeedWalletGenerationResult randomWalletSecp256k1();

  /**
   * Generate a {@link Wallet} from a Base58Check encoded seed value.
   *
   * @param seed A {@link Seed}, which is a Base58Check encoded 16 byte value.
   * @return The {@link Wallet} derived from the seed.
   */
  Wallet fromSeed(Seed seed);

  /**
   * Generate a {@link Wallet} from a {@link KeyPair}.
   *
   * @param keyPair The {@link KeyPair} containing the private and public keys used to generate the {@link Wallet}.
   * @return The {@link Wallet} derived from the keyPair.
   */
  Wallet fromKeyPair(KeyPair keyPair);

}
