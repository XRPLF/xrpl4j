package org.xrpl.xrpl4j.model;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AddressConstants}.
 */
class AddressConstantsTest {

  @Test
  void testAddressConstants() {
    Assertions.assertThat(AddressConstants.ACCOUNT_ZERO.value()).isEqualTo("rrrrrrrrrrrrrrrrrrrrrhoLvTp");
    Assertions.assertThat(AddressConstants.ACCOUNT_ONE.value()).isEqualTo("rrrrrrrrrrrrrrrrrrrrBZbvji");
    Assertions.assertThat(AddressConstants.GENESIS_ACCOUNT.value()).isEqualTo("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    Assertions.assertThat(AddressConstants.NAME_RESERVATION_BLACKHOLE.value()).isEqualTo("rrrrrrrrrrrrrrrrrNAMEtxvNvQ");
    Assertions.assertThat(AddressConstants.NAN_ADDRESS.value()).isEqualTo("rrrrrrrrrrrrrrrrrrrn5RM1rHd");
  }

}
