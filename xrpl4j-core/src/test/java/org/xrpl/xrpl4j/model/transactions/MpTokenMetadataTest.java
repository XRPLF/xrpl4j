package org.xrpl.xrpl4j.model.transactions;

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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MpTokenMetadata}.
 */
public class MpTokenMetadataTest {

  @Test
  void testToString() {
    MpTokenMetadata metadata = MpTokenMetadata.of("");
    assertThat(metadata.toString()).isEqualTo("");

    metadata = MpTokenMetadata.of("ABCD");
    assertThat(metadata.toString()).isEqualTo("ABCD");

    metadata = MpTokenMetadata.of("48656C6C6F20576F726C64");
    assertThat(metadata.toString()).isEqualTo("48656C6C6F20576F726C64");
  }
}

