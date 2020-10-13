package com.ripple.xrpl4j.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class Secp256k1WalletFactoryTest {

  WalletFactory walletFactory = Secp256k1WalletFactory.getInstance();

  @Test
  public void generateMainnetWalletFromSeed() {
    String seed = "snYP7oArxKepd3GPDcrjMsJYiJeJB";

    Wallet wallet = walletFactory.fromSeed(seed, false);
    assertThat(wallet.xAddress()).isEqualTo("XVnJMYQFqA8EAijpKh5EdjEY5JqyxykMKKSbrUX8uchF6U8");
  }

  @Test
  public void randomMainnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet(false);

    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed(), false);
    assertThat(restoredWallet).isEqualTo(randomWallet.wallet());
  }

  @Test
  public void randomTestnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet(true);

    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed(), true);
    assertThat(restoredWallet).isEqualTo(randomWallet.wallet());
  }
}
