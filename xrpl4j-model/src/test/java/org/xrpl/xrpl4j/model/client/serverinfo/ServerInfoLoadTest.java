package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

public class ServerInfoLoadTest {

  @Test
  void serverInfoLoadTest() {

    UnsignedLong threads = UnsignedLong.valueOf(6);
    ImmutableServerInfoLoad.Builder serverInfoLoadBuilder = ServerInfoLoad.builder()
      .addJobTypes(
        JobType.builder()
          .jobType("ledgerRequest")
          .peakTime(UnsignedInteger.valueOf(4))
          .perSecond(UnsignedInteger.valueOf(4))
          .build(),
        JobType.builder()
          .jobType("untrustedProposal")
          .peakTime(UnsignedInteger.valueOf(5))
          .perSecond(UnsignedInteger.valueOf(43))
          .build(),
        JobType.builder()
          .jobType("ledgerData")
          .peakTime(UnsignedInteger.valueOf(337))
          .averageTime(UnsignedInteger.valueOf(14))
          .build(),
        JobType.builder()
          .jobType("clientCommand")
          .inProgress(UnsignedInteger.valueOf(1))
          .perSecond(UnsignedInteger.valueOf(9))
          .build(),
        JobType.builder()
          .jobType("transaction")
          .peakTime(UnsignedInteger.valueOf(8))
          .perSecond(UnsignedInteger.valueOf(8))
          .build(),
        JobType.builder()
          .jobType("batch")
          .peakTime(UnsignedInteger.valueOf(5))
          .perSecond(UnsignedInteger.valueOf(6))
          .build(),
        JobType.builder()
          .jobType("advanceLedger")
          .peakTime(UnsignedInteger.valueOf(96))
          .averageTime(UnsignedInteger.valueOf(6))
          .build(),
        JobType.builder()
          .jobType("fetchTxnData")
          .perSecond(UnsignedInteger.valueOf(14))
          .build()
      )
      .threads(threads);

    ServerInfoLoad serverInfoLoad = assertDoesNotThrow(() -> serverInfoLoadBuilder.build());

    assertThat(serverInfoLoad.threads()).isEqualTo(UnsignedLong.valueOf(6));
    assertThat(serverInfoLoad.jobTypes().size()).isEqualTo(8);
  }
}
