package org.xrpl.xrpl4j.crypto.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Unit tests for {@link ServerSecret}.
 */
class ServerSecretTest {

  private ServerSecret serverSecret1;
  private ServerSecret serverSecret2;

  @BeforeEach
  public void setUp() {
    serverSecret1 = ServerSecret.of("hello".getBytes(StandardCharsets.UTF_8));
    serverSecret2 = ServerSecret.of("world".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void serverSecretWithNullBytes() {
    assertThrows(NullPointerException.class, () -> {
      byte[] nullBytes = null;
      ServerSecret.of(nullBytes);
    });
  }

  @Test
  void destroy() {
    assertThat(serverSecret1.isDestroyed()).isFalse();
    serverSecret1.destroy();
    assertThat(serverSecret1.isDestroyed()).isTrue();
    org.assertj.core.api.Assertions.assertThat(Arrays.equals(serverSecret1.value(), new byte[5])).isTrue();
  }

  @Test
  void equals() {
    assertThat(serverSecret1).isEqualTo(serverSecret1);
    assertThat(serverSecret1).isNotEqualTo(serverSecret2);
    assertThat(serverSecret2).isNotEqualTo(serverSecret1);
    assertThat(serverSecret1).isNotEqualTo(new Object());
  }

  @Test
  void hashcode() {
    assertThat(serverSecret1.hashCode()).isEqualTo(serverSecret1.hashCode());
    assertThat(serverSecret2.hashCode()).isEqualTo(serverSecret2.hashCode());
    assertThat(serverSecret2.hashCode()).isNotEqualTo(serverSecret1.hashCode());
  }

  @Test
  void testToString() {
    assertThat(serverSecret1.toString()).isEqualTo(
      "ServerSecret{value=[redacted], destroyed=false}"
    );
  }
}