package org.xrpl.xrpl4j.wallet;

import org.immutables.value.Value;

@Value.Immutable
public interface SeedWalletGenerationResult {

  static ImmutableSeedWalletGenerationResult.Builder builder() {
    return ImmutableSeedWalletGenerationResult.builder();
  }

  String seed();

  Wallet wallet();

}
