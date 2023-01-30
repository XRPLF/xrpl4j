package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.ServerSecret;

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
