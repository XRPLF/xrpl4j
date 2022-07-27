package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Unit tests for {@link ServerInfoLastClose}.
 */
public class ServerInfoLastCloseTest {

  @Test
  void serverInfoLastCloseTest() {
    BigDecimal convergeTimeSeconds = BigDecimal.valueOf(3.002);
    UnsignedInteger proposers = UnsignedInteger.valueOf(38);

    ImmutableServerInfoLastClose.Builder serverInfoLastCloseBuilder = ServerInfoLastClose.builder()
      .convergeTimeSeconds(convergeTimeSeconds)
      .proposers(proposers);

    ServerInfoLastClose serverInfoLastClose = assertDoesNotThrow(() -> serverInfoLastCloseBuilder.build());

    assertThat(serverInfoLastClose.convergeTimeSeconds()).isEqualTo(BigDecimal.valueOf(3.002));
    assertThat(serverInfoLastClose.proposers()).isEqualTo(UnsignedInteger.valueOf(38));
  }
}
