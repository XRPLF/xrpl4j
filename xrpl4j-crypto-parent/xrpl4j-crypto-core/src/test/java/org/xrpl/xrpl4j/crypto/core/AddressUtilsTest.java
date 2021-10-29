package org.xrpl.xrpl4j.crypto.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Unit tests for {@link AddressUtils}.
 */
class AddressUtilsTest {

  @Test
  public void deriveAddressWithNull() {
    Assertions.assertThrows(NullPointerException.class, () -> AddressUtils.getInstance().deriveAddress(null));
  }

  @Test
  public void deriveAddress() {
    PublicKey publicKey = DefaultWalletFactory.getInstance()
      .fromSeed(Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))).publicKey();

    Address address = AddressUtils.getInstance().deriveAddress(publicKey);
    assertThat(address.value()).isEqualTo("rwGWYtRR6jJJJq7FKQg74YwtkiPyUqJ466");
  }

}