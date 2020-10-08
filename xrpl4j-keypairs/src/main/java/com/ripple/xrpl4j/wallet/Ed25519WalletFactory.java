package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.keypairs.Ed25519KeyPairService;

import java.util.Objects;

public class Ed25519WalletFactory extends AbstractWalletFactory {

  public Ed25519WalletFactory(boolean isTest) {
    this.addressCodec = new AddressCodec();
    this.keyPairService = new Ed25519KeyPairService(addressCodec);
    this.isTest = isTest;
  }

  public Ed25519WalletFactory(final Ed25519KeyPairService keyPairService, boolean isTest) {
    this.keyPairService = Objects.requireNonNull(keyPairService);
    this.isTest = isTest;
  }

}
