package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LedgerRangeUtilsTest {

  @Test
  public void completeLedgersRanges() {
    ServerInfo serverInfo = ClioServerInfoTest.clioServerInfo("empty");
    assertThat(serverInfo.map(
      ($) -> null,
      serverInfoCopy -> serverInfoCopy.completeLedgerRanges(),
      ($) -> null
    ).size()).isEqualTo(0);


    ServerInfo reportingServerInfo = ReportingModeServerInfoTest.reportingServerInfo("empty");
    assertThat(reportingServerInfo.map(
      ($) -> null,
      ($) -> null,
      param -> param.completeLedgerRanges()
    ).size()).isEqualTo(0);

    ServerInfo rippledServerInfo = RippledServerInfoTest.rippledServerInfo("empty");
    assertThat(rippledServerInfo.map(
      param -> param.completeLedgerRanges(),
      ($) -> null,
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("");
    assertThat(serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo");
    assertThat(serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo100");
    assertThat(serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("1--2");
    assertThat(serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    ).size()).isEqualTo(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("0");
    List<Range<UnsignedLong>> ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo("1");
    ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(2L))).isFalse();

    serverInfo = ClioServerInfoTest.clioServerInfo("1-2");
    ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
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
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(1);

    serverInfo = ClioServerInfoTest.clioServerInfo("0-foo");
    ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo-0");
    ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("foo-0,bar-20");
    ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(0);

    serverInfo = ClioServerInfoTest.clioServerInfo("0-10,20-30");
    ranges = serverInfo.map(
      ($) -> null,
      param -> param.completeLedgerRanges(),
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
      param -> param.completeLedgerRanges(),
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
      param -> param.completeLedgerRanges(),
      ($) -> null
    );
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.MAX_VALUE)).isTrue();
  }
}
