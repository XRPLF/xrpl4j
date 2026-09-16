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

import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for {@link LedgerRangeUtils}.
 */
public class LedgerRangeUtilsTest {

  @Test
  public void completeLedgersRanges() {
    ServerInfo serverInfo = ClioServerInfoTest.clioServerInfo("empty");
    assertThat(serverInfo.map(
      ($) -> null,
      serverInfoCopy -> serverInfoCopy.completeLedgers(),
      ($) -> null
    ).size()).isEqualTo(0);

    ServerInfo reportingServerInfo = ReportingModeServerInfoTest.reportingServerInfo("empty");
    assertThat(reportingServerInfo.map(
      ($) -> null,
      ($) -> null,
      ServerInfo::completeLedgers
    ).size()).isEqualTo(0);

    ServerInfo rippledServerInfo = RippledServerInfoTest.rippledServerInfo("empty");
    assertThat(rippledServerInfo.map(
      ServerInfo::completeLedgers,
      ($) -> null,
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("");
    assertThat(serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo");
    assertThat(serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo100");
    assertThat(serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("1--2");
    assertThat(serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("0");
    List<Range<UnsignedLong>> ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo("1");
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(2L))).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo("1-2");
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(2))).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo("0-" + UnsignedLong.MAX_VALUE.toString());
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(1);

    serverInfo = ClioServerInfoTest.clioServerInfo("0-foo");
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo-0");
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo-0,bar-20");
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("0-10,20-30");
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(2);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(10L))).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(11L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(19L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(20L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(30L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(31L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo("0-10, 20-30 "); // <-- Test the trim function
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(2);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(10L))).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(11L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(19L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(20L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(30L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(31L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo(UnsignedLong.MAX_VALUE.toString());
    ranges = serverInfo.map(
      ($) -> null,
      ServerInfo::completeLedgers,
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.MAX_VALUE)).isTrue();
  }
}
