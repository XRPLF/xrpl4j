package org.xrpl.xrpl4j.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class Secp256k1KeyPairServiceTest {

  KeyPairService keyPairService = Secp256k1KeyPairService.getInstance();

  @Test
  public void generateSecp2561kKeyPairFromSeed() {
    String seed = "sp5fghtJtpUorTwvof1NpDXAzNwf5";
    KeyPair expectedKeyPair = KeyPair.builder()
        .privateKey("00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23")
        .publicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
        .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }

  @Test
  public void signMessageWithSecp2561k() {
    String privateKey = "00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    String expectedSignature = "30440220583A91C95E54E6A651C47BEC22744E0B101E2C4060E7B08F6341657DAD9BC3EE02207D1489C7395DB0188D3A56A977ECBA54B36FA9371B40319655B1B4429E33EF2D";
    String signature = keyPairService.sign(message, privateKey);

    assertThat(signature).isEqualTo(expectedSignature);
  }

  @Test
  public void verifySignatureWithSecp2561k() {
    String signature = "30440220583A91C95E54E6A651C47BEC22744E0B101E2C4060E7B08F6341657DAD9BC3EE02207D1489C7395DB0188D3A56A977ECBA54B36FA9371B40319655B1B4429E33EF2D";
    String publicKey = "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    assertThat(keyPairService.verify(message, signature, publicKey)).isTrue();
  }

  @Test
  public void deriveAddressFromSecp2561kPublicKey() {
    String publicKey = "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435";
    Address expectedAddress = Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1");
    Address address = keyPairService.deriveAddress(publicKey);
    assertThat(address).isEqualTo(expectedAddress);
  }
}
