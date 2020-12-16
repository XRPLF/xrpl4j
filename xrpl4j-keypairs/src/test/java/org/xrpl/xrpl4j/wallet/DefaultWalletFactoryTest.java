package org.xrpl.xrpl4j.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

@SuppressWarnings("LocalVariableName")
public class DefaultWalletFactoryTest {

  private final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  @Test
  public void generateMainnetWalletFromEd25519Seed() {
    String seed = "sEdSKaCy2JT7JaM7v95H9SxkhP9wS2r";
    String xAddress = "XVYaPuwjbmRPA9pdyiXAGXsw8NhgJqESZxvSGuTLKhngUD4";

    Wallet wallet = walletFactory.fromSeed(seed, false);
    assertThat(wallet.xAddress()).isEqualTo(xAddress);
  }

  @Test
  public void generateMainnetWalletFromSecp256k1Seed() {
    String seed = "snYP7oArxKepd3GPDcrjMsJYiJeJB";

    Wallet wallet = walletFactory.fromSeed(seed, false);
    assertThat(wallet.xAddress()).isEqualTo("XVnJMYQFqA8EAijpKh5EdjEY5JqyxykMKKSbrUX8uchF6U8");
  }

  @Test
  public void randomMainnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet(false);
    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed(), false);
    assertThat(randomWallet.wallet()).isEqualTo(restoredWallet);
  }

  @Test
  public void randomTestnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet(true);
    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed(), true);
    assertThat(randomWallet.wallet()).isEqualTo(restoredWallet);
  }

}
