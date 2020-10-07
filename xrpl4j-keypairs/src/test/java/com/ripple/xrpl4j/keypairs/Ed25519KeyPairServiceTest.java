package com.ripple.xrpl4j.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.Before;
import org.junit.Test;

public class Ed25519KeyPairServiceTest {

  private Ed25519KeyPairService keyPairService;

  @Before
  public void setUp() throws Exception {
    keyPairService = new Ed25519KeyPairService();
  }

  @Test
  public void generateEd25519KeyPairFromSeed() {
    String seed = "sEdSKaCy2JT7JaM7v95H9SxkhP9wS2r";
    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey("EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3")
      .publicKey("ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63")
      .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }

  @Test
  public void signMessageWithEd25519() {
    String privateKey = "EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    String expectedSignature = "CB199E1BFD4E3DAA105E4832EEDFA36413E1F44205E4EFB9E27E826044C21E3E2E848BBC8195E8959BADF887599B7310AD1B7047EF11B682E0D068F73749750E";
    String signature = keyPairService.sign(message, privateKey);

    assertThat(signature).isEqualTo(expectedSignature);
  }

  @Test
  public void verifySignatureWithEd25519() {
    String signature = "CB199E1BFD4E3DAA105E4832EEDFA36413E1F44205E4EFB9E27E826044C21E3E2E848BBC8195E8959BADF887599B7310AD1B7047EF11B682E0D068F73749750E";
    String publicKey = "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    assertThat(keyPairService.verify(message, signature, publicKey)).isTrue();
  }

  @Test
  public void deriveAddressFromEd25519PublicKey() {
    String publicKey = "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63";
    String expectedAddress = "rLUEXYuLiQptky37CqLcm9USQpPiz5rkpD";
    String address = keyPairService.deriveAddress(publicKey);
    assertThat(address).isEqualTo(expectedAddress);
  }
}
