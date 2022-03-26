package org.xrpl.xrpl4j.keypairs;

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

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.security.SecureRandom;

/**
 * An abstract implementation of {@link KeyPairService} which handles common behavior among concrete implementations.
 *
 * @deprecated This class will go away in a future version. Prefer xrpl4j-crypto variants instead.
 */
@Deprecated
public abstract class AbstractKeyPairService implements KeyPairService {

  protected AddressCodec addressCodec = AddressCodec.getInstance();

  @Override
  public String generateSeed() {
    return generateSeed(UnsignedByteArray.of(SecureRandom.getSeed(16)));
  }

  @Override
  public String sign(String message, String privateKey) {
    UnsignedByteArray messageBytes = UnsignedByteArray.fromHex(message);
    return this.sign(messageBytes, privateKey);
  }

  @Override
  public boolean verify(String message, String signature, String publicKey) {
    UnsignedByteArray messageBytes = UnsignedByteArray.fromHex(message);
    return this.verify(messageBytes, signature, publicKey);
  }

  @Override
  public Address deriveAddress(String publicKey) {
    UnsignedByteArray publicKeyBytes = UnsignedByteArray.of(BaseEncoding.base16().decode(publicKey));
    return this.deriveAddress(publicKeyBytes);
  }

  @Override
  public Address deriveAddress(UnsignedByteArray publicKey) {
    return addressCodec.encodeAccountId(computePublicKeyHash(publicKey));
  }

  /**
   * Compute the RIPEMD160 of the SHA256 of the given public key, which can be encoded to an XRPL address.
   *
   * @param publicKey The public key that should be hashed.
   *
   * @return An {@link UnsignedByteArray} containing the non-encoded XRPL address derived from the public key.
   *
   * @deprecated Will be replaced by AddressService.
   */
  @SuppressWarnings("UnstableApiUsage")
  @Deprecated
  private UnsignedByteArray computePublicKeyHash(UnsignedByteArray publicKey) {
    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(sha256, 0, sha256.length);
    byte[] ripemdSha256 = new byte[digest.getDigestSize()];
    digest.doFinal(ripemdSha256, 0);
    return UnsignedByteArray.of(ripemdSha256);
  }

}
