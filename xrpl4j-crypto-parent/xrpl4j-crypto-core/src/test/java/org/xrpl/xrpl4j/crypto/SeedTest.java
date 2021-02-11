package org.xrpl.xrpl4j.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.VersionType;

/**
 * Unit tests for {@link Seed}.
 */
public class SeedTest {

  @Test
  public void testEd25519SeedFromPassphrase() {
    Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    assertThat(seed.decodedSeed().type().get()).isEqualTo(VersionType.ED25519);
    assertThat(BaseEncoding.base64().encode(seed.decodedSeed().bytes().toByteArray()))
      .isEqualTo("m3HSJL1i83hdltRq0+o9cw==");
  }

  @Test
  public void testSecp256k1SeedFromPassphrase() {
    Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    assertThat(seed.decodedSeed().type().get()).isEqualTo(VersionType.SECP256K1);
    assertThat(BaseEncoding.base64().encode(seed.decodedSeed().bytes().toByteArray()))
      .isEqualTo("m3HSJL1i83hdltRq0+o9cw==");
  }

}