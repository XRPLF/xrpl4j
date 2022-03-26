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

import org.xrpl.xrpl4j.keypairs.KeyPair;

/**
 * Interface for constructing XRPL wallets.
 */
public interface WalletFactory {

  /**
   * Generate a {@link Wallet} by generating a random seed and deriving the public/private keys and XRPL
   * address from it.
   *
   * @param isTest A boolean requesting either a testnet wallet (if {@code true}) or a mainnet wallet
   *               (if {@code false}).
   *
   * @return A {@link SeedWalletGenerationResult}, which contains the seed that was generated,
   *   as well as the {@link Wallet}.
   */
  SeedWalletGenerationResult randomWallet(boolean isTest);

  /**
   * Generate a {@link Wallet} from a Base58Check encoded seed value.
   *
   * @param seed   A Base58Check encoded 16 byte seed value.
   * @param isTest A boolean indicating either a testnet wallet (if {@code true}) or a mainnet wallet
   *               (if {@code false}).
   *
   * @return The {@link Wallet} derived from the seed.
   */
  Wallet fromSeed(String seed, boolean isTest);

  /**
   * Generate a {@link Wallet} from a {@link KeyPair}.
   *
   * @param keyPair The {@link KeyPair} containing the private and public keys used to generate the {@link Wallet}.
   * @param isTest  A boolean indicating either a testnet wallet (if {@code true}) or a mainnet wallet
   *                (if {@code false}).
   *
   * @return The {@link Wallet} derived from the keyPair.
   */
  Wallet fromKeyPair(KeyPair keyPair, boolean isTest);

}
