package org.xrpl.xrpl4j.codec.addresses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.TestConstants;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link PrivateKeyCodec}.
 */
class PrivateKeyCodecTest extends AbstractCodecTest {

  private PrivateKeyCodec privateKeyCodec;

  @BeforeEach
  void setUp() {
    privateKeyCodec = PrivateKeyCodec.getInstance();
  }

  @Test
  public void encodeDecodeEdNodePrivate() {
    testEncodeDecode(
      prefixedNodePrivateKey -> privateKeyCodec.encodeNodePrivateKey(prefixedNodePrivateKey),
      prefixedNodePrivateKey -> privateKeyCodec.decodeNodePrivateKey(prefixedNodePrivateKey),
      TestConstants.getEdPrivateKey().naturalBytes(),
      "paZHnTCvwm4GsxZ7qiA2nUBKE2DLnCoDWYqyocVZfVEZx3kvA4u"
    );
  }

  /**
   * These values come from the rippled codebase in Seed_test.cpp.
   */
  @Test
  public void encodeDecodeNodePrivateFromRippled() {
    Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("masterpassphrase"));

    testEncodeDecode(
      prefixedNodePrivateKey -> privateKeyCodec.encodeNodePrivateKey(prefixedNodePrivateKey),
      prefixedNodePrivateKey -> privateKeyCodec.decodeNodePrivateKey(prefixedNodePrivateKey),
      seed.deriveKeyPair().privateKey().naturalBytes(),
      "paKv46LztLqK3GaKz1rG2nQGN6M4JLyRtxFBYFTw4wAVHtGys36"
    );
  }

  @Test
  public void encodeDecodeEdAccountPrivateKey() {
    testEncodeDecode(
      prefixedAccountPrivateKey -> privateKeyCodec.encodeAccountPrivateKey(prefixedAccountPrivateKey),
      prefixedAccountPrivateKey -> privateKeyCodec.decodeAccountPrivateKey(prefixedAccountPrivateKey),
      TestConstants.getEdPrivateKey().naturalBytes(),
      "pwSmRvZy1c55Kb5tCpBZyq41noSmPn7ynFzUHu1MaoGLAP1VfrT"
    );
  }

  /**
   * These values come from the rippled codebase in Seed_test.cpp.
   */
  @Test
  public void encodeDecodeAccountPrivateKeyFromRippled() {
    Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("masterpassphrase"));

    testEncodeDecode(
      prefixedNodePrivateKey -> privateKeyCodec.encodeAccountPrivateKey(prefixedNodePrivateKey),
      prefixedNodePrivateKey -> privateKeyCodec.decodeAccountPrivateKey(prefixedNodePrivateKey),
      seed.deriveKeyPair().privateKey().naturalBytes(),
      "p9JfM6HHi64m6mvB6v5k7G2b1cXzGmYiCNJf6GHPKvFTWdeRVjh"
    );
  }

  @Test
  public void encodeDecodeEcNodePrivate() {
    testEncodeDecode(
      prefixedNodePrivate -> privateKeyCodec.encodeNodePrivateKey(prefixedNodePrivate),
      prefixedNodePrivate -> privateKeyCodec.decodeNodePrivateKey(prefixedNodePrivate),
      TestConstants.getEcPrivateKey().naturalBytes(),
      "pa1UHARsPMiuDqrJLwFhzcJokoHgyiuaxgPhUGYhkG5ArCfG2vt"
    );
  }

  @Test
  public void encodeDecodeEcAccountPrivateKey() {
    testEncodeDecode(
      prefixedAccountPrivateKey -> privateKeyCodec.encodeAccountPrivateKey(prefixedAccountPrivateKey),
      prefixedNodePrivateKey -> privateKeyCodec.decodeAccountPrivateKey(prefixedNodePrivateKey),
      TestConstants.getEcPrivateKey().naturalBytes(),
      "pwkgeQKfaDDMV7w59LhhuEWMbpX3HG2iXxXGgZuij2j6z1RYY7n"
    );
  }
}