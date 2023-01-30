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
