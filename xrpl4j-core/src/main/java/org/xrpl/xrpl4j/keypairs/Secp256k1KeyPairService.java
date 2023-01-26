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

import static org.xrpl.xrpl4j.keypairs.Secp256k1.ecDomainParameters;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Implementation of {@link KeyPairService} which uses the ECDSA algorithm with the secp256k1 curve to derive keys and
 * sign/verify signatures.
 *
 * @deprecated This class will go away in a future version.
 */
@Deprecated
public class Secp256k1KeyPairService extends AbstractKeyPairService {

  private static final Secp256k1KeyPairService INSTANCE = new Secp256k1KeyPairService();

  public static Secp256k1KeyPairService getInstance() {
    return INSTANCE;
  }

  @Override
  public String generateSeed(UnsignedByteArray entropy) {
    return addressCodec.encodeSeed(entropy, KeyType.SECP256K1);
  }


  @Override
  public KeyPair deriveKeyPair(String seed) {
    return deriveKeyPair(addressCodec.decodeSeed(seed).bytes(), 0);
  }

  /**
   * Note that multiple keypairs can be derived from the same seed using the secp2551k algorithm by deriving the keys
   * from a seed and an account index UInt32. However, this use case is incredibly uncommon, and a vast majority of
   * users use 0 for the account index. Thus, this implementation does not allow for custom account indexes for deriving
   * secp2551k keys.
   *
   * @param seed An {@link UnsignedByteArray} of length 16 containing a seed.
   *
   * @return A {@link KeyPair} containing a public/private keypair derived from seed using the secp2561k algorithm.
   */
  private KeyPair deriveKeyPair(UnsignedByteArray seed) {
    int accountNumber = 0;
    return deriveKeyPair(seed, accountNumber);
  }

  private KeyPair deriveKeyPair(UnsignedByteArray seed, int accountNumber) {
    // private key needs to be a BigInteger so we can derive the public key by multiplying G by the private key.
    BigInteger privateKey = derivePrivateKey(seed, accountNumber);
    UnsignedByteArray publicKey = derivePublicKey(privateKey);

    return KeyPair.builder()
      .privateKey(UnsignedByteArray.of(privateKey.toByteArray()).hexValue())
      .publicKey(UnsignedByteArray.of(publicKey.toByteArray()).hexValue())
      .build();
  }

  private UnsignedByteArray derivePublicKey(BigInteger privateKey) {
    return UnsignedByteArray.of(ecDomainParameters.getG().multiply(privateKey).getEncoded(true));
  }

  private BigInteger derivePrivateKey(UnsignedByteArray seed, int accountNumber) {
    BigInteger privateGen = deriveScalar(seed);
    if (accountNumber == -1) {
      return privateGen;
    }

    UnsignedByteArray publicGen = UnsignedByteArray.of(ecDomainParameters.getG().multiply(privateGen).getEncoded(true));
    return deriveScalar(publicGen, accountNumber)
      .add(privateGen)
      .mod(ecDomainParameters.getN());
  }

  private BigInteger deriveScalar(UnsignedByteArray seed) {
    return deriveScalar(seed, Optional.empty());
  }

  private BigInteger deriveScalar(UnsignedByteArray seed, Integer discriminator) {
    return deriveScalar(seed, Optional.of(discriminator));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private BigInteger deriveScalar(UnsignedByteArray seed, Optional<Integer> discriminator) {
    BigInteger key = null;
    UnsignedByteArray seedCopy = UnsignedByteArray.of(seed.toByteArray());
    for (long i = 0; i <= 0xFFFFFFFFL; i++) {
      discriminator.map(d -> HashUtils.addUInt32(seedCopy, d));
      HashUtils.addUInt32(seedCopy, (int) i);
      UnsignedByteArray hash = HashUtils.sha512Half(seedCopy);
      key = new BigInteger(1, hash.toByteArray());
      if (key.compareTo(BigInteger.ZERO) > 0 && key.compareTo(ecDomainParameters.getN()) < 0) {
        break;
      }
    }

    return key;
  }

  @Override
  public String sign(UnsignedByteArray message, String privateKey) {
    UnsignedByteArray messageHash = HashUtils.sha512Half(message);
    EcDsaSignature signature = createEcdsaSignature(messageHash, new BigInteger(privateKey, 16));
    return signature.der().hexValue();
  }

  @SuppressWarnings("LocalVariableName")
  private EcDsaSignature createEcdsaSignature(UnsignedByteArray messageHash, BigInteger privateKey) {
    ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    ECPrivateKeyParameters parameters = new ECPrivateKeyParameters(privateKey, ecDomainParameters);
    signer.init(true, parameters);
    BigInteger[] signatures = signer.generateSignature(messageHash.toByteArray());
    BigInteger r = signatures[0];
    BigInteger s = signatures[1];
    BigInteger otherS = ecDomainParameters.getN().subtract(s);
    if (s.compareTo(otherS) > 0) {
      s = otherS;
    }

    return EcDsaSignature.builder()
      .r(r)
      .s(s)
      .build();
  }

  @Override
  public boolean verify(UnsignedByteArray message, String signature, String publicKey) {
    UnsignedByteArray messageHash = HashUtils.sha512Half(message);
    EcDsaSignature sig = EcDsaSignature.fromDer(BaseEncoding.base16().decode(signature.toUpperCase()));
    if (sig == null) {
      return false;
    }

    ECDSASigner signer = new ECDSASigner();
    ECPoint publicKeyPoint = ecDomainParameters.getCurve().decodePoint(
      BaseEncoding.base16().decode(publicKey.toUpperCase())
    );
    ECPublicKeyParameters params = new ECPublicKeyParameters(publicKeyPoint, ecDomainParameters);
    signer.init(false, params);
    return signer.verifySignature(messageHash.toByteArray(), sig.r(), sig.s());
  }
}
