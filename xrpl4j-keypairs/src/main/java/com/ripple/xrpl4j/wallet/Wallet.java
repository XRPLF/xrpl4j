package com.ripple.xrpl4j.wallet;

import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.ClassicAddress;
import com.ripple.xrpl4j.keypairs.KeyPair;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface Wallet {

  static ImmutableWallet.Builder builder() {
    return ImmutableWallet.builder();
  }

  Optional<String> privateKey();

  String publicKey();

  String classicAddress();

  boolean isTest();

  String xAddress();

}
