package com.ripple.xrpl4j.keypairs;

import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.addresses.VersionType;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;

import java.math.BigInteger;
import java.util.Optional;

public class Secp256k1KeyPairService extends AbstractKeyPairService {

  private static final Secp256k1KeyPairService INSTANCE = new Secp256k1KeyPairService();
  private ECDomainParameters ecDomainParameters;

  public static Secp256k1KeyPairService getInstance() {
    return INSTANCE;
  }

  public Secp256k1KeyPairService() {
    X9ECParameters x9ECParameters = SECNamedCurves.getByName("secp256k1");
    this.ecDomainParameters = new ECDomainParameters(
      x9ECParameters.getCurve(),
      x9ECParameters.getG(),
      x9ECParameters.getN(),
      x9ECParameters.getH()
    );
  }

  @Override
  public String generateSeed(UnsignedByteArray entropy) {
    return addressCodec.encodeSeed(entropy, VersionType.SECP256K1);
  }

  /**
   * Note that multiple keypairs can be derived from the same seed using the secp2551k algorithm by
   * deriving the keys from a seed and an account index UInt32. However, this use case is incredibly uncommon,
   * and a vast majority of users use 0 for the account index. Thus, this implementation does not allow for custom
   * account indexes for deriving secp2551k keys.
   *
   * @param seed An {@link UnsignedByteArray} of length 16 containing a seed.
   * @return A {@link KeyPair} containing a public/private keypair derived from seed using the secp2561k algorithm.
   */
  @Override
  public KeyPair deriveKeyPair(UnsignedByteArray seed) {
    int accountNumber = 0;
    return deriveKeyPair(seed, accountNumber);
  }


  @Override
  public KeyPair deriveKeyPair(String seed) {
    return deriveKeyPair(addressCodec.decodeSeed(seed).bytes(), 0);
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
    return null;
  }

  @Override
  public boolean verify(UnsignedByteArray message, String signature, String publicKey) {
    return false;
  }
}
