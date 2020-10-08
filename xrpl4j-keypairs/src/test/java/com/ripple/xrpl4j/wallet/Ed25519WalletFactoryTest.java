package com.ripple.xrpl4j.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class Ed25519WalletFactoryTest {

  WalletFactory walletFactory;

  @Before
  public void setUp() {
    this.walletFactory = Ed25519WalletFactory.getInstance();
  }

  @Test
  public void generateMainnetWalletFromSeed() {
    String seed = "sEdSKaCy2JT7JaM7v95H9SxkhP9wS2r";
    String privateKey = "EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3";
    String publicKey = "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63";
    String classicAddress = "rLUEXYuLiQptky37CqLcm9USQpPiz5rkpD";
    String xAddress = "XVYaPuwjbmRPA9pdyiXAGXsw8NhgJqESZxvSGuTLKhngUD4";

    Wallet wallet = walletFactory.fromSeed(seed, false);
    assertThat(wallet.privateKey()).isNotEmpty().get().isEqualTo(privateKey);
    assertThat(wallet.publicKey()).isEqualTo(publicKey);
    assertThat(wallet.classicAddress()).isEqualTo(classicAddress);
    assertThat(wallet.xAddress()).isEqualTo(xAddress);
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
