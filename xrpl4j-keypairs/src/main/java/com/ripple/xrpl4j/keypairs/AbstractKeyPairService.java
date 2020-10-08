package com.ripple.xrpl4j.keypairs;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import org.bouncycastle.crypto.Signer;

public abstract class AbstractKeyPairService implements KeyPairService {

  protected Signer signer;
  protected AddressCodec addressCodec;

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
  public String deriveAddress(String publicKey) {
    UnsignedByteArray publicKeyBytes = UnsignedByteArray.of(BaseEncoding.base16().decode(publicKey));
    return this.deriveAddress(publicKeyBytes);
  }

  @Override
  public String deriveAddress(UnsignedByteArray publicKey) {
    return addressCodec.encodeAccountId(computePublicKeyHash(publicKey));
  }

  /**
   * Compute the RIPEMD160 of the SHA256 of the given public key, which can be encoded to an XRPL address.
   *
   * @param publicKey The public key that should be hashed.
   * @return An {@link UnsignedByteArray} containing the non-encoded XRPL address derived from the public key.
   */
  private UnsignedByteArray computePublicKeyHash(UnsignedByteArray publicKey) {
    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    return UnsignedByteArray.of(Ripemd160.getHash(sha256));
  }

}
