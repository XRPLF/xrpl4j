package org.xrpl.xrpl4j.keypairs;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.security.SecureRandom;

public abstract class AbstractKeyPairService implements KeyPairService {

  protected Signer signer;
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
    return Address.of(addressCodec.encodeAccountId(computePublicKeyHash(publicKey)));
  }

  /**
   * Compute the RIPEMD160 of the SHA256 of the given public key, which can be encoded to an XRPL address.
   *
   * @param publicKey The public key that should be hashed.
   * @return An {@link UnsignedByteArray} containing the non-encoded XRPL address derived from the public key.
   */
  private UnsignedByteArray computePublicKeyHash(UnsignedByteArray publicKey) {
    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(sha256, 0, sha256.length);
    byte[] ripemdSha256 = new byte[digest.getDigestSize()];
    digest.doFinal(ripemdSha256, 0);
    return UnsignedByteArray.of(ripemdSha256);
  }

}
