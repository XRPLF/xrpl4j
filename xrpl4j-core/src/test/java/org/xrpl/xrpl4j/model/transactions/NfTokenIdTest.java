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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NfTokenIdTest {

  @Test
  public void nfTokenEquality() {
    assertThat(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65")
      .equals(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65")));

    assertThat(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65")
      .equals(NfTokenId.of("000b013a95f14b0044f78a264e41713c64b5f89242540ee208c3098e00000d65")));

    assertThat(NfTokenId.of("0000000000000000000000000000000000000000000000000000000000000000")).isNotEqualTo(null);
    assertThat(NfTokenId.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(new Object());
  }

  @Test
  public void nfTokenHashcode() {
    assertThat(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65").hashCode())
      .isEqualTo(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65").hashCode());
  }
}
