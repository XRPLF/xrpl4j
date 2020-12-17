package org.xrpl.xrpl4j.wallet;

import org.immutables.value.Value;

/**
 * The result of generating a {@link Wallet} from a seed value.
 */
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
  String seed();

  /**
   * The {@link Wallet} generated from {@link #seed()}.
   *
   * @return A {@link Wallet}.
   */
  Wallet wallet();

}
