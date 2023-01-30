package org.xrpl.xrpl4j.model.client;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Finality}.
 */
public class FinalityTest {

  @Test
  public void createFinalityObjectTest() {

    Finality finality = Finality.builder()
      .finalityStatus(FinalityStatus.NOT_FINAL)
      .resultCode("tesSUCCESS")
      .build();

    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.NOT_FINAL);
    assertThat(finality.resultCode().get()).isEqualTo("tesSUCCESS");
    assertThat(finality.resultCodeSafe()).isEqualTo("tesSUCCESS");
  }

  @Test
  public void throwForMissingResultCodeTest() {

    Finality finality = Finality.builder()
      .finalityStatus(FinalityStatus.NOT_FINAL)
      .build();

    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.NOT_FINAL);
    assertThrows(IllegalStateException.class, () -> finality.resultCodeSafe());
  }
}
