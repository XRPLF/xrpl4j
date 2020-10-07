package com.ripple.xrpl4j.keypairs;

import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

public abstract class AbstractKeyPairService implements KeyPairService {

  protected AddressCodec addressCodec;

  @Override
  public String deriveAddress(String publicKey) {
    UnsignedByteArray publicKeyBytes = UnsignedByteArray.of(BaseEncoding.base16().decode(publicKey));
    return this.deriveAddress(publicKeyBytes);
  }

  @Override
  public String deriveAddress(UnsignedByteArray publicKey) {
    return addressCodec.encodeAccountId(HashUtils.computePublicKeyHash(publicKey));
  }

}
