package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

public class ServerInfoTests extends AbstractJsonTest {

  ServerInfo rippledServerInfo;
  ServerInfo clioServerInfo;
  ServerInfo reportingServerInfo;

  @BeforeEach
  public void setUp() {
    rippledServerInfo = RippledServerInfoTest.rippledServerInfo("54300020-54300729");
    clioServerInfo = ClioServerInfoTest.clioServerInfo("54300020-54300729");
    reportingServerInfo = ReportingModeServerInfoTest.reportingServerInfo("54300020-54300729");
  }

  @Test
  void map() {
    String rippledVersion = rippledServerInfo.map(
      rippledServerInfoCopy -> rippledServerInfoCopy.buildVersion(),
      ($) -> fail(),
      ($) -> fail()
    );
    assertThat(rippledVersion).isEqualTo("1.5.0-rc1");

    String clioVersion = clioServerInfo.map(
      ($) -> fail(),
      clioServerInfoCopy -> clioServerInfoCopy.clioVersion(),
      ($) -> fail()
    );
    assertThat(clioVersion).isEqualTo("1.5.0-rc1");

    String reportingModeVersion = reportingServerInfo.map(
      ($) -> fail(),
      ($) -> fail(),
      reportingModeServerInfo -> reportingModeServerInfo.buildVersion()
    );
    assertThat(reportingModeVersion).isEqualTo("1.5.0-rc1");
  }

  @Test
  void handle() {
    rippledServerInfo.handle(
      rippledServerInfoCopy -> assertThat(rippledServerInfoCopy.buildVersion()).isEqualTo("1.5.0-rc1"),
      ($) -> fail(),
      ($) -> fail()
    );

    clioServerInfo.handle(
      ($) -> fail(),
      clioServerInfoCopy -> assertThat(clioServerInfoCopy.clioVersion()).isEqualTo("1.5.0-rc1"),
      ($) -> fail()
    );

    reportingServerInfo.handle(
      ($) -> fail(),
      ($) -> fail(),
      reportingModeServerInfo -> assertThat(reportingModeServerInfo.buildVersion()).isEqualTo("1.5.0-rc1")
    );
  }
}
