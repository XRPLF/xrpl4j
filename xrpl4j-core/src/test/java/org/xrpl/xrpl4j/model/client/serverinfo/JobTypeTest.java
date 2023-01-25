package org.xrpl.xrpl4j.model.client.serverinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.JobType;

/**
 * Unit tests for {@link JobType}.
 */
public class JobTypeTest {

  @Test
  void jobTypeTest() {
    ImmutableJobType.Builder jobTypeBuilder = JobType.builder()
      .jobType("ledgerRequest")
      .peakTime(UnsignedInteger.valueOf(4))
      .perSecond(UnsignedInteger.valueOf(4));

    JobType jobType = assertDoesNotThrow(() -> jobTypeBuilder.build());
    assertThat(jobType.jobType()).isEqualTo("ledgerRequest");
    assertThat(jobType.peakTime().get()).isEqualTo(UnsignedInteger.valueOf(4));
    assertThat(jobType.perSecond().get()).isEqualTo(UnsignedInteger.valueOf(4));
  }
}
