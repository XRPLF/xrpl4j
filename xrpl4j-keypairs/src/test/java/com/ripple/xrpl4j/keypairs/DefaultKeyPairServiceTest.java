package com.ripple.xrpl4j.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.model.transactions.Address;
import org.junit.Test;

public class DefaultKeyPairServiceTest {

  KeyPairService keyPairService = DefaultKeyPairService.getInstance();

  @Test
  public void deriveKeyPairFromEd25519Seed() {
    String seed = "sEdSKaCy2JT7JaM7v95H9SxkhP9wS2r";
    KeyPair expectedKeyPair = KeyPair.builder()
        .privateKey("EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3")
        .publicKey("ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63")
        .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }

  @Test
  public void deriveKeyPairFromSecp256k1Seed() {
    String seed = "sp5fghtJtpUorTwvof1NpDXAzNwf5";
    KeyPair expectedKeyPair = KeyPair.builder()
        .privateKey("00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23")
        .publicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
        .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }

  @Test
  public void signMessageWithEd25519PrivateKey() {
    String privateKey = "EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    String expectedSignature = "CB199E1BFD4E3DAA105E4832EEDFA36413E1F44205E4EFB9E27E826044C21E3E2E848BBC8195E8959BADF887599B7310AD1B7047EF11B682E0D068F73749750E";
    String signature = keyPairService.sign(message, privateKey);

    assertThat(signature).isEqualTo(expectedSignature);
  }

  @Test
  public void signMessageWithSecp256k1PrivateKey() {
    String privateKey = "00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    String expectedSignature = "30440220583A91C95E54E6A651C47BEC22744E0B101E2C4060E7B08F6341657DAD9BC3EE02207D1489C7395DB0188D3A56A977ECBA54B36FA9371B40319655B1B4429E33EF2D";
    String signature = keyPairService.sign(message, privateKey);

    assertThat(signature).isEqualTo(expectedSignature);
  }

  @Test
  public void verifyMessageWithEd25519PublicKey() {
    String signature = "CB199E1BFD4E3DAA105E4832EEDFA36413E1F44205E4EFB9E27E826044C21E3E2E848BBC8195E8959BADF887599B7310AD1B7047EF11B682E0D068F73749750E";
    String publicKey = "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    assertThat(keyPairService.verify(message, signature, publicKey)).isTrue();
  }

  @Test
  public void verifyMessageWithSecp256k1PublicKey() {
    String signature = "30440220583A91C95E54E6A651C47BEC22744E0B101E2C4060E7B08F6341657DAD9BC3EE02207D1489C7395DB0188D3A56A977ECBA54B36FA9371B40319655B1B4429E33EF2D";
    String publicKey = "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    assertThat(keyPairService.verify(message, signature, publicKey)).isTrue();
  }

  @Test
  public void deriveAddressFromEd25519PublicKey() {
    String publicKey = "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63";
    Address expectedAddress = Address.of("rLUEXYuLiQptky37CqLcm9USQpPiz5rkpD");
    Address address = keyPairService.deriveAddress(publicKey);
    assertThat(address).isEqualTo(expectedAddress);
  }

  @Test
  public void deriveAddressFromSecp2561kPublicKey() {
    String publicKey = "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435";
    Address expectedAddress = Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1");
    Address address = keyPairService.deriveAddress(publicKey);
    assertThat(address).isEqualTo(expectedAddress);
  }
}
