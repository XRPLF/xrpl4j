package org.xrpl.xrpl4j.crypto.core.wallet;

import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.wallet.ImmutableWallet.Builder;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Represents an Account on the XRP Ledger, otherwise known as a Wallet.
 */
@Value.Immutable
public interface Wallet {

  /**
   * Immutable wallet builder.
   *
   * @return The {@link Builder} for this wallet.
   */
  static Builder builder() {
    return ImmutableWallet.builder();
  }

  /**
   * The private key of the wallet, encoded in hexadecimal. Non-optional because wallets are only used when private keys
   * are in-memory. For delegated key environments, wallet will not be used (instead just a PublicKey will be used).
   *
   * @return An optionally present {@link PrivateKey} containing a private key.
   */
  PrivateKey privateKey();

  /**
   * The public key of this {@link Wallet}.
   *
   * @return A {@link PublicKey} containing the public key.
   */
  PublicKey publicKey();

  /**
   * The XRPL address of this wallet, in the Classic Address form.
   *
   * @return The classic {@link Address} of this wallet.
   */
  @Derived
  default Address address() {
    return AddressUtils.getInstance().deriveAddress(publicKey());
  }

}
