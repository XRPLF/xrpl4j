package org.xrpl.xrpl4j.wallet;

import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.util.Optional;

/**
 * Represents an Account on the XRP Ledger, otherwise known as a Wallet.
 */
@Value.Immutable
public interface Wallet {

  /**
   * Builder immutable wallet . builder.
   *
   * @return the immutable wallet . builder
   */
  static ImmutableWallet.Builder builder() {
    return ImmutableWallet.builder();
  }

  /**
   * The private key of the wallet, encoded in hexadecimal.
   *
   * @return An optionally present {@link String} containing a private key.
   *
   * @deprecated This method will be removed in a future version. Consider storing private keys in an associated
   *   instance of TransactionSigner.
   */
  @Deprecated
  Optional<String> privateKey();

  /**
   * The public key of the wallet, encoded in hexadecimal.
   *
   * @return A {@link String} containing a public key.
   */
  String publicKey();

  /**
   * The XRPL address of this wallet, in the Classic Address form.
   *
   * @return The classic {@link Address} of this wallet.
   */
  Address classicAddress();

  /**
   * The XRPL address of this wallet, in the X-Address form.
   *
   * @return An {@link XAddress} containing the X-Address of this wallet.
   */
  @SuppressWarnings("MethodName")
  XAddress xAddress();

  /**
   * Whether or not this wallet is on XRPL testnet or mainnet.
   *
   * @return A boolean indicating if this is a testnet or mainnet wallet.
   */
  boolean isTest();

}
