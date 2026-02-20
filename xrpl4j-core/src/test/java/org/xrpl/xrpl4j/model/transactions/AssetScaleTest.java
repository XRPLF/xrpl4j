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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AssetScale}.
 */
public class AssetScaleTest {

  @Test
  void testToString() {
    AssetScale assetScale = AssetScale.of(UnsignedInteger.ZERO);
    assertThat(assetScale.toString()).isEqualTo("0");

    assetScale = AssetScale.of(UnsignedInteger.valueOf(2));
    assertThat(assetScale.toString()).isEqualTo("2");

    assetScale = AssetScale.of(UnsignedInteger.MAX_VALUE);
    assertThat(assetScale.toString()).isEqualTo(UnsignedInteger.MAX_VALUE.toString());
  }
}

