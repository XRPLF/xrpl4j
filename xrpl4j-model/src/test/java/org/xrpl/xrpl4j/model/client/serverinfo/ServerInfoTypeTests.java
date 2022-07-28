package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ServerInfoType}.
 */
public class ServerInfoTypeTests {

  @Test
  public void validTypesTest() {
    assertThat(ServerInfoType.RIPPLED_SERVER_INFO).isEqualTo(ServerInfoType.RIPPLED_SERVER_INFO);
    assertThat(ServerInfoType.CLIO_SERVER_INFO).isEqualTo(ServerInfoType.CLIO_SERVER_INFO);
    assertThat(ServerInfoType.REPORTING_MODE_SERVER_INFO).isEqualTo(ServerInfoType.REPORTING_MODE_SERVER_INFO);
    assertThat(ServerInfoType.RIPPLED_SERVER_INFO).isNotEqualTo(ServerInfoType.CLIO_SERVER_INFO);
    assertThat(ServerInfoType.CLIO_SERVER_INFO).isNotEqualTo(ServerInfoType.REPORTING_MODE_SERVER_INFO);
  }
}
