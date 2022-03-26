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

import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Default implementation of {@link WalletFactory}.
 *
 * @deprecated This class will be removed in a future version. Consider using classes from org.xrpl.xrpl4j.core.wallet
 *   instead.
 */
@Deprecated
public class DefaultWalletFactory implements WalletFactory {

  private static final WalletFactory INSTANCE = new DefaultWalletFactory(
    DefaultKeyPairService.getInstance(),
    AddressCodec.getInstance()
  );

  private final KeyPairService keyPairService;
  private final AddressCodec addressCodec;

  /**
   * Construct a {@link DefaultWalletFactory} from a {@link KeyPairService} and an {@link AddressCodec}.
   *
   * @param keyPairService A {@link KeyPairService}.
   * @param addressCodec   An {@link AddressCodec}.
   */
  public DefaultWalletFactory(KeyPairService keyPairService, AddressCodec addressCodec) {
    this.keyPairService = keyPairService;
    this.addressCodec = addressCodec;
  }

  /**
   * Get a JVM wide {@link WalletFactory} instance.
   *
   * @return A static {@link DefaultWalletFactory} instance.
   */
  public static WalletFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SeedWalletGenerationResult randomWallet(boolean isTest) {
    String seed = keyPairService.generateSeed();
    Wallet wallet = this.fromSeed(seed, isTest);

    return SeedWalletGenerationResult.builder()
      .seed(seed)
      .wallet(wallet)
      .build();
  }

  @Override
  public Wallet fromSeed(String seed, boolean isTest) {
    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    return this.fromKeyPair(keyPair, isTest);
  }

  @Override
  public Wallet fromKeyPair(KeyPair keyPair, boolean isTest) {
    Address classicAddress = keyPairService.deriveAddress(keyPair.publicKey());
    return Wallet.builder()
      .privateKey(keyPair.privateKey())
      .publicKey(keyPair.publicKey())
      .isTest(isTest)
      .classicAddress(classicAddress)
      .xAddress(addressCodec.classicAddressToXAddress(classicAddress, isTest))
      .build();
  }

}
