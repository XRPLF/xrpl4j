package org.xrpl.xrpl4j.crypto;

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

import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Constants used for testing.
 */
public interface TestConstants {

  // ED25519 Public Key
  String ED_PUBLIC_KEY_HEX = "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE";
  String ED_PUBLIC_KEY_B58 = "aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B";
  PublicKey ED_PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX);

  String ED_PRIVATE_KEY_HEX = "B224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";
  String ED_PRIVATE_KEY_WITH_PREFIX_HEX = "ED" + ED_PRIVATE_KEY_HEX;

  static PrivateKey getEdPrivateKey() {
    return PrivateKey.fromNaturalBytes(
      UnsignedByteArray.of(BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX)),
      KeyType.ED25519
    );
  }

  // Secp256k1 Public Key
  String EC_PUBLIC_KEY_HEX = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";
  String EC_PUBLIC_KEY_B58 = "aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk";
  PublicKey EC_PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(EC_PUBLIC_KEY_HEX);

  String EC_PRIVATE_KEY_HEX = "DAD3C2B4BF921398932C889DE5335F89D90249355FC6FFB73F1256D2957F9F17";
  String EC_PRIVATE_KEY_WITH_PREFIX_HEX = "00" + EC_PRIVATE_KEY_HEX;

  static PrivateKey getEcPrivateKey() {
    return PrivateKey.fromNaturalBytes(
      UnsignedByteArray.of(BaseEncoding.base16().decode(EC_PRIVATE_KEY_HEX)), KeyType.SECP256K1
    );
  }

  // Both generated from Passphrase.of("hello")
  Address ED_ADDRESS = Address.of("rwGWYtRR6jJJJq7FKQg74YwtkiPyUqJ466");
  Address EC_ADDRESS = Address.of("rD8ATvjj9mfnFuYYTGRNb9DygnJW9JNN1C");
}
