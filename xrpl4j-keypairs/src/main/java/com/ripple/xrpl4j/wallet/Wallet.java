package com.ripple.xrpl4j.wallet;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Represents an Account on the XRP Ledger, otherwise known as a Wallet.
 */
@Value.Immutable
public interface Wallet {

  static ImmutableWallet.Builder builder() {
    return ImmutableWallet.builder();
  }

  /**
   * The private key of the wallet, encoded in hexadecimal.
   */
  Optional<String> privateKey();

  /**
   * The public key of the wallet, encoded in hexadecimal.
   */
  String publicKey();

  /**
   * The XRPL address of this wallet, in the Classic Address form.
   */
  String classicAddress();

  /**
   * The XRPL address of this wallet, in the X-Address form.
   * @return
   */
  String xAddress();

  /**
   * Whether or not this wallet is on XRPL testnet or mainnet.
   */
  boolean isTest();

}
