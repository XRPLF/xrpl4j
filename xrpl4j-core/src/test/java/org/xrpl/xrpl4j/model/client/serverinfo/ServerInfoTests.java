package org.xrpl.xrpl4j.model.client.serverinfo;

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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

/**
 * Unit tests for {@link ServerInfo}.
 */
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
      RippledServerInfo::buildVersion,
      ($) -> fail(),
      ($) -> fail()
    );
    assertThat(rippledVersion).isEqualTo("1.5.0-rc1");

    String clioVersion = clioServerInfo.map(
      ($) -> fail(),
      ClioServerInfo::clioVersion,
      ($) -> fail()
    );
    assertThat(clioVersion).isEqualTo("1.5.0-rc1");

    String reportingModeVersion = reportingServerInfo.map(
      ($) -> fail(),
      ($) -> fail(),
      ReportingModeServerInfo::buildVersion
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
