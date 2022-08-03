package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.JobType;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.Load;

/**
 * Unit tests for {@link Load}.
 */
public class ServerInfoLoadTest {

  @Test
  void serverInfoLoadTest() {

    UnsignedInteger threads = UnsignedInteger.valueOf(6);
    ImmutableLoad.Builder serverInfoLoadBuilder = Load.builder()
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

    Load serverInfoLoad = assertDoesNotThrow(() -> serverInfoLoadBuilder.build());

    assertThat(serverInfoLoad.threads()).isEqualTo(UnsignedInteger.valueOf(6));
    assertThat(serverInfoLoad.jobTypes().size()).isEqualTo(8);
  }
}
