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

import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.keypairs.exceptions.SigningException;

import java.util.Objects;

/**
 * Implementation of {@link KeyPairService} which uses the ED25519 algorithm to derive keys and sign/verify signatures.
 *
 * @deprecated This class will go away in a future version. Prefer xrpl4j-crypto variants instead.
 */
@Deprecated
public class Ed25519KeyPairService extends AbstractKeyPairService {

  private final Signer signer;

  private static final Ed25519KeyPairService INSTANCE = new Ed25519KeyPairService(AddressCodec.getInstance());

  Ed25519KeyPairService(final AddressCodec addressCodec) {
    Objects.requireNonNull(addressCodec);
    this.addressCodec = addressCodec;
    this.signer = new Ed25519Signer();
  }

  public static Ed25519KeyPairService getInstance() {
    return INSTANCE;
  }

  @Override
  public String generateSeed(UnsignedByteArray entropy) {
    return addressCodec.encodeSeed(entropy, VersionType.ED25519);
  }

  @Override
  public KeyPair deriveKeyPair(String seed) {
    Decoded decoded = addressCodec.decodeSeed(seed);

    if (!decoded.version().equals(Version.ED25519_SEED)) {
      throw new DecodeException("Seed must use ED25519 algorithm. Algorithm was " + decoded.version());
    }

    return deriveKeyPair(decoded.bytes());
  }

  private KeyPair deriveKeyPair(UnsignedByteArray seed) {
    UnsignedByteArray rawPrivateKey = HashUtils.sha512Half(seed);
    Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(rawPrivateKey.toByteArray(), 0);

    Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

    // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of sekp256k1 keys.
    // Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
    UnsignedByte prefix = UnsignedByte.of(0xED);
    UnsignedByteArray prefixedPrivateKey = UnsignedByteArray.of(prefix)
      .append(UnsignedByteArray.of(privateKey.getEncoded()));
    UnsignedByteArray prefixedPublicKey = UnsignedByteArray.of(prefix)
      .append(UnsignedByteArray.of(publicKey.getEncoded()));

    return KeyPair.builder()
      .privateKey(prefixedPrivateKey.hexValue())
      .publicKey(prefixedPublicKey.hexValue())
      .build();
  }

  @Override
  public String sign(UnsignedByteArray message, String privateKey) {
    Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(privateKey.substring(2)), // Remove ED prefix byte
      0
    );

    signer.reset();
    signer.init(true, privateKeyParameters);
    signer.update(message.toByteArray(), 0, message.getUnsignedBytes().size());

    try {
      byte[] signature = signer.generateSignature();
      return BaseEncoding.base16().encode(signature);
    } catch (CryptoException e) {
      throw new SigningException(e);
    }
  }

  @Override
  public boolean verify(UnsignedByteArray message, String signature, String publicKey) {
    Ed25519PublicKeyParameters publicKeyParameters = new Ed25519PublicKeyParameters(
      BaseEncoding.base16().decode(publicKey.substring(2)), // Remove ED prefix byte
      0
    );

    signer.reset();
    signer.init(false, publicKeyParameters);
    signer.update(message.toByteArray(), 0, message.getUnsignedBytes().size());
    return signer.verifySignature(BaseEncoding.base16().decode(signature));
  }
}
