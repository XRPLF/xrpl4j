package com.ripple.xrpl4j.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.Decoded;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import org.junit.Before;
import org.junit.Test;

public class Ed25519KeyPairServiceTest {

  private Ed25519KeyPairService keyPairService;
  private AddressCodec addressCodec;

  @Before
  public void setUp() throws Exception {
    keyPairService = new Ed25519KeyPairService();
    addressCodec = new AddressCodec();
  }

  @Test
  public void generateEd25519KeyPairFromSeed() {
    String seed = "sEdSKaCy2JT7JaM7v95H9SxkhP9wS2r";
    Decoded decodedSeed = addressCodec.decodeSeed(seed);
    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey("EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3")
      .publicKey("ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63")
      .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(decodedSeed.bytes());
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }
}
