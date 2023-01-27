package org.xrpl.xrpl4j.crypto.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for {@link KeyPair}.
 */
class KeyPairTest {

  @Test
  void build() {
    KeyPair keyPair = KeyPair
      .builder()
      .publicKey(mock(PublicKey.class))
      .privateKey(mock(PrivateKey.class))
      .build();

    assertThat(keyPair).isNotNull();
  }
}