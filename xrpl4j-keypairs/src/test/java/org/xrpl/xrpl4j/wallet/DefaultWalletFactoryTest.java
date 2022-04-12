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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.XAddress;

@SuppressWarnings("LocalVariableName")
public class DefaultWalletFactoryTest {

  private final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  @Test
  public void generateMainnetWalletFromEd25519Seed() {
    String seed = "sEdSKaCy2JT7JaM7v95H9SxkhP9wS2r";
    XAddress xAddress = XAddress.of("XVYaPuwjbmRPA9pdyiXAGXsw8NhgJqESZxvSGuTLKhngUD4");

    Wallet wallet = walletFactory.fromSeed(seed, false);
    assertThat(wallet.xAddress()).isEqualTo(xAddress);
  }

  @Test
  public void generateMainnetWalletFromSecp256k1Seed() {
    String seed = "snYP7oArxKepd3GPDcrjMsJYiJeJB";

    Wallet wallet = walletFactory.fromSeed(seed, false);
    assertThat(wallet.xAddress()).isEqualTo(XAddress.of("XVnJMYQFqA8EAijpKh5EdjEY5JqyxykMKKSbrUX8uchF6U8"));
  }

  @Test
  public void randomMainnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet(false);
    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed(), false);
    assertThat(randomWallet.wallet()).isEqualTo(restoredWallet);
  }

  @Test
  public void randomTestnetWalletCanBeRegenerated() {
    SeedWalletGenerationResult randomWallet = walletFactory.randomWallet(true);
    Wallet restoredWallet = walletFactory.fromSeed(randomWallet.seed(), true);
    assertThat(randomWallet.wallet()).isEqualTo(restoredWallet);
  }

}
