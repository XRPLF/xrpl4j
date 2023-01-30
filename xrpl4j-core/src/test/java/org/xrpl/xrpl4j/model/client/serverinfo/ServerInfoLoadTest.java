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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Assertions;
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

    Load serverInfoLoad = Assertions.assertDoesNotThrow(() -> serverInfoLoadBuilder.build());

    assertThat(serverInfoLoad.threads()).isEqualTo(UnsignedInteger.valueOf(6));
    assertThat(serverInfoLoad.jobTypes().size()).isEqualTo(8);
  }
}
