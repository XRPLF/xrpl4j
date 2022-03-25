package org.xrpl.xrpl4j.wallet;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: keypairs
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import org.immutables.value.Value;

/**
 * The result of generating a {@link Wallet} from a seed value.
 */
@Value.Immutable
public interface SeedWalletGenerationResult {

  static ImmutableSeedWalletGenerationResult.Builder builder() {
    return ImmutableSeedWalletGenerationResult.builder();
  }

  /**
   * The seed value that was used to generate {@link #wallet()}.
   *
   * @return A {@link String} containing the seed.
   */
  String seed();

  /**
   * The {@link Wallet} generated from {@link #seed()}.
   *
   * @return A {@link Wallet}.
   */
  Wallet wallet();

}
