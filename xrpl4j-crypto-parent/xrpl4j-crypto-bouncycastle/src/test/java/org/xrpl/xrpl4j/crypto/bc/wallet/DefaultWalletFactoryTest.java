package org.xrpl.xrpl4j.crypto.bc.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.keys.Entropy;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.wallet.SeedWalletGenerationResult;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.crypto.core.wallet.WalletFactory;

/**
 * Unit tests for {@link BcWalletFactory}.
 */
class DefaultWalletFactoryTest {

  private final WalletFactory walletFactory = BcWalletFactory.getInstance();

  @SuppressWarnings("checkstyle:LocalVariableName")
  @Test
  public void generateWalletFromEd25519Seed() {
    Entropy entropy = Entropy.of(BaseEncoding.base16().decode("0102030405060708090A0B0C0D0E0F10"));
    Seed seed = Seed.ed25519SeedFromEntropy(entropy);
    Wallet wallet = walletFactory.fromSeed(seed);
    assertThat(wallet.publicKey()).isEqualTo(
      PublicKey.fromBase16EncodedPublicKey("ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63"));
    assertThat(wallet.privateKey()).isEqualTo(
      PrivateKey.of(UnsignedByteArray.fromHex("EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3")));
    assertThat(wallet.address().value()).isEqualTo("rLUEXYuLiQptky37CqLcm9USQpPiz5rkpD");
  }

  @Test
  public void generateWalletFromSecp256k1Seed() {
    Entropy entropy = Entropy.of(BaseEncoding.base16().decode("CC4E55BC556DD561CBE990E3D4EF7069"));
    Seed seed = Seed.secp256k1SeedFromEntropy(entropy);
    Wallet wallet = walletFactory.fromSeed(seed);
    assertThat(wallet.publicKey().base16Value()).isEqualTo(
      "02FD0E8479CE8182ABD35157BB0FA17A469AF27DCB12B5DDED697C61809116A33B");
    assertThat(wallet.privateKey().value().hexValue()).isEqualTo(
      "27690792130FC12883E83AE85946B018B3BEDE6EEDCDA3452787A94FC0A17438");
    assertThat(wallet.address().value()).isEqualTo("rByLcEZ7iwTBAK8FfjtpFuT7fCzt4kF4r2");
  }

  @Test
  public void randomMainnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet();
    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed());
    assertThat(randomWallet.wallet()).isEqualTo(restoredWallet);
  }
}