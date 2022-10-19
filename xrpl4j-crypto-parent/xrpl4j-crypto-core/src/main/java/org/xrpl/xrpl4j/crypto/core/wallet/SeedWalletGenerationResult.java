package org.xrpl.xrpl4j.crypto.core.wallet;

import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;

/**
 * The result of generating a {@link Wallet} from a seed value.
 *
 * @deprecated Prefer {@link KeyPairService}.
 */
@Deprecated
@Value.Immutable
public interface SeedWalletGenerationResult {

  static ImmutableSeedWalletGenerationResult.Builder builder() {
    return ImmutableSeedWalletGenerationResult.builder();
  }

  /**
   * The seed value that was used to generate {@link #wallet()}.
   *
   * @return A {@link String} containing the seed.
   */
  Seed seed();

  /**
   * The {@link Wallet} generated from {@link #seed()}.
   *
   * @return A {@link Wallet}.
   */
  Wallet wallet();

}
