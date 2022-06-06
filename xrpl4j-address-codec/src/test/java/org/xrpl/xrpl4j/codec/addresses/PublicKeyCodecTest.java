package org.xrpl.xrpl4j.codec.addresses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PublicKeyCodec}.
 */
class PublicKeyCodecTest extends AbstractCodecTest {

  private PublicKeyCodec publicKeyCodec;

  @BeforeEach
  public void setUp() {
    publicKeyCodec = new PublicKeyCodec();
  }

  @Test
  public void encodeDecodeNodePublic() {
    testEncodeDecode(
      nodePublic -> publicKeyCodec.encodeNodePublicKey(nodePublic),
      nodePublic -> publicKeyCodec.decodeNodePublicKey(nodePublic),
      unsignedByteArrayFromHex("0388E5BA87A000CB807240DF8C848EB0B5FFA5C8E5A521BC8E105C0F0A44217828"),
      "n9MXXueo837zYH36DvMc13BwHcqtfAWNJY5czWVbp7uYTj7x17TH"
    );
  }

  @Test
  public void encodeDecodeAccountPublicKey() {
    testEncodeDecode(
      publicKey -> publicKeyCodec.encodeAccountPublicKey(publicKey),
      publicKey -> publicKeyCodec.decodeAccountPublicKey(publicKey),
      unsignedByteArrayFromHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6"),
      "aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3"
    );
  }

}